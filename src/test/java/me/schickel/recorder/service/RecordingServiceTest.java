package me.schickel.recorder.service;

import me.schickel.recorder.entity.RecordingSchedule;
import me.schickel.recorder.repository.ScheduleRepository;
import me.schickel.recorder.util.ExecutorConfig;
import me.schickel.recorder.util.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class RecordingServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private ExecutorConfig executorConfig;
    @Mock
    private TimeUtils timeUtils;
    @Mock
    private FfmpegService ffmpegService;
    @Mock
    private PastRecordingService pastRecordingService;
    @Mock
    private ExecutorService executorService;

    private RecordingService recordingService;

    @BeforeEach
    void setUp() {
        recordingService = new RecordingService(scheduleRepository, executorConfig, timeUtils, ffmpegService, pastRecordingService);
        ReflectionTestUtils.setField(recordingService, "allowedSimultaneousStreams", 2);
        when(executorConfig.executorService()).thenReturn(executorService);
    }

    @Test
    void resumeOngoingRecordings_shouldResumeValidRecordings() {
        RecordingSchedule recording = createRecording("test.mkv");
        LocalDateTime futureEndTime = LocalDateTime.now().plusHours(1);
        
        when(scheduleRepository.findByTriggeredTrue()).thenReturn(List.of(recording));
        when(timeUtils.parseStringToLocalDateTime(recording.getEndTime())).thenReturn(futureEndTime);

        recordingService.resumeOngoingRecordings();

        verify(executorService).submit(any(Runnable.class));
    }

    @Test
    void resumeOngoingRecordings_shouldSkipExpiredRecordings() {
        RecordingSchedule recording = createRecording("test.mkv");
        LocalDateTime pastEndTime = LocalDateTime.now().minusHours(1);
        
        when(scheduleRepository.findByTriggeredTrue()).thenReturn(List.of(recording));
        when(timeUtils.parseStringToLocalDateTime(recording.getEndTime())).thenReturn(pastEndTime);

        recordingService.resumeOngoingRecordings();

        verify(executorService, never()).submit(any(Runnable.class));
    }

    @Test
    void triggerUpcomingRecordings_shouldTriggerUpcomingRecordings() {
        RecordingSchedule recording = createRecording("test.mkv");
        LocalDateTime startTime = LocalDateTime.now().plusSeconds(15);
        
        when(scheduleRepository.findByTriggeredFalse()).thenReturn(List.of(recording));
        when(timeUtils.parseStringToLocalDateTime(recording.getStartTime())).thenReturn(startTime);

        recordingService.triggerUpcomingRecordings();

        verify(recording).setTriggered(true);
        verify(scheduleRepository).saveAll(List.of(recording));
        verify(executorService).submit(any(Runnable.class));
    }

    @Test
    void triggerUpcomingRecordings_shouldLimitSimultaneousStreams() {
        RecordingSchedule recording1 = createRecording("test1.mkv");
        RecordingSchedule recording2 = createRecording("test2.mkv");
        RecordingSchedule recording3 = createRecording("test3.mkv");
        LocalDateTime startTime = LocalDateTime.now().plusSeconds(15);
        
        when(scheduleRepository.findByTriggeredFalse()).thenReturn(List.of(recording1, recording2, recording3));
        when(timeUtils.parseStringToLocalDateTime(anyString())).thenReturn(startTime);

        recordingService.triggerUpcomingRecordings();

        verify(scheduleRepository).saveAll(argThat(recordings -> ((java.util.Collection<?>) recordings).size() == 2));
        verify(executorService, times(2)).submit(any(Runnable.class));
    }

    @Test
    void triggerUpcomingRecordings_shouldHandleOngoingRecordings() {
        RecordingSchedule recording = createRecording("test.mkv");
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(30);
        LocalDateTime endTime = LocalDateTime.now().plusMinutes(30);
        
        when(scheduleRepository.findByTriggeredFalse()).thenReturn(List.of(recording));
        when(timeUtils.parseStringToLocalDateTime(recording.getStartTime())).thenReturn(startTime);
        when(timeUtils.parseStringToLocalDateTime(recording.getEndTime())).thenReturn(endTime);

        recordingService.triggerUpcomingRecordings();

        verify(recording).setTriggered(true);
        verify(scheduleRepository).saveAll(List.of(recording));
        verify(executorService).submit(any(Runnable.class));
    }

    @Test
    void removeTriggeredRecordings_shouldRemoveExpiredRecordings() {
        RecordingSchedule recording = createRecording("test.mkv");
        LocalDateTime pastEndTime = LocalDateTime.now().minusHours(1);
        
        when(scheduleRepository.findByTriggeredTrue()).thenReturn(List.of(recording));
        when(timeUtils.parseStringToLocalDateTime(recording.getEndTime())).thenReturn(pastEndTime);

        recordingService.removeTriggeredRecordings();

        verify(pastRecordingService).saveRecordingHistory(recording);
        verify(scheduleRepository).deleteAll(List.of(recording));
    }

    @Test
    void removeTriggeredRecordings_shouldKeepActiveRecordings() {
        RecordingSchedule recording = createRecording("test.mkv");
        LocalDateTime futureEndTime = LocalDateTime.now().plusHours(1);
        
        when(scheduleRepository.findByTriggeredTrue()).thenReturn(List.of(recording));
        when(timeUtils.parseStringToLocalDateTime(recording.getEndTime())).thenReturn(futureEndTime);

        recordingService.removeTriggeredRecordings();

        verify(scheduleRepository, never()).deleteAll(any());
    }

    private RecordingSchedule createRecording(String fileName) {
        RecordingSchedule recording = mock(RecordingSchedule.class);
        when(recording.getFileName()).thenReturn(fileName);
        when(recording.getStartTime()).thenReturn("10:00 01/01/2025");
        when(recording.getEndTime()).thenReturn("12:00 01/01/2025");
        return recording;
    }
}