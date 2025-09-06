package me.schickel.recorder.service;

import lombok.RequiredArgsConstructor;
import me.schickel.recorder.dto.request.RecordingScheduleRequest;
import me.schickel.recorder.dto.response.RecordingScheduleResponse;
import me.schickel.recorder.entity.RecordingSchedule;
import me.schickel.recorder.mapper.RecordingMapper;
import me.schickel.recorder.repository.ScheduleRepository;
import me.schickel.recorder.util.MiscUtils;
import me.schickel.recorder.util.TimeUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleManagementService {

    private final ScheduleRepository scheduleRepository;
    private final TimeUtils timeUtils;
    private final RecordingMapper recordingMapper;
    private final ChannelManagementService channelManagementService;
    private final MiscUtils miscUtils;

    public void saveSchedule(RecordingScheduleRequest request) {
        validateAndProcessRequest(request);
        RecordingSchedule entity = recordingMapper.toEntity(request);
        scheduleRepository.save(entity);
    }

    public List<RecordingScheduleResponse> getAllSchedules() {
        List<RecordingSchedule> allSchedules = (List<RecordingSchedule>) scheduleRepository.findAll();
        allSchedules.sort(timeUtils);
        return allSchedules.stream()
                .map(recordingMapper::toResponse)
                .toList();
    }

    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Schedule not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }

    public void patchSchedule(Long id, RecordingScheduleRequest request) {
        if (!scheduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Schedule not found with id: " + id);
        }
        validateAndProcessRequest(request);
        RecordingSchedule entity = recordingMapper.toEntity(request);
        entity.setId(id);
        scheduleRepository.save(entity);
    }

    private void validateAndProcessRequest(RecordingScheduleRequest request) {
        // Validate times
        if (!timeUtils.isBefore(request.getStartTime(), request.getEndTime())) {
            throw new IllegalArgumentException("Start time should be before end time!");
        }
        
        if (timeUtils.isInPast(request.getEndTime())) {
            throw new IllegalArgumentException("End time should be in the future!");
        }
        
        // Handle channel name or direct URL
        if (request.getChannel() != null && !request.getChannel().isEmpty()) {
            if (channelManagementService.existsInChannelLinks(request.getChannel())) {
                String channelUrl = channelManagementService.getUrlByName(request.getChannel());
                request.setM3uUrl(channelUrl);
            } else if (miscUtils.isValidUrl(request.getChannel())) {
                request.setM3uUrl(request.getChannel());
                request.setChannel(null);
            } else {
                throw new IllegalArgumentException("Invalid ChannelName / M3U URL!");
            }
        } else if (!miscUtils.isValidUrl(request.getM3uUrl())) {
            throw new IllegalArgumentException("M3U URL is required!");
        }
        
        // Validate and fix filename
        if (!isValidFilename(request.getFileName())) {
            throw new IllegalArgumentException("Invalid filename!");
        }
        request.setFileName(ensureMkvExtension(request.getFileName()));
    }
    
    private boolean isValidFilename(String filename) {
        if (filename == null) return false;
        if (filename.contains(String.valueOf(File.separatorChar))) {
            return false;
        }
        if (filename.endsWith(".mkv") || filename.endsWith(".mp4") || filename.endsWith(".ts")) {
            return true;
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 || (lastDot > 0 && Character.isLetterOrDigit(filename.charAt(lastDot - 1)));
    }
    
    private String ensureMkvExtension(String filename) {
        if (filename.endsWith(".mp4") || filename.endsWith(".ts")) {
            return filename.substring(0, filename.lastIndexOf(".")) + ".mkv";
        }
        if (!filename.endsWith(".mkv")) {
            return filename + ".mkv";
        }
        return filename;
    }
}