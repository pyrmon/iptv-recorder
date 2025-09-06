package me.schickel.recorder.service;

import me.schickel.recorder.dto.request.ChannelRequest;
import me.schickel.recorder.dto.response.ChannelResponse;
import me.schickel.recorder.entity.ChannelUrl;
import me.schickel.recorder.mapper.ChannelMapper;
import me.schickel.recorder.repository.ChannelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChannelManagementServiceTest {

    @Mock
    private ChannelRepository channelRepository;
    @Mock
    private ChannelMapper channelMapper;

    private ChannelManagementService service;

    @BeforeEach
    void setUp() {
        service = new ChannelManagementService(channelRepository, channelMapper);
    }

    @Test
    void createChannelLink_shouldSaveChannel() {
        ChannelRequest request = createChannelRequest("Test Channel", "http://test.url");
        ChannelUrl entity = createChannelEntity(1L, "Test Channel", "http://test.url");
        
        when(channelMapper.toEntity(request)).thenReturn(entity);

        service.createChannelLink(request);

        verify(channelRepository).save(entity);
    }

    @Test
    void createChannelLinks_shouldSaveAllChannels() {
        List<ChannelRequest> requests = List.of(
            createChannelRequest("Channel 1", "http://test1.url"),
            createChannelRequest("Channel 2", "http://test2.url")
        );
        List<ChannelUrl> entities = List.of(
            createChannelEntity(1L, "Channel 1", "http://test1.url"),
            createChannelEntity(2L, "Channel 2", "http://test2.url")
        );
        
        when(channelMapper.toEntity(any(ChannelRequest.class))).thenReturn(entities.get(0), entities.get(1));

        service.createChannelLinks(requests);

        verify(channelRepository).saveAll(entities);
    }

    @Test
    void getChannels_shouldReturnAllChannels() {
        List<ChannelUrl> entities = List.of(
            createChannelEntity(1L, "Channel 1", "http://test1.url"),
            createChannelEntity(2L, "Channel 2", "http://test2.url")
        );
        List<ChannelResponse> responses = List.of(
            createChannelResponse(1L, "Channel 1", "http://test1.url"),
            createChannelResponse(2L, "Channel 2", "http://test2.url")
        );
        
        when(channelRepository.findAll()).thenReturn(entities);
        when(channelMapper.toResponseList(entities)).thenReturn(responses);

        List<ChannelResponse> result = service.getChannels();

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(responses);
    }

    @Test
    void existsInChannelLinks_shouldReturnTrue_whenChannelExists() {
        when(channelRepository.existsByName("Test Channel")).thenReturn(true);

        boolean result = service.existsInChannelLinks("Test Channel");

        assertThat(result).isTrue();
    }

    @Test
    void existsInChannelLinks_shouldReturnFalse_whenChannelDoesNotExist() {
        when(channelRepository.existsByName("Non-existent Channel")).thenReturn(false);

        boolean result = service.existsInChannelLinks("Non-existent Channel");

        assertThat(result).isFalse();
    }

    @Test
    void getUrlByName_shouldReturnUrl_whenChannelExists() {
        ChannelUrl entity = createChannelEntity(1L, "Test Channel", "http://test.url");
        
        when(channelRepository.findByName("Test Channel")).thenReturn(Optional.of(entity));

        String result = service.getUrlByName("Test Channel");

        assertThat(result).isEqualTo("http://test.url");
    }

    @Test
    void getUrlByName_shouldThrowException_whenChannelDoesNotExist() {
        when(channelRepository.findByName("Non-existent Channel")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUrlByName("Non-existent Channel"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Channel not found: Non-existent Channel");
    }

    @Test
    void deleteChannelLink_shouldDeleteAndReturnName_whenChannelExists() {
        ChannelUrl entity = createChannelEntity(1L, "Test Channel", "http://test.url");
        
        when(channelRepository.findById(1L)).thenReturn(Optional.of(entity));

        String result = service.deleteChannelLink(1L);

        assertThat(result).isEqualTo("Test Channel");
        verify(channelRepository).deleteById(1L);
    }

    @Test
    void deleteChannelLink_shouldThrowException_whenChannelDoesNotExist() {
        when(channelRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteChannelLink(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Channel not found with id: 1");
    }

    @Test
    void updateChannelLink_shouldUpdateChannel() {
        ChannelRequest request = createChannelRequest("Updated Channel", "http://updated.url");
        ChannelUrl entity = createChannelEntity(null, "Updated Channel", "http://updated.url");
        
        when(channelMapper.toEntity(request)).thenReturn(entity);

        service.updateChannelLink(1L, request);

        verify(entity).setId(1L);
        verify(channelRepository).save(entity);
    }

    private ChannelRequest createChannelRequest(String name, String url) {
        return mock(ChannelRequest.class);
    }

    private ChannelUrl createChannelEntity(Long id, String name, String url) {
        ChannelUrl entity = mock(ChannelUrl.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getName()).thenReturn(name);
        when(entity.getUrl()).thenReturn(url);
        return entity;
    }

    private ChannelResponse createChannelResponse(Long id, String name, String url) {
        return mock(ChannelResponse.class);
    }
}