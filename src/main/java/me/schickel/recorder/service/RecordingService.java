package me.schickel.recorder.service;

import lombok.RequiredArgsConstructor;
import me.schickel.recorder.entity.RecordingSchedule;
import me.schickel.recorder.repository.ScheduleRepository;
import me.schickel.recorder.util.ExecutorConfig;
import me.schickel.recorder.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordingService {

    @Value("${allowed.simultaneous.streams}")
    private int allowedSimultaneousStreams;

    private final ScheduleRepository scheduleRepository;
    private final ExecutorConfig executorConfig;
    private final TimeUtils timeUtils;
    private final FfmpegService ffmpegService;
    private final PastRecordingService pastRecordingService;
    private static final Logger logger = LoggerFactory.getLogger(RecordingService.class);

    @EventListener(ApplicationReadyEvent.class)
    public void resumeOngoingRecordings() {
        logger.info("Application started - checking for ongoing recordings to resume");
        List<RecordingSchedule> triggeredRecordings = scheduleRepository.findByTriggeredTrue();
        LocalDateTime now = LocalDateTime.now();
        
        for (RecordingSchedule recording : triggeredRecordings) {
            LocalDateTime endTime = timeUtils.parseStringToLocalDateTime(recording.getEndTime());
            if (now.isBefore(endTime)) {
                logger.info("Resuming ongoing recording: {}", recording.getFileName());
                executorConfig.executorService().submit(() -> ffmpegService.recordingHandler(recording));
            }
        }
    }

    @Scheduled(fixedRate = 30000)
    public void triggerUpcomingRecordings() {
        List<RecordingSchedule> allRecordingsToBeExecuted = new ArrayList<>();
        List<RecordingSchedule> scheduledRecordings = scheduleRepository.findByTriggeredFalse();

        if (!scheduledRecordings.isEmpty()){
            List<RecordingSchedule> ongoingRecordings = findOngoingNonTriggeredRecordings(scheduledRecordings);
            List<RecordingSchedule> upcomingRecordings = findNonTriggeredRecordingsStartingSoon(scheduledRecordings);

            allRecordingsToBeExecuted.addAll(ongoingRecordings);
            allRecordingsToBeExecuted.addAll(upcomingRecordings);
            if (allRecordingsToBeExecuted.size() > 1) {
                allRecordingsToBeExecuted = allRecordingsToBeExecuted.stream()
                                                                     .distinct()
                                                                     .toList();
            }

            if (allRecordingsToBeExecuted.size() > allowedSimultaneousStreams) {
                int numToReduce = allRecordingsToBeExecuted.size() - allowedSimultaneousStreams;
                List<String> skippedRecordings =
                    allRecordingsToBeExecuted.subList(allowedSimultaneousStreams, allRecordingsToBeExecuted.size()).stream()
                                             .map(RecordingSchedule::getFileName) // Get filenames of skipped recordings
                                             .toList();

                if (!skippedRecordings.isEmpty()) {
                    logger.info("Skipping {} recordings due to exceeding allowed parallel streams: {}",
                                numToReduce, String.join(", ", skippedRecordings));
                }
                // Reduce list to allowedSimultaneousStreams size
                allRecordingsToBeExecuted = allRecordingsToBeExecuted.subList(0, allowedSimultaneousStreams);
            }

            // Batch update all recordings
            allRecordingsToBeExecuted.forEach(recording -> recording.setTriggered(true));
            scheduleRepository.saveAll(allRecordingsToBeExecuted);
            
            for (RecordingSchedule recording : allRecordingsToBeExecuted) {
                logger.info("Triggering recording {}", recording.getFileName());
                executorConfig.executorService().submit(() -> ffmpegService.recordingHandler(recording));
            }
            if (!allRecordingsToBeExecuted.isEmpty())
                logger.info("Triggered {} recordings", allRecordingsToBeExecuted.size());
        }
    }

    @Scheduled(fixedRate = 600000)
    public void removeTriggeredRecordings(){
        List<RecordingSchedule> allRecordings = scheduleRepository.findByTriggeredTrue();
        List<RecordingSchedule> recordingsToDelete = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (RecordingSchedule recording : allRecordings) {
            LocalDateTime endTime = timeUtils.parseStringToLocalDateTime(recording.getEndTime());
            if (now.isAfter(endTime)) {
                logger.info("Removing triggered recording {}", recording.getFileName());
                pastRecordingService.saveRecordingHistory(recording, "COMPLETED");
                recordingsToDelete.add(recording);
            }
        }
        
        if (!recordingsToDelete.isEmpty()) {
            scheduleRepository.deleteAll(recordingsToDelete);
        }
    }

    public void forceStopRecording(Long id) {
        RecordingSchedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + id));

        if (!schedule.isTriggered()) {
            throw new IllegalStateException("Schedule " + id + " is not currently recording");
        }

        boolean stopped = ffmpegService.stopRecording(id);
        if (!stopped) {
            logger.warn("No active ffmpeg process found for schedule {}, but it was marked as triggered", id);
        }

        pastRecordingService.saveRecordingHistory(schedule, "STOPPED_BY_USER");
        scheduleRepository.deleteById(id);
        logger.info("Force stopped and removed recording schedule {}: {}", id, schedule.getFileName());
    }

    private List<RecordingSchedule> findNonTriggeredRecordingsStartingSoon(List<RecordingSchedule> allRecordings) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtySecondsFromNow = now.plusSeconds(30);

        List<RecordingSchedule> soonToBeTriggeredRecordings = new ArrayList<>();

        for (RecordingSchedule recording : allRecordings) {
            LocalDateTime startTime = timeUtils.parseStringToLocalDateTime(recording.getStartTime());
            if (startTime.isAfter(now) && startTime.isBefore(thirtySecondsFromNow)) {
                soonToBeTriggeredRecordings.add(recording);
            }
        }
        if (soonToBeTriggeredRecordings.size() > 1) {
            logger.info("Found {} recordings starting soon: {}",
                        soonToBeTriggeredRecordings.size(),
                        soonToBeTriggeredRecordings.stream().map(RecordingSchedule::getFileName).toList());
        }
        return soonToBeTriggeredRecordings;
    }

    private List<RecordingSchedule> findOngoingNonTriggeredRecordings(List<RecordingSchedule> allRecordings) {
        LocalDateTime now = LocalDateTime.now();
        List<RecordingSchedule> ongoingRecordings = new ArrayList<>();

        for (RecordingSchedule recording : allRecordings) {
            LocalDateTime startTime = timeUtils.parseStringToLocalDateTime(recording.getStartTime());
            LocalDateTime endTime = timeUtils.parseStringToLocalDateTime(recording.getEndTime());
            if (now.isAfter(startTime) && now.isBefore(endTime)) {
                ongoingRecordings.add(recording);
            }
        }
        if (ongoingRecordings.size() > 1) {
            logger.info("Found {} ongoing recordings: {}",
                        ongoingRecordings.size(),
                        ongoingRecordings.stream().map(RecordingSchedule::getFileName).toList());
        }
        return ongoingRecordings;
    }
}
