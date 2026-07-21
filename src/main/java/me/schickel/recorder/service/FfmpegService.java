package me.schickel.recorder.service;

import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.*;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.schickel.recorder.config.RecordingServiceConfig;
import me.schickel.recorder.entity.RecordingSchedule;
import me.schickel.recorder.repository.ScheduleRepository;
import me.schickel.recorder.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class FfmpegService {

    private final ScheduleRepository scheduleRepository;
    private final TimeUtils timeUtils;
    private final RecordingServiceConfig config;
    private static final Logger logger = LoggerFactory.getLogger(FfmpegService.class);
    private final ConcurrentMap<Long, FFmpegResultFuture> activeRecordings = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Boolean> stoppedSchedules = new ConcurrentHashMap<>();

    public String recordingHandler(RecordingSchedule recordingSchedule) {
        String m3uUrl = recordingSchedule.getM3uUrl();
        String streamCodec = getStreamCodec(m3uUrl);
        if (streamCodec == null) {
            logger.error("Could not determine codec for {}, aborting recording", recordingSchedule.getFileName());
            return "FAILED_CODEC_DETECTION";
        }
        LocalDateTime endTime = timeUtils.parseStringToLocalDateTime(recordingSchedule.getEndTime());
        LocalDateTime stopTime = endTime.plusSeconds(20);

        int counter = 1;
        List<Path> recordedFiles = new ArrayList<>();

        while (timeUtils.parseStringToLocalDateTime(recordingSchedule.getEndTime())
                        .isAfter(LocalDateTime.now().plusSeconds(30))
                && !stoppedSchedules.containsKey(recordingSchedule.getId())
                && scheduleRepository.existsById(recordingSchedule.getId())) {
            String timeToRecord = timeUtils.calculateTimeToRecord(stopTime);
            Path outputPath = Paths.get(decideFileName(recordingSchedule.getFileName(), counter));
            logger.info("Loop iteration {} for {}: timeToRecord={}s, outputPath={}", counter, recordingSchedule.getFileName(), timeToRecord, outputPath);
            startRecording(recordingSchedule, m3uUrl, timeToRecord, outputPath);
            logger.info("startRecording returned for iteration {} of {}", counter, recordingSchedule.getFileName());
            if (Files.exists(outputPath)) {
                recordedFiles.add(outputPath);
            }
            counter++;
        }
        logger.info("Exited recording loop for {} after {} iterations. stopped={}, exists={}",
            recordingSchedule.getFileName(), counter - 1,
            stoppedSchedules.containsKey(recordingSchedule.getId()),
            scheduleRepository.existsById(recordingSchedule.getId()));
        stoppedSchedules.remove(recordingSchedule.getId());

        if (recordingSchedule.isRemuxToMkv()) {
            remuxToMkv(recordedFiles, recordingSchedule.isKeepOriginalTs());
        }

        return "COMPLETED";
    }

    private void startRecording(RecordingSchedule recordingSchedule, String m3uUrl, String timeToRecord, Path outputPath) {
        int counter = extractCounter(outputPath);
        if (counter == 1) {
            logger.info("Starting recording for schedule with filename {} (duration: {}s)", recordingSchedule.getFileName(), timeToRecord);
        } else {
            logger.info("Retrying recording for schedule with filename {} for the {} time (remaining: {}s)", recordingSchedule.getFileName(), counter, timeToRecord);
        }

        try {
            executeRecording(recordingSchedule.getId(), m3uUrl, timeToRecord, outputPath);
        } catch (Exception e) {
            logger.error("Error recording M3U stream {}: {}", recordingSchedule.getFileName(), e.getMessage());
        }
    }

    private int extractCounter(Path outputPath) {
        String name = outputPath.getFileName().toString();
        String nameWithoutExt = name.substring(0, name.lastIndexOf('.'));
        int underscoreIdx = nameWithoutExt.lastIndexOf('_');
        if (underscoreIdx > 0) {
            try {
                return Integer.parseInt(nameWithoutExt.substring(underscoreIdx + 1));
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    private String decideFileName(String fileName, int counter) {
        if (counter == 1) {
            return config.getRecordingFolderPrefix() + fileName;
        } else {
            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            return config.getRecordingFolderPrefix() + fileNameWithoutExtension + "_" + counter + extension;
        }
    }

    private String getStreamCodec(String m3uUrl) {
        FFprobe probe = FFprobe.atPath();
        FFprobeResult result = probe.setShowStreams(true).setInput(m3uUrl).setLogLevel(LogLevel.WARNING).execute();

        for (com.github.kokorin.jaffree.ffprobe.Stream stream : result.getStreams()) {
            if (stream.getCodecType() == StreamType.VIDEO) {
                String codec = stream.getCodecName();
                log.info("Found video stream with codec: {}", codec);
                return codec;
            }
        }

        return null;
    }

    private void executeRecording(Long scheduleId, String m3uUrl, String timeToRecord, Path outputPath) {
        FFmpegResultFuture future;
        try {
            logger.info("Launching ffmpeg for scheduleId={}, duration={}s, output={}", scheduleId, timeToRecord, outputPath);
            future = FFmpeg.atPath()
                           .addInput(UrlInput.fromUrl(m3uUrl)
                               .addArguments("-reconnect", "1")
                               .addArguments("-reconnect_streamed", "1")
                               .addArguments("-reconnect_delay_max", "5")
                               .addArguments("-reconnect_on_network_error", "1")
                               .addArguments("-rw_timeout", "15000000")
                               .addArguments("-fflags", "+genpts+igndts")
                               .addArguments("-analyzeduration", "10000000")
                               .addArguments("-probesize", "10000000"))
                           .addArguments("-t", timeToRecord)
                           .addArguments("-c", "copy")
                           .addOutput(UrlOutput.toPath(outputPath))
                           .setLogLevel(LogLevel.INFO)
                           .setProgressListener(_ -> {})
                           .executeAsync();

            activeRecordings.put(scheduleId, future);
            logger.info("ffmpeg process started for scheduleId={}, waiting for completion...", scheduleId);
            future.toCompletableFuture().join();
            logger.info("Recording complete. Output file: {}", outputPath.toAbsolutePath());
        } catch (Exception e) {
            logger.error("FFmpeg execution failed for {}: {}", outputPath.getFileName(), e.getMessage(), e);
        } finally {
            activeRecordings.remove(scheduleId);
            logger.info("executeRecording exiting for scheduleId={}, file={}", scheduleId, outputPath.getFileName());
        }
    }

    private void remuxToMkv(List<Path> tsFiles, boolean keepOriginalTs) {
        for (Path tsFile : tsFiles) {
            if (!Files.exists(tsFile)) {
                continue;
            }
            String tsFileName = tsFile.getFileName().toString();
            String mkvFileName = tsFileName.substring(0, tsFileName.lastIndexOf('.')) + ".mkv";
            Path mkvPath = tsFile.getParent().resolve(mkvFileName);

            try {
                logger.info("Remuxing {} to {}", tsFileName, mkvFileName);
                FFmpeg.atPath()
                      .addInput(UrlInput.fromPath(tsFile))
                      .addArguments("-c", "copy")
                      .addOutput(UrlOutput.toPath(mkvPath))
                      .setLogLevel(LogLevel.WARNING)
                      .setOverwriteOutput(true)
                      .execute();
                logger.info("Remux complete: {}", mkvPath.toAbsolutePath());

                if (!keepOriginalTs) {
                    Files.deleteIfExists(tsFile);
                    logger.info("Deleted original TS file: {}", tsFile.toAbsolutePath());
                }
            } catch (Exception e) {
                logger.error("Remux failed for {}: {}", tsFileName, e.getMessage());
            }
        }
    }

    /**
     * Gracefully stops an active recording by its schedule ID.
     * Attempts graceStop first, then forceStop if needed.
     * @return true if a recording was found and stopped, false if no active recording for this ID
     */
    public boolean stopRecording(Long scheduleId) {
        stoppedSchedules.put(scheduleId, Boolean.TRUE);
        FFmpegResultFuture future = activeRecordings.remove(scheduleId);
        if (future == null) {
            return false;
        }
        try {
            logger.info("Gracefully stopping recording for schedule {}", scheduleId);
            future.graceStop();
            if (!future.toCompletableFuture().isDone()) {
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.warn("Grace stop failed for schedule {}, forcing stop", scheduleId);
        }
        if (!future.toCompletableFuture().isDone()) {
            try {
                logger.info("Force stopping recording for schedule {}", scheduleId);
                future.forceStop();
            } catch (Exception e) {
                logger.error("Force stop also failed for schedule {}: {}", scheduleId, e.getMessage());
            }
        }
        return true;
    }

}
