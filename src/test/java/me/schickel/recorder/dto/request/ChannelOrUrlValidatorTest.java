package me.schickel.recorder.dto.request;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ChannelOrUrlValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private ChannelOrUrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ChannelOrUrlValidator();
    }

    @Test
    void isValid_shouldReturnFalse_whenRequestIsNull() {
        boolean result = validator.isValid(null, context);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_shouldReturnTrue_whenChannelIsProvided() {
        RecordingScheduleRequest request = new RecordingScheduleRequest();
        request.setChannel("test-channel");

        boolean result = validator.isValid(request, context);

        assertThat(result).isTrue();
    }

    @Test
    void isValid_shouldReturnTrue_whenM3uUrlIsProvided() {
        RecordingScheduleRequest request = new RecordingScheduleRequest();
        request.setM3uUrl("http://test.url");

        boolean result = validator.isValid(request, context);

        assertThat(result).isTrue();
    }

    @Test
    void isValid_shouldReturnTrue_whenBothChannelAndM3uUrlAreProvided() {
        RecordingScheduleRequest request = new RecordingScheduleRequest();
        request.setChannel("test-channel");
        request.setM3uUrl("http://test.url");

        boolean result = validator.isValid(request, context);

        assertThat(result).isTrue();
    }

    @Test
    void isValid_shouldReturnFalse_whenBothChannelAndM3uUrlAreNull() {
        RecordingScheduleRequest request = new RecordingScheduleRequest();

        boolean result = validator.isValid(request, context);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenBothChannelAndM3uUrlAreEmpty() {
        RecordingScheduleRequest request = new RecordingScheduleRequest();
        request.setChannel("");
        request.setM3uUrl("");

        boolean result = validator.isValid(request, context);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenChannelIsWhitespaceOnly() {
        RecordingScheduleRequest request = new RecordingScheduleRequest();
        request.setChannel("   ");

        boolean result = validator.isValid(request, context);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_shouldReturnFalse_whenM3uUrlIsWhitespaceOnly() {
        RecordingScheduleRequest request = new RecordingScheduleRequest();
        request.setM3uUrl("   ");

        boolean result = validator.isValid(request, context);

        assertThat(result).isFalse();
    }

    @Test
    void isValid_shouldReturnTrue_whenChannelHasValidValueWithWhitespace() {
        RecordingScheduleRequest request = new RecordingScheduleRequest();
        request.setChannel("  test-channel  ");

        boolean result = validator.isValid(request, context);

        assertThat(result).isTrue();
    }
}