package me.schickel.recorder.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.schickel.recorder.dto.request.ChannelRequest;
import me.schickel.recorder.dto.response.ChannelResponse;
import me.schickel.recorder.service.ChannelManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channelUrls")
@RequiredArgsConstructor
public class ChannelController {

    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);
    private final ChannelManagementService channelManagementService;

    @GetMapping("/channels")
    public ResponseEntity<List<ChannelResponse>> getAllChannels() {
        List<ChannelResponse> channels = channelManagementService.getChannels();
        logger.info("Returned all channels to user!");
        return ResponseEntity.ok(channels);
    }

    @PostMapping("/channel")
    public ResponseEntity<String> addChannel(@Valid @RequestBody ChannelRequest request) {
        try {
            channelManagementService.createChannelLink(request);
            String sanitizedName = request.getChannelName() != null ? 
                request.getChannelName().replaceAll("[\r\n]", "_") : "unknown";
            logger.info("Added channel {} to database!", sanitizedName);
            return ResponseEntity.ok("Added channel " + request.getChannelName());
        } catch (Exception e) {
            logger.warn("Failed to add channel: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to add channel: " + e.getMessage());
        }
    }

    @PostMapping("/channels")
    public ResponseEntity<String> addChannels(@Valid @RequestBody List<ChannelRequest> requests) {
        try {
            channelManagementService.createChannelLinks(requests);
            int counter = 1;
            for (ChannelRequest request : requests) {
                String sanitizedName = request.getChannelName() != null ? 
                    request.getChannelName().replaceAll("[\r\n]", "_") : "unknown";
                logger.info("Added channel #{} with the name {} to database!", counter, sanitizedName);
                counter++;
            }
            return ResponseEntity.ok("Added " + requests.size() + " channels");
        } catch (Exception e) {
            logger.warn("Failed to add channels: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Failed to add channels: " + e.getMessage());
        }
    }

    @DeleteMapping("/channel/{id}")
    public ResponseEntity<String> deleteChannel(@PathVariable long id) {
        try {
            String channelName = channelManagementService.deleteChannelLink(id);
            return ResponseEntity.ok("Deleted channel #" + id + " with the name: " + channelName);
        } catch (IllegalArgumentException e) {
            logger.warn("Channel with id {} not found for deletion", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/channel/{id}")
    public ResponseEntity<String> updateChannel(@PathVariable long id, @Valid @RequestBody ChannelRequest request) {
        try {
            channelManagementService.updateChannelLink(id, request);
            return ResponseEntity.ok("Updated channel " + id);
        } catch (IllegalArgumentException e) {
            logger.warn("Channel with id {} not found for update", id);
            return ResponseEntity.notFound().build();
        }
    }


}
