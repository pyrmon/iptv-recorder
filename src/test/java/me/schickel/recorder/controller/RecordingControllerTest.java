package me.schickel.recorder.controller;

import me.schickel.recorder.dto.request.RecordingScheduleRequest;
import me.schickel.recorder.dto.response.RecordingScheduleResponse;
import me.schickel.recorder.service.RecordingService;
import me.schickel.recorder.service.ScheduleManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecordingControllerTest {

    @Mock
    private ScheduleManagementService scheduleManagementService;

    @Mock
    private RecordingService recordingService;

    private RecordingController controller;

    @BeforeEach
    void setUp() {
        controller = new RecordingController(scheduleManagementService, recordingService);
    }

    @Test
    void getAllSchedules_shouldReturnOkWithSchedules() {
        List<RecordingScheduleResponse> schedules = List.of(
            mock(RecordingScheduleResponse.class),
            mock(RecordingScheduleResponse.class)
        );
        when(scheduleManagementService.getAllSchedules()).thenReturn(schedules);

        ResponseEntity<List<RecordingScheduleResponse>> response = controller.getAllSchedules();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        verify(scheduleManagementService).getAllSchedules();
    }

    @Test
    void deleteSchedule_shouldReturnOk_whenScheduleExists() {
        doNothing().when(scheduleManagementService).deleteSchedule(1L);

        ResponseEntity<String> response = controller.deleteSchedule(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Schedule deleted successfully!");
        verify(scheduleManagementService).deleteSchedule(1L);
    }

    @Test
    void deleteSchedule_shouldReturnNotFound_whenScheduleDoesNotExist() {
        doThrow(new IllegalArgumentException("Schedule not found")).when(scheduleManagementService).deleteSchedule(1L);

        ResponseEntity<String> response = controller.deleteSchedule(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void deleteSchedule_shouldReturnNotFound_whenEmptyResultDataAccessException() {
        doThrow(new EmptyResultDataAccessException(1)).when(scheduleManagementService).deleteSchedule(1L);

        ResponseEntity<String> response = controller.deleteSchedule(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void patchSchedule_shouldReturnOk_whenScheduleExists() {
        RecordingScheduleRequest request = mock(RecordingScheduleRequest.class);
        doNothing().when(scheduleManagementService).patchSchedule(1L, request);

        ResponseEntity<String> response = controller.patchSchedule(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Schedule patched successfully!");
        verify(scheduleManagementService).patchSchedule(1L, request);
    }

    @Test
    void patchSchedule_shouldReturnNotFound_whenScheduleDoesNotExist() {
        RecordingScheduleRequest request = mock(RecordingScheduleRequest.class);
        doThrow(new IllegalArgumentException("Schedule not found")).when(scheduleManagementService).patchSchedule(1L, request);

        ResponseEntity<String> response = controller.patchSchedule(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void scheduleRecording_shouldReturnOk_whenRequestIsValid() {
        RecordingScheduleRequest request = mock(RecordingScheduleRequest.class);
        when(request.getFileName()).thenReturn("test.mkv");
        doNothing().when(scheduleManagementService).saveSchedule(request);

        ResponseEntity<String> response = controller.scheduleRecording(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Recording scheduled successfully!");
        verify(scheduleManagementService).saveSchedule(request);
    }

    @Test
    void scheduleRecording_shouldReturnBadRequest_whenRequestIsInvalid() {
        RecordingScheduleRequest request = mock(RecordingScheduleRequest.class);
        when(request.getFileName()).thenReturn("test.mkv");
        doThrow(new IllegalArgumentException("Invalid request")).when(scheduleManagementService).saveSchedule(request);

        ResponseEntity<String> response = controller.scheduleRecording(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid request");
    }

    @Test
    void scheduleRecording_shouldHandleNullFileName() {
        RecordingScheduleRequest request = mock(RecordingScheduleRequest.class);
        when(request.getFileName()).thenReturn(null);
        doNothing().when(scheduleManagementService).saveSchedule(request);

        ResponseEntity<String> response = controller.scheduleRecording(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Recording scheduled successfully!");
    }
}