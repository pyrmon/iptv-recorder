package me.schickel.recorder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("recorder")
public class RecordingServiceConfig {
    private String recordingFolderPrefix;
}
