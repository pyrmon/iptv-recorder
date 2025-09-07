package me.schickel.recorder.controller;

import lombok.RequiredArgsConstructor;
import me.schickel.recorder.dto.response.PastRecordingResponse;
import me.schickel.recorder.service.PastRecordingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/past-recordings")
@RequiredArgsConstructor
public class PastRecordingController {

    private static final Logger logger = LoggerFactory.getLogger(PastRecordingController.class);
    private final PastRecordingService pastRecordingService;

    @GetMapping
    public ResponseEntity<List<PastRecordingResponse>> getAllPastRecordings() {
        List<PastRecordingResponse> recordings = pastRecordingService.getAllPastRecordings();
        logger.info("Returned {} past recordings to user!", recordings.size());
        return ResponseEntity.ok(recordings);
    }

    @GetMapping("/channel/{channelName}")
    public ResponseEntity<List<PastRecordingResponse>> getPastRecordingsByChannel(
            @PathVariable String channelName) {
        String sanitizedChannelName = channelName.replaceAll("[\r\n]", "_");
        List<PastRecordingResponse> recordings = pastRecordingService.getPastRecordingsByChannel(channelName);
        logger.info("Returned {} past recordings for channel {} to user!", recordings.size(), sanitizedChannelName);
        return ResponseEntity.ok(recordings);
    }
}