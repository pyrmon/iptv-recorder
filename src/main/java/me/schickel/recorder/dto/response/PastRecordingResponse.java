package me.schickel.recorder.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PastRecordingResponse {
    private Long id;
    private String channelName;
    private String m3uUrl;
    private String fileName;
    private String startTime;
    private String endTime;
    private String recordedAt;
    private boolean wasTriggered;
    private String completionStatus;
}