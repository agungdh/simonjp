package id.my.agungdh.simonjp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Konfigurasi autentikasi. Token disimpan di Valkey dan tidak pernah menyimpan data user.
 *
 * @param cookieName      nama cookie token (production: prefix {@code __Host-})
 * @param cookieSecure    set {@code true} di production (wajib HTTPS)
 * @param cookieSameSite  {@code Lax} aman untuk FE dan API dalam satu registrable domain
 * @param csrfCookieName  nama cookie CSRF
 * @param webTokenTtl     sliding TTL untuk session web
 * @param mobileTokenTtl  sliding TTL untuk bearer mobile
 * @param bcryptStrength  cost BCrypt (4 khusus test, 12 untuk production)
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String cookieName,
        boolean cookieSecure,
        String cookieSameSite,
        String csrfCookieName,
        Duration webTokenTtl,
        Duration mobileTokenTtl,
        int bcryptStrength
) {

    public AuthProperties {
        if ("None".equalsIgnoreCase(cookieSameSite) && !cookieSecure) {
            throw new IllegalStateException("SameSite=None mewajibkan app.auth.cookie-secure=true");
        }
        if (cookieName != null && cookieName.startsWith("__Host-") && !cookieSecure) {
            throw new IllegalStateException("Cookie __Host- mewajibkan app.auth.cookie-secure=true");
        }
        if (webTokenTtl == null || mobileTokenTtl == null) {
            throw new IllegalStateException("app.auth.web-token-ttl dan mobile-token-ttl wajib diisi");
        }
    }
}
