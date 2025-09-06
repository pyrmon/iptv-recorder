package me.schickel.recorder.dto.response;

import lombok.Data;

@Data
public class ChannelResponse {
    private Long id;
    private String channelName;
    private String m3uUrl;
}