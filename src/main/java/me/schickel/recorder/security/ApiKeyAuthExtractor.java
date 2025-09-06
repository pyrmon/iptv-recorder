package me.schickel.recorder.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Component;

import javax.annotation.processing.Generated;
import java.util.Optional;

@Generated("Spring Security Component")
@Component
public class ApiKeyAuthExtractor {

    @Value("${application.security.api-key}")
    private String apiKey;

    public Optional<Authentication> extract(HttpServletRequest request) {
        String providedKey = request.getHeader("x-api-key");
        if (providedKey == null || !providedKey.equals(apiKey))
            return Optional.empty();

        ApiKeyAuth auth = new ApiKeyAuth(providedKey, AuthorityUtils.NO_AUTHORITIES);
        auth.setAuthenticated(true); // Set authenticated after validation
        return Optional.of(auth);
    }

}