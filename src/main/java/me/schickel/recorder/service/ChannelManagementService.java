package me.schickel.recorder.service;

import lombok.RequiredArgsConstructor;
import me.schickel.recorder.dto.request.ChannelRequest;
import me.schickel.recorder.dto.response.ChannelResponse;
import me.schickel.recorder.entity.ChannelUrl;
import me.schickel.recorder.mapper.ChannelMapper;
import me.schickel.recorder.repository.ChannelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelManagementService {

    private final ChannelRepository channelRepository;
    private final ChannelMapper channelMapper;

    public void createChannelLink(ChannelRequest request) {
        ChannelUrl entity = channelMapper.toEntity(request);
        channelRepository.save(entity);
    }

    public void createChannelLinks(List<ChannelRequest> requests) {
        List<ChannelUrl> entities = requests.stream()
                .map(channelMapper::toEntity)
                .toList();
        channelRepository.saveAll(entities);
    }

    public List<ChannelResponse> getChannels() {
        List<ChannelUrl> entities = (List<ChannelUrl>) channelRepository.findAll();
        return channelMapper.toResponseList(entities);
    }

    public boolean existsInChannelLinks(String name) {
        return channelRepository.existsByName(name);
    }

    public String getUrlByName(String name) {
        return channelRepository.findByName(name)
            .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + name))
            .getUrl();
    }

    public String deleteChannelLink(Long id) {
        return channelRepository.findById(id)
            .map(channel -> {
                channelRepository.deleteById(id);
                return channel.getName();
            })
            .orElseThrow(() -> new IllegalArgumentException("Channel not found with id: " + id));
    }

    public void updateChannelLink(Long id, ChannelRequest request) {
        ChannelUrl entity = channelMapper.toEntity(request);
        entity.setId(id);
        channelRepository.save(entity);
    }
}