package me.schickel.recorder.service;

import lombok.RequiredArgsConstructor;
import me.schickel.recorder.dto.response.PastRecordingResponse;
import me.schickel.recorder.entity.PastRecording;
import me.schickel.recorder.entity.RecordingSchedule;
import me.schickel.recorder.repository.PastRecordingRepository;
import me.schickel.recorder.util.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PastRecordingService {

    private static final Logger logger = LoggerFactory.getLogger(PastRecordingService.class);
    private final PastRecordingRepository pastRecordingRepository;
    private final TimeUtils timeUtils;

    public void saveRecordingHistory(RecordingSchedule recording, String completionStatus) {
        PastRecording pastRecording = new PastRecording();
        pastRecording.setChannelName(recording.getChannel());
        pastRecording.setM3uUrl(recording.getM3uUrl());
        pastRecording.setFileName(recording.getFileName());
        pastRecording.setStartTime(timeUtils.parseStringToLocalDateTime(recording.getStartTime()));
        pastRecording.setEndTime(timeUtils.parseStringToLocalDateTime(recording.getEndTime()));
        pastRecording.setRecordedAt(LocalDateTime.now());
        pastRecording.setWasTriggered(recording.isTriggered());
        pastRecording.setCompletionStatus(completionStatus != null ? completionStatus : "COMPLETED");
        
        pastRecordingRepository.save(pastRecording);
        String sanitizedFileName = recording.getFileName() != null ? 
            recording.getFileName().replaceAll("[\r\n]", "_") : "unknown";
        logger.info("Saved recording history for {} with status: {}", sanitizedFileName, completionStatus);
    }

    public List<PastRecordingResponse> getAllPastRecordings() {
        List<PastRecordingResponse> recordings = pastRecordingRepository.findAllByOrderByRecordedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
        logger.info("Retrieved {} past recordings from database", recordings.size());
        return recordings;
    }

    public List<PastRecordingResponse> getPastRecordingsByChannel(String channelName) {
        List<PastRecordingResponse> recordings = pastRecordingRepository.findByChannelNameOrderByRecordedAtDesc(channelName)
                .stream()
                .map(this::toResponse)
                .toList();
        String sanitizedChannelName = channelName != null ? channelName.replaceAll("[\r\n]", "_") : "unknown";
        logger.info("Retrieved {} past recordings for channel {} from database", recordings.size(), sanitizedChannelName);
        return recordings;
    }

    private PastRecordingResponse toResponse(PastRecording entity) {
        PastRecordingResponse response = new PastRecordingResponse();
        response.setId(entity.getId());
        response.setChannelName(entity.getChannelName());
        response.setM3uUrl(entity.getM3uUrl());
        response.setFileName(entity.getFileName());
        response.setStartTime(timeUtils.parseLocalDateTimeToString(entity.getStartTime()));
        response.setEndTime(timeUtils.parseLocalDateTimeToString(entity.getEndTime()));
        response.setRecordedAt(timeUtils.parseLocalDateTimeToString(entity.getRecordedAt()));
        response.setWasTriggered(entity.isWasTriggered());
        response.setCompletionStatus(entity.getCompletionStatus());
        return response;
    }
}