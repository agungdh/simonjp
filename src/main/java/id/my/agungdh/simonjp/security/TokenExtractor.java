package id.my.agungdh.simonjp.security;

import id.my.agungdh.simonjp.config.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.util.Optional;

/**
 * Mengambil token dari header {@code Authorization: Bearer} atau cookie, dengan
 * bearer diprioritaskan. Dipakai oleh filter dan oleh endpoint logout.
 */
@Component
@RequiredArgsConstructor
public class TokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthProperties properties;

    public Optional<String> extract(HttpServletRequest request) {
        String bearer = bearerToken(request);
        if (bearer != null) {
            return Optional.of(bearer);
        }
        Cookie cookie = WebUtils.getCookie(request, properties.cookieName());
        if (cookie != null && cookie.getValue() != null && !cookie.getValue().isBlank()) {
            return Optional.of(cookie.getValue());
        }
        return Optional.empty();
    }

    public boolean isBearer(HttpServletRequest request) {
        return bearerToken(request) != null;
    }

    private static String bearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        String token = header.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }
}
