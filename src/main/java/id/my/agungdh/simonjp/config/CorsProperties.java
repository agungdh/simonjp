package id.my.agungdh.simonjp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Origin FE yang diizinkan. Wajib eksplisit (bukan {@code *}) karena request membawa cookie.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
