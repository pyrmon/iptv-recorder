package me.schickel.recorder.service;

import me.schickel.recorder.dto.response.PastRecordingResponse;
import me.schickel.recorder.entity.PastRecording;
import me.schickel.recorder.entity.RecordingSchedule;
import me.schickel.recorder.repository.PastRecordingRepository;
import me.schickel.recorder.util.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PastRecordingServiceTest {

    @Mock
    private PastRecordingRepository pastRecordingRepository;
    @Mock
    private TimeUtils timeUtils;

    private PastRecordingService service;

    @BeforeEach
    void setUp() {
        service = new PastRecordingService(pastRecordingRepository, timeUtils);
    }

    @Test
    void saveRecordingHistory_shouldSavePastRecording() {
        RecordingSchedule recording = createRecordingSchedule();
        LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2025, 1, 1, 12, 0);
        
        when(timeUtils.parseStringToLocalDateTime(recording.getStartTime())).thenReturn(startTime);
        when(timeUtils.parseStringToLocalDateTime(recording.getEndTime())).thenReturn(endTime);

        service.saveRecordingHistory(recording);

        verify(pastRecordingRepository).save(any(PastRecording.class));
    }

    @Test
    void getAllPastRecordings_shouldReturnAllRecordings() {
        List<PastRecording> entities = List.of(
            createPastRecording(1L, "Channel 1"),
            createPastRecording(2L, "Channel 2")
        );
        
        when(pastRecordingRepository.findAllByOrderByRecordedAtDesc()).thenReturn(entities);

        List<PastRecordingResponse> result = service.getAllPastRecordings();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getChannelName()).isEqualTo("Channel 1");
        assertThat(result.get(1).getChannelName()).isEqualTo("Channel 2");
    }

    @Test
    void getPastRecordingsByChannel_shouldReturnFilteredRecordings() {
        List<PastRecording> entities = List.of(createPastRecording(1L, "Test Channel"));
        
        when(pastRecordingRepository.findByChannelNameOrderByRecordedAtDesc("Test Channel")).thenReturn(entities);

        List<PastRecordingResponse> result = service.getPastRecordingsByChannel("Test Channel");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChannelName()).isEqualTo("Test Channel");
    }

    private RecordingSchedule createRecordingSchedule() {
        RecordingSchedule recording = mock(RecordingSchedule.class);
        when(recording.getChannel()).thenReturn("Test Channel");
        when(recording.getM3uUrl()).thenReturn("http://test.url");
        when(recording.getFileName()).thenReturn("test.mkv");
        when(recording.getStartTime()).thenReturn("10:00 01/01/2025");
        when(recording.getEndTime()).thenReturn("12:00 01/01/2025");
        when(recording.isTriggered()).thenReturn(true);
        return recording;
    }

    private PastRecording createPastRecording(Long id, String channelName) {
        PastRecording recording = mock(PastRecording.class);
        when(recording.getId()).thenReturn(id);
        when(recording.getChannelName()).thenReturn(channelName);
        when(recording.getM3uUrl()).thenReturn("http://test.url");
        when(recording.getFileName()).thenReturn("test.mkv");
        when(recording.getStartTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 10, 0));
        when(recording.getEndTime()).thenReturn(LocalDateTime.of(2025, 1, 1, 12, 0));
        when(recording.getRecordedAt()).thenReturn(LocalDateTime.of(2025, 1, 1, 12, 0));
        when(recording.isWasTriggered()).thenReturn(true);
        return recording;
    }
}