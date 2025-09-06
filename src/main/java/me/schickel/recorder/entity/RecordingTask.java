package me.schickel.recorder.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordingTask {
    private String startTime;
    private String endTime;
    private String channel;
    private String m3uUrl;
    private String fileName;
}