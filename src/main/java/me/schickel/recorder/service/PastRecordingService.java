package me.schickel.recorder.service;

import lombok.RequiredArgsConstructor;
import me.schickel.recorder.dto.response.PastRecordingResponse;
import me.schickel.recorder.entity.PastRecording;
import me.schickel.recorder.entity.RecordingSchedule;
import me.schickel.recorder.repository.PastRecordingRepository;
import me.schickel.recorder.util.TimeUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PastRecordingService {

    private final PastRecordingRepository pastRecordingRepository;
    private final TimeUtils timeUtils;

    public void saveRecordingHistory(RecordingSchedule recording) {
        PastRecording pastRecording = new PastRecording();
        pastRecording.setChannelName(recording.getChannel());
        pastRecording.setM3uUrl(recording.getM3uUrl());
        pastRecording.setFileName(recording.getFileName());
        pastRecording.setStartTime(timeUtils.parseStringToLocalDateTime(recording.getStartTime()));
        pastRecording.setEndTime(timeUtils.parseStringToLocalDateTime(recording.getEndTime()));
        pastRecording.setRecordedAt(LocalDateTime.now());
        pastRecording.setWasTriggered(recording.isTriggered());
        
        pastRecordingRepository.save(pastRecording);
    }

    public List<PastRecordingResponse> getAllPastRecordings() {
        return pastRecordingRepository.findAllByOrderByRecordedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PastRecordingResponse> getPastRecordingsByChannel(String channelName) {
        return pastRecordingRepository.findByChannelNameOrderByRecordedAtDesc(channelName)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PastRecordingResponse toResponse(PastRecording entity) {
        PastRecordingResponse response = new PastRecordingResponse();
        response.setId(entity.getId());
        response.setChannelName(entity.getChannelName());
        response.setM3uUrl(entity.getM3uUrl());
        response.setFileName(entity.getFileName());
        response.setStartTime(entity.getStartTime());
        response.setEndTime(entity.getEndTime());
        response.setRecordedAt(entity.getRecordedAt());
        response.setWasTriggered(entity.isWasTriggered());
        return response;
    }
}