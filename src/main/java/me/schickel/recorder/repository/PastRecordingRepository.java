package me.schickel.recorder.repository;

import me.schickel.recorder.entity.PastRecording;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PastRecordingRepository extends CrudRepository<PastRecording, Long> {
    List<PastRecording> findAllByOrderByRecordedAtDesc();
    List<PastRecording> findByChannelNameOrderByRecordedAtDesc(String channelName);
    List<PastRecording> findByRecordedAtBetweenOrderByRecordedAtDesc(LocalDateTime start, LocalDateTime end);
}