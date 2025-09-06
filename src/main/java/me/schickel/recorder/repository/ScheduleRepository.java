package me.schickel.recorder.repository;

import me.schickel.recorder.entity.RecordingSchedule;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends CrudRepository<RecordingSchedule, Long> {

    List<RecordingSchedule> findByTriggeredFalse();
    List<RecordingSchedule> findByTriggeredTrue();
}