package me.schickel.recorder.mapper;

import me.schickel.recorder.dto.request.ChannelRequest;
import me.schickel.recorder.dto.response.ChannelResponse;
import me.schickel.recorder.entity.ChannelUrl;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChannelMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "channelName", target = "name")
    @Mapping(source = "m3uUrl", target = "url")
    ChannelUrl toEntity(ChannelRequest request);
    
    @Mapping(source = "name", target = "channelName")
    @Mapping(source = "url", target = "m3uUrl")
    ChannelResponse toResponse(ChannelUrl entity);
    
    List<ChannelResponse> toResponseList(List<ChannelUrl> entities);
}
