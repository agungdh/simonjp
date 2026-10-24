package id.my.agungdh.simonjp.security;

import id.my.agungdh.simonjp.config.AuthProperties;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Cookie auth dibangun manual karena aplikasi memakai {@code SessionCreationPolicy.STATELESS}
 * (tidak ada JSESSIONID). Cookie selalu host-only: atribut {@code Domain} tidak di-set.
 */
@Component
@RequiredArgsConstructor
public class AuthCookieFactory {

    private final AuthProperties properties;

    public void addToken(HttpServletResponse response, String token, Duration ttl) {
        response.addHeader(HttpHeaders.SET_COOKIE, build(properties.cookieName(), token, ttl));
    }

    public void clearToken(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, build(properties.cookieName(), "", Duration.ZERO));
    }

    public void clearCsrf(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, build(properties.csrfCookieName(), "", Duration.ZERO));
    }

    private String build(String name, String value, Duration ttl) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite(properties.cookieSameSite())
                .path("/")
                .maxAge(ttl)
                .build()
                .toString();
    }
}
