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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

    public void recordingHandler(RecordingSchedule recordingSchedule) {
        String m3uUrl = recordingSchedule.getM3uUrl();
        String streamCodec = getStreamCodec(m3uUrl);
        LocalDateTime endTime = timeUtils.parseStringToLocalDateTime(recordingSchedule.getEndTime());
        LocalDateTime stopTime = endTime.plusSeconds(20);

        int counter = 1;

        while (timeUtils.parseStringToLocalDateTime(recordingSchedule.getEndTime())
                        .isAfter(LocalDateTime.now().plusSeconds(30)) && scheduleRepository.existsById(recordingSchedule.getId())) {
            String timeToRecord = timeUtils.calculateTimeToRecord(stopTime);
            startRecording(recordingSchedule, streamCodec, m3uUrl, timeToRecord, counter);
            counter++;
        }
    }

    private void startRecording(RecordingSchedule recordingSchedule, String streamCodec, String m3uUrl, String timeToRecord, int counter) {
        if (counter == 1) {
            logger.info("Starting recording for schedule with filename {} (duration: {}s)", recordingSchedule.getFileName(), timeToRecord);
        } else {
            logger.info("Retrying recording for schedule with filename {} for the {} time (remaining: {}s)", recordingSchedule.getFileName(), counter, timeToRecord);
        }
        Path outputPath = Paths.get(decideFileName(recordingSchedule.getFileName(), counter));

        try {
            assert streamCodec != null;
            if (streamCodec.equalsIgnoreCase("h264")) {
                executeRecording(recordingSchedule.getId(), m3uUrl, timeToRecord, outputPath, "h264");
            } else if (streamCodec.equalsIgnoreCase("hevc")) {
                executeRecording(recordingSchedule.getId(), m3uUrl, timeToRecord, outputPath, "hevc");
            } else {
                logger.error("Unsupported codec: {}", streamCodec);
            }
        } catch (Exception e) {
            logger.error("Error recording M3U stream {}: {}", recordingSchedule.getFileName(), e.getMessage());
        }
    }

    private String decideFileName(String fileName, int counter) {
        if (counter == 1) {
            return config.getRecordingFolderPrefix() + fileName;
        } else {
            String fileNameWithoutExtension = fileName.substring(0, fileName.length() - 4);
            String extension = fileName.substring(fileName.length() - 4);
            return config.getRecordingFolderPrefix() + fileNameWithoutExtension + "_" + counter + extension;
        }
    }

    private String getStreamCodec(String m3uUrl) {
        FFprobe probe = FFprobe.atPath();
        FFprobeResult result = probe.setShowStreams(true).setInput(m3uUrl).setLogLevel(LogLevel.WARNING).execute();

        for (com.github.kokorin.jaffree.ffprobe.Stream stream : result.getStreams()) {
            if (stream.getCodecType() == StreamType.VIDEO) {
                log.info("Found video stream with codec: {}", stream.getCodecName());
                return stream.getCodecName();
            }
        }

        return null;
    }

    private void executeRecording(Long scheduleId, String m3uUrl, String timeToRecord, Path outputPath, String codecType) {
        FFmpegResultFuture future = null;
        try {
            future = FFmpeg.atPath()
                  .addInput(UrlInput.fromUrl(m3uUrl))
                  .addArgument("-xerror")
                  .addArguments("-reconnect", "5")
                  .addArguments("-reconnect_streamed", "5")
                  .addArguments("-reconnect_delay_max", "20")
                  .addArguments("-fps_mode", "vfr")
                  .addArguments("-bsf:v", codecType + "_mp4toannexb")
                  .addArguments("-t", timeToRecord)
                  .addArguments("-c", "copy")
                  .addOutput(UrlOutput.toPath(outputPath))
                  .setLogLevel(LogLevel.WARNING)
                  .setProgressListener(progress -> {}) // Silent progress listener to suppress warning
                  .executeAsync();

            activeRecordings.put(scheduleId, future);
            future.toCompletableFuture().join();
            logger.info("Recording complete. Output file: {}", outputPath.toAbsolutePath());
        } catch (Exception e) {
            logger.error("FFmpeg execution failed for {}: {}", outputPath.getFileName(), e.getMessage());
            // Don't throw - let the recording attempt continue or retry
        } finally {
            activeRecordings.remove(scheduleId);
        }
    }

    /**
     * Gracefully stops an active recording by its schedule ID.
     * Attempts graceStop first, then forceStop if needed.
     * @return true if a recording was found and stopped, false if no active recording for this ID
     */
    public boolean stopRecording(Long scheduleId) {
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