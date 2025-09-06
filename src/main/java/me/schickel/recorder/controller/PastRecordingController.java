package me.schickel.recorder.controller;

import lombok.RequiredArgsConstructor;
import me.schickel.recorder.dto.response.PastRecordingResponse;
import me.schickel.recorder.service.PastRecordingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/past-recordings")
@RequiredArgsConstructor
public class PastRecordingController {

    private final PastRecordingService pastRecordingService;

    @GetMapping
    public ResponseEntity<List<PastRecordingResponse>> getAllPastRecordings() {
        return ResponseEntity.ok(pastRecordingService.getAllPastRecordings());
    }

    @GetMapping("/channel/{channelName}")
    public ResponseEntity<List<PastRecordingResponse>> getPastRecordingsByChannel(
            @PathVariable String channelName) {
        return ResponseEntity.ok(pastRecordingService.getPastRecordingsByChannel(channelName));
    }
}