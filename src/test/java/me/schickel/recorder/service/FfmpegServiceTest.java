package me.schickel.recorder.service;

import me.schickel.recorder.config.RecordingServiceConfig;
import me.schickel.recorder.repository.ScheduleRepository;
import me.schickel.recorder.util.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FfmpegServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private TimeUtils timeUtils;
    @Mock
    private RecordingServiceConfig config;

    private FfmpegService ffmpegService;

    @BeforeEach
    void setUp() {
        ffmpegService = new FfmpegService(scheduleRepository, timeUtils, config);
        when(config.getRecordingFolderPrefix()).thenReturn("/recordings/");
    }

    @Test
    void decideFileName_shouldReturnOriginalName_whenCounterIsOne() throws Exception {
        Method method = FfmpegService.class.getDeclaredMethod("decideFileName", String.class, int.class);
        method.setAccessible(true);

        String result = (String) method.invoke(ffmpegService, "test.mkv", 1);

        assertThat(result).isEqualTo("/recordings/test.mkv");
    }

    @Test
    void decideFileName_shouldAppendCounter_whenCounterIsGreaterThanOne() throws Exception {
        Method method = FfmpegService.class.getDeclaredMethod("decideFileName", String.class, int.class);
        method.setAccessible(true);

        String result = (String) method.invoke(ffmpegService, "test.mkv", 2);

        assertThat(result).isEqualTo("/recordings/test_2.mkv");
    }

    @Test
    void decideFileName_shouldHandleMultipleRetries() throws Exception {
        Method method = FfmpegService.class.getDeclaredMethod("decideFileName", String.class, int.class);
        method.setAccessible(true);

        String result = (String) method.invoke(ffmpegService, "recording.mkv", 5);

        assertThat(result).isEqualTo("/recordings/recording_5.mkv");
    }
}