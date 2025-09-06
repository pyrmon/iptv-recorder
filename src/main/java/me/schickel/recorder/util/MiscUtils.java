package me.schickel.recorder.util;

import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class MiscUtils {

    public boolean isValidUrl(String url) {
        if (url == null) return false;
        try {
            URI uri = URI.create(url);
            return uri.isAbsolute();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
