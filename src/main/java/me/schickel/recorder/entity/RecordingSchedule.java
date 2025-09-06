package me.schickel.recorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recording_schedules")
@Getter
@Setter
public class RecordingSchedule extends RecordingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time", nullable = false)
    private String startTime;

    @Column(name = "end_time", nullable = false)
    private String endTime;

    @Column(name = "m3u_url", nullable = false)
    private String m3uUrl;

    @Column(name = "channel_name")
    private String channel;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "triggered", nullable = false)
    private boolean triggered;
}
