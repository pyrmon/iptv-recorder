package me.schickel.recorder.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChannelRequest {
    
    @NotBlank(message = "Channel name is required")
    private String channelName;
    
    @NotBlank(message = "M3U URL is required")
    private String m3uUrl;
}