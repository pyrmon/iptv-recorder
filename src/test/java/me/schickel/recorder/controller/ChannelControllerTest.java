package me.schickel.recorder.controller;

import me.schickel.recorder.dto.request.ChannelRequest;
import me.schickel.recorder.dto.response.ChannelResponse;
import me.schickel.recorder.service.ChannelManagementService;
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
class ChannelControllerTest {

    @Mock
    private ChannelManagementService channelManagementService;

    private ChannelController controller;

    @BeforeEach
    void setUp() {
        controller = new ChannelController(channelManagementService);
    }

    @Test
    void getAllChannels_shouldReturnOkWithChannels() {
        List<ChannelResponse> channels = List.of(
            mock(ChannelResponse.class),
            mock(ChannelResponse.class)
        );
        when(channelManagementService.getChannels()).thenReturn(channels);

        ResponseEntity<List<ChannelResponse>> response = controller.getAllChannels();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        verify(channelManagementService).getChannels();
    }

    @Test
    void addChannel_shouldReturnOk_whenChannelIsValid() {
        ChannelRequest request = mock(ChannelRequest.class);
        when(request.getChannelName()).thenReturn("Test Channel");
        doNothing().when(channelManagementService).createChannelLink(request);

        ResponseEntity<String> response = controller.addChannel(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Added channel Test Channel");
        verify(channelManagementService).createChannelLink(request);
    }

    @Test
    void addChannel_shouldReturnBadRequest_whenExceptionOccurs() {
        ChannelRequest request = mock(ChannelRequest.class);
        when(request.getChannelName()).thenReturn("Test Channel");
        doThrow(new RuntimeException("Database error")).when(channelManagementService).createChannelLink(request);

        ResponseEntity<String> response = controller.addChannel(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Failed to add channel: Database error");
    }

    @Test
    void addChannel_shouldHandleNullChannelName() {
        ChannelRequest request = mock(ChannelRequest.class);
        when(request.getChannelName()).thenReturn(null);
        doNothing().when(channelManagementService).createChannelLink(request);

        ResponseEntity<String> response = controller.addChannel(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Added channel null");
    }

    @Test
    void addChannels_shouldReturnOk_whenChannelsAreValid() {
        ChannelRequest request1 = mock(ChannelRequest.class);
        ChannelRequest request2 = mock(ChannelRequest.class);
        when(request1.getChannelName()).thenReturn("Channel 1");
        when(request2.getChannelName()).thenReturn("Channel 2");
        List<ChannelRequest> requests = List.of(request1, request2);
        
        doNothing().when(channelManagementService).createChannelLinks(requests);

        ResponseEntity<String> response = controller.addChannels(requests);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Added 2 channels");
        verify(channelManagementService).createChannelLinks(requests);
    }

    @Test
    void addChannels_shouldReturnBadRequest_whenExceptionOccurs() {
        List<ChannelRequest> requests = List.of(mock(ChannelRequest.class));
        doThrow(new RuntimeException("Database error")).when(channelManagementService).createChannelLinks(requests);

        ResponseEntity<String> response = controller.addChannels(requests);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Failed to add channels: Database error");
    }

    @Test
    void deleteChannel_shouldReturnOk_whenChannelExists() {
        when(channelManagementService.deleteChannelLink(1L)).thenReturn("Test Channel");

        ResponseEntity<String> response = controller.deleteChannel(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Deleted channel #1 with the name: Test Channel");
        verify(channelManagementService).deleteChannelLink(1L);
    }

    @Test
    void deleteChannel_shouldReturnNotFound_whenChannelDoesNotExist() {
        when(channelManagementService.deleteChannelLink(1L)).thenThrow(new IllegalArgumentException("Channel not found"));

        ResponseEntity<String> response = controller.deleteChannel(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void updateChannel_shouldReturnOk_whenChannelExists() {
        ChannelRequest request = mock(ChannelRequest.class);
        doNothing().when(channelManagementService).updateChannelLink(1L, request);

        ResponseEntity<String> response = controller.updateChannel(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Updated channel 1");
        verify(channelManagementService).updateChannelLink(1L, request);
    }

    @Test
    void updateChannel_shouldReturnNotFound_whenChannelDoesNotExist() {
        ChannelRequest request = mock(ChannelRequest.class);
        doThrow(new IllegalArgumentException("Channel not found")).when(channelManagementService).updateChannelLink(1L, request);

        ResponseEntity<String> response = controller.updateChannel(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }
}