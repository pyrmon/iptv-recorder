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

@Service
@Slf4j
@RequiredArgsConstructor
public class FfmpegService {

    private final ScheduleRepository scheduleRepository;
    private final TimeUtils timeUtils;
    private final RecordingServiceConfig config;
    private static final Logger logger = LoggerFactory.getLogger(FfmpegService.class);

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
                executeRecording(m3uUrl, timeToRecord, outputPath, "h264");
            } else if (streamCodec.equalsIgnoreCase("hevc")) {
                executeRecording(m3uUrl, timeToRecord, outputPath, "hevc");
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

    private void executeRecording(String m3uUrl, String timeToRecord, Path outputPath, String codecType) {
        try {
            FFmpeg.atPath()
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
                  .execute();
            logger.info("Recording complete. Output file: {}", outputPath.toAbsolutePath());
        } catch (Exception e) {
            logger.error("FFmpeg execution failed for {}: {}", outputPath.getFileName(), e.getMessage());
            // Don't throw - let the recording attempt continue or retry
        }
    }
}