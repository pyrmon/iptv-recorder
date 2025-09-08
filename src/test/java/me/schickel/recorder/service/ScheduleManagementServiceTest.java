package me.schickel.recorder.service;

import me.schickel.recorder.dto.request.RecordingScheduleRequest;
import me.schickel.recorder.dto.response.RecordingScheduleResponse;
import me.schickel.recorder.entity.RecordingSchedule;
import me.schickel.recorder.mapper.RecordingMapper;
import me.schickel.recorder.repository.ScheduleRepository;
import me.schickel.recorder.util.MiscUtils;
import me.schickel.recorder.util.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleManagementServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private TimeUtils timeUtils;
    @Mock
    private RecordingMapper recordingMapper;
    @Mock
    private ChannelManagementService channelManagementService;
    @Mock
    private MiscUtils miscUtils;
    @Mock
    private PastRecordingService pastRecordingService;

    private ScheduleManagementService service;

    @BeforeEach
    void setUp() {
        service = new ScheduleManagementService(scheduleRepository, timeUtils, recordingMapper, channelManagementService, miscUtils, pastRecordingService);
    }

    @Test
    void saveSchedule_shouldSaveValidSchedule() {
        RecordingScheduleRequest request = createValidRequest();
        RecordingSchedule entity = new RecordingSchedule();
        
        when(timeUtils.isBefore(request.getStartTime(), request.getEndTime())).thenReturn(true);
        when(timeUtils.isInPast(request.getEndTime())).thenReturn(false);
        when(miscUtils.isValidUrl(request.getM3uUrl())).thenReturn(true);
        when(recordingMapper.toEntity(request)).thenReturn(entity);

        service.saveSchedule(request);

        verify(scheduleRepository).save(entity);
        assertThat(request.getFileName()).endsWith(".mkv");
    }

    @Test
    void saveSchedule_shouldThrowException_whenStartTimeNotBeforeEndTime() {
        RecordingScheduleRequest request = createValidRequest();
        
        when(timeUtils.isBefore(request.getStartTime(), request.getEndTime())).thenReturn(false);

        assertThatThrownBy(() -> service.saveSchedule(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Start time should be before end time!");
    }

    @Test
    void saveSchedule_shouldThrowException_whenEndTimeIsInPast() {
        RecordingScheduleRequest request = createValidRequest();
        
        when(timeUtils.isBefore(request.getStartTime(), request.getEndTime())).thenReturn(true);
        when(timeUtils.isInPast(request.getEndTime())).thenReturn(true);

        assertThatThrownBy(() -> service.saveSchedule(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End time should be in the future!");
    }

    @Test
    void getAllSchedules_shouldReturnSortedSchedules() {
        List<RecordingSchedule> schedules = new ArrayList<>(List.of(new RecordingSchedule(), new RecordingSchedule()));
        List<RecordingScheduleResponse> responses = List.of(new RecordingScheduleResponse(), new RecordingScheduleResponse());
        
        when(scheduleRepository.findAll()).thenReturn(schedules);
        when(recordingMapper.toResponse(any(RecordingSchedule.class))).thenReturn(responses.get(0), responses.get(1));

        List<RecordingScheduleResponse> result = service.getAllSchedules();

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteSchedule_shouldDeleteExistingSchedule() {
        RecordingSchedule schedule = new RecordingSchedule();
        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.of(schedule));

        service.deleteSchedule(1L);

        verify(pastRecordingService).saveRecordingHistory(schedule, "DELETED_BY_USER");
        verify(scheduleRepository).deleteById(1L);
    }

    @Test
    void deleteSchedule_shouldThrowException_whenScheduleNotFound() {
        when(scheduleRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.deleteSchedule(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schedule not found with id: 1");
    }

    @Test
    void patchSchedule_shouldUpdateExistingSchedule() {
        RecordingScheduleRequest request = createValidRequest();
        RecordingSchedule entity = new RecordingSchedule();
        
        when(scheduleRepository.existsById(1L)).thenReturn(true);
        when(timeUtils.isBefore(request.getStartTime(), request.getEndTime())).thenReturn(true);
        when(timeUtils.isInPast(request.getEndTime())).thenReturn(false);
        when(miscUtils.isValidUrl(request.getM3uUrl())).thenReturn(true);
        when(recordingMapper.toEntity(request)).thenReturn(entity);

        service.patchSchedule(1L, request);

        assertThat(entity.getId()).isEqualTo(1L);
        verify(scheduleRepository).save(entity);
    }

    @Test
    void patchSchedule_shouldThrowException_whenScheduleNotFound() {
        RecordingScheduleRequest request = createValidRequest();
        
        when(scheduleRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> service.patchSchedule(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Schedule not found with id: 1");
    }

    @Test
    void saveSchedule_shouldConvertTsToMkv() {
        RecordingScheduleRequest request = createValidRequest();
        request.setFileName("test.ts");
        
        when(timeUtils.isBefore(request.getStartTime(), request.getEndTime())).thenReturn(true);
        when(timeUtils.isInPast(request.getEndTime())).thenReturn(false);
        when(miscUtils.isValidUrl(request.getM3uUrl())).thenReturn(true);
        when(recordingMapper.toEntity(request)).thenReturn(new RecordingSchedule());

        service.saveSchedule(request);

        assertThat(request.getFileName()).isEqualTo("test.mkv");
    }

    @Test
    void saveSchedule_shouldAddMkvExtension_whenNoExtension() {
        RecordingScheduleRequest request = createValidRequest();
        request.setFileName("test");
        
        when(timeUtils.isBefore(request.getStartTime(), request.getEndTime())).thenReturn(true);
        when(timeUtils.isInPast(request.getEndTime())).thenReturn(false);
        when(miscUtils.isValidUrl(request.getM3uUrl())).thenReturn(true);
        when(recordingMapper.toEntity(request)).thenReturn(new RecordingSchedule());

        service.saveSchedule(request);

        assertThat(request.getFileName()).isEqualTo("test.mkv");
    }

    @Test
    void saveSchedule_shouldThrowException_whenFilenameContainsSeparator() {
        RecordingScheduleRequest request = createValidRequest();
        request.setFileName("test/file.mkv");
        
        when(timeUtils.isBefore(request.getStartTime(), request.getEndTime())).thenReturn(true);
        when(timeUtils.isInPast(request.getEndTime())).thenReturn(false);
        when(miscUtils.isValidUrl(request.getM3uUrl())).thenReturn(true);

        assertThatThrownBy(() -> service.saveSchedule(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid filename!");
    }

    @Test
    void saveSchedule_shouldThrowException_whenFilenameIsNull() {
        RecordingScheduleRequest request = createValidRequest();
        request.setFileName(null);
        
        when(timeUtils.isBefore(request.getStartTime(), request.getEndTime())).thenReturn(true);
        when(timeUtils.isInPast(request.getEndTime())).thenReturn(false);
        when(miscUtils.isValidUrl(request.getM3uUrl())).thenReturn(true);

        assertThatThrownBy(() -> service.saveSchedule(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid filename!");
    }

    private RecordingScheduleRequest createValidRequest() {
        RecordingScheduleRequest request = new RecordingScheduleRequest();
        request.setStartTime("10:00 01/01/2025");
        request.setEndTime("11:00 01/01/2025");
        request.setM3uUrl("http://valid.url");
        request.setFileName("test");
        return request;
    }
}