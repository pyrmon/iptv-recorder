package me.schickel.recorder.controller;

import me.schickel.recorder.dto.response.PastRecordingResponse;
import me.schickel.recorder.service.PastRecordingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PastRecordingControllerTest {

    @Mock
    private PastRecordingService pastRecordingService;

    private PastRecordingController controller;

    @BeforeEach
    void setUp() {
        controller = new PastRecordingController(pastRecordingService);
    }

    @Test
    void getAllPastRecordings_shouldReturnOkWithRecordings() {
        List<PastRecordingResponse> recordings = List.of(
            mock(PastRecordingResponse.class),
            mock(PastRecordingResponse.class)
        );
        when(pastRecordingService.getAllPastRecordings()).thenReturn(recordings);

        ResponseEntity<List<PastRecordingResponse>> response = controller.getAllPastRecordings();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        verify(pastRecordingService).getAllPastRecordings();
    }

    @Test
    void getPastRecordingsByChannel_shouldReturnOkWithFilteredRecordings() {
        List<PastRecordingResponse> recordings = List.of(mock(PastRecordingResponse.class));
        when(pastRecordingService.getPastRecordingsByChannel("Test Channel")).thenReturn(recordings);

        ResponseEntity<List<PastRecordingResponse>> response = controller.getPastRecordingsByChannel("Test Channel");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(pastRecordingService).getPastRecordingsByChannel("Test Channel");
    }
}