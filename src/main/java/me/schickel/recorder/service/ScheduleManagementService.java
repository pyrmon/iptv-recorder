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
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleManagementService {

    private final ScheduleRepository scheduleRepository;
    private final TimeUtils timeUtils;
    private final RecordingMapper recordingMapper;
    private final ChannelManagementService channelManagementService;
    private final MiscUtils miscUtils;
    private final PastRecordingService pastRecordingService;

    public void saveSchedule(RecordingScheduleRequest request) {
        validateAndProcessRequest(request, null);
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
        RecordingSchedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found with id: " + id));
        
        pastRecordingService.saveRecordingHistory(schedule, "DELETED_BY_USER");
        scheduleRepository.deleteById(id);
    }

    public void patchSchedule(Long id, RecordingScheduleRequest request) {
        if (!scheduleRepository.existsById(id)) {
            throw new IllegalArgumentException("Schedule not found with id: " + id);
        }
        validateAndProcessRequest(request, id);
        RecordingSchedule entity = recordingMapper.toEntity(request);
        entity.setId(id);
        scheduleRepository.save(entity);
    }

    private void validateAndProcessRequest(RecordingScheduleRequest request, Long excludeId) {
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

        // Prevent overlapping schedules (half-open intervals [start, end))
        LocalDateTime newStart = timeUtils.parseStringToLocalDateTime(request.getStartTime());
        LocalDateTime newEnd = timeUtils.parseStringToLocalDateTime(request.getEndTime());

        List<RecordingSchedule> existingSchedules = (List<RecordingSchedule>) scheduleRepository.findAll();
        for (RecordingSchedule existing : existingSchedules) {
            if (excludeId != null && excludeId.equals(existing.getId())) {
                continue; // Skip self when patching
            }
            LocalDateTime existStart = timeUtils.parseStringToLocalDateTime(existing.getStartTime());
            LocalDateTime existEnd = timeUtils.parseStringToLocalDateTime(existing.getEndTime());
            if (intervalsOverlapHalfOpen(newStart, newEnd, existStart, existEnd)) {
                throw new IllegalArgumentException("Schedule overlaps with an existing schedule (" + existing.getFileName() + ")");
            }
        }
    }

    private boolean intervalsOverlapHalfOpen(LocalDateTime s1, LocalDateTime e1, LocalDateTime s2, LocalDateTime e2) {
        // Treat intervals as [start, end); allow adjacency at exact boundaries
        return s1.isBefore(e2) && s2.isBefore(e1);
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