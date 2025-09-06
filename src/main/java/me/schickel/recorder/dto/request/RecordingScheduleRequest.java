package me.schickel.recorder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@ChannelOrUrlRequired
public class RecordingScheduleRequest {
    
    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "\\d{2}:\\d{2} \\d{2}/\\d{2}/\\d{4}", message = "Start time must be in format HH:mm dd/MM/yyyy")
    private String startTime;
    
    @NotBlank(message = "End time is required")
    @Pattern(regexp = "\\d{2}:\\d{2} \\d{2}/\\d{2}/\\d{4}", message = "End time must be in format HH:mm dd/MM/yyyy")
    private String endTime;
    
    private String m3uUrl;
    
    private String channel;
    
    @NotBlank(message = "File name is required")
    private String fileName;
}