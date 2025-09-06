package me.schickel.recorder.mapper;

import me.schickel.recorder.dto.request.RecordingScheduleRequest;
import me.schickel.recorder.dto.response.RecordingScheduleResponse;
import me.schickel.recorder.entity.RecordingSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecordingMapper {
    
    @Mapping(target = "triggered", ignore = true)
    @Mapping(target = "id", ignore = true)
    RecordingSchedule toEntity(RecordingScheduleRequest request);
    
    RecordingScheduleResponse toResponse(RecordingSchedule entity);
}

