package me.schickel.recorder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.schickel.recorder.dto.request.RecordingScheduleRequest;
import me.schickel.recorder.dto.response.RecordingScheduleResponse;
import me.schickel.recorder.service.ScheduleManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recordings")
@RequiredArgsConstructor
public class RecordingController {

    private final ScheduleManagementService scheduleManagementService;
    private static final Logger logger = LoggerFactory.getLogger(RecordingController.class);

    @GetMapping("/schedules")
    public ResponseEntity<List<RecordingScheduleResponse>> getAllSchedules() {
        List<RecordingScheduleResponse> schedules = scheduleManagementService.getAllSchedules();
        logger.info("Returned all schedules to user!");
        return ResponseEntity.ok(schedules);
    }

    @DeleteMapping("/schedule/{id}")
    public ResponseEntity<String> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleManagementService.deleteSchedule(id);
            logger.info("Deleted Schedule with the Id {}", id);
            return ResponseEntity.ok("Schedule deleted successfully!");
        } catch (EmptyResultDataAccessException | IllegalArgumentException e) {
            logger.warn("Schedule with id {} not found for deletion", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/schedule/{id}")
    public ResponseEntity<String> patchSchedule(@PathVariable Long id, @Valid @RequestBody RecordingScheduleRequest request) {
        try {
            scheduleManagementService.patchSchedule(id, request);
            logger.info("Patched Schedule with the Id {}", id);
            return ResponseEntity.ok("Schedule patched successfully!");
        } catch (EmptyResultDataAccessException | IllegalArgumentException e) {
            logger.warn("Schedule with id {} not found for patch", id, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/schedule")
    public ResponseEntity<String> scheduleRecording(@Valid @RequestBody RecordingScheduleRequest request) {
        try {
            scheduleManagementService.saveSchedule(request);
            String sanitizedFileName = request.getFileName() != null ? 
                request.getFileName().replaceAll("[\r\n]", "_") : "unknown";
            logger.info("Recording scheduled for {}", sanitizedFileName);
            return ResponseEntity.ok("Recording scheduled successfully!");
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid schedule request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}