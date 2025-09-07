package me.schickel.recorder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "past_recordings")
@Getter
@Setter
public class PastRecording {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "m3u_url", nullable = false)
    private String m3uUrl;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "was_triggered", nullable = false)
    private boolean wasTriggered;
}