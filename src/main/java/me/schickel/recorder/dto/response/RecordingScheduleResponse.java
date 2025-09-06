package me.schickel.recorder.dto.response;

import lombok.Data;

@Data
public class RecordingScheduleResponse {
    private Long id;
    private String startTime;
    private String endTime;
    private String m3uUrl;
    private String channel;
    private String fileName;
    private boolean triggered;
}