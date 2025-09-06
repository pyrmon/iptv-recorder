package me.schickel.recorder.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MiscUtilsTest {

    private MiscUtils miscUtils;

    @BeforeEach
    void setUp() {
        miscUtils = new MiscUtils();
    }

    @Test
    void isValidUrl_shouldReturnTrue_whenUrlIsValid() {
        String validUrl = "http://example.com";
        
        boolean result = miscUtils.isValidUrl(validUrl);
        
        assertThat(result).isTrue();
    }

    @Test
    void isValidUrl_shouldReturnFalse_whenUrlIsNull() {
        boolean result = miscUtils.isValidUrl(null);
        
        assertThat(result).isFalse();
    }

    @Test
    void isValidUrl_shouldReturnFalse_whenUrlIsInvalid() {
        String invalidUrl = "not a url at all";
        
        boolean result = miscUtils.isValidUrl(invalidUrl);
        
        assertThat(result).isFalse();
    }

    @Test
    void isValidUrl_shouldReturnFalse_whenUrlIsNotAbsolute() {
        String relativeUrl = "example.com";
        
        boolean result = miscUtils.isValidUrl(relativeUrl);
        
        assertThat(result).isFalse();
    }

    @Test
    void isValidUrl_shouldReturnFalse_whenUrlThrowsException() {
        String invalidUrl = "http://example.com\n";
        
        boolean result = miscUtils.isValidUrl(invalidUrl);
        
        assertThat(result).isFalse();
    }
}