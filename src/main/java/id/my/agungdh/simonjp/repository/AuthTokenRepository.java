package id.my.agungdh.simonjp.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Menyimpan token opaque di Valkey. Nilai token asli tidak pernah disimpan:
 * key memakai SHA-256 dari token, dan isinya hanya id user (tanpa data user lain).
 */
@Repository
@RequiredArgsConstructor
public class AuthTokenRepository {

    private static final String TOKEN_KEY_PREFIX = "auth:sess:";
    private static final String USER_KEY_PREFIX = "auth:user:";
    private static final int TOKEN_BYTES = 32;
    private static final Duration USER_INDEX_MARGIN = Duration.ofDays(1);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StringRedisTemplate redis;

    /**
     * Menerbitkan token baru untuk {@code userId} dengan TTL tertentu.
     *
     * @return token asli (hanya dikembalikan sekali, tidak pernah disimpan)
     */
    public String issue(Long userId, Duration ttl) {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = hash(token);

        redis.opsForValue().set(tokenKey(hash), userId.toString(), ttl);
        String userKey = userKey(userId);
        redis.opsForSet().add(userKey, hash);
        extendIfShorter(userKey, ttl.plus(USER_INDEX_MARGIN));
        return token;
    }

    /** Mengembalikan id user jika token masih berlaku. */
    public Optional<Long> resolve(String token) {
        String value = redis.opsForValue().get(tokenKey(hash(token)));
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    /**
     * Sliding expiration: TTL diperpanjang hanya jika sisa umur token sudah di bawah 50%,
     * supaya tidak ada write ke Valkey di setiap request.
     *
     * @return {@code true} jika TTL diperpanjang
     */
    public boolean refresh(String token, Duration ttl) {
        String hash = hash(token);
        String key = tokenKey(hash);
        Long remainingMillis = redis.getExpire(key, TimeUnit.MILLISECONDS);
        if (remainingMillis == null || remainingMillis < 0) {
            return false;
        }
        if (remainingMillis > ttl.dividedBy(2).toMillis()) {
            return false;
        }
        redis.expire(key, ttl);
        String userId = redis.opsForValue().get(key);
        if (userId != null) {
            extendIfShorter(userKey(Long.valueOf(userId)), ttl.plus(USER_INDEX_MARGIN));
        }
        return true;
    }

    /** Sisa umur token; {@link Duration#ZERO} jika token sudah tidak ada. */
    public Duration remaining(String token) {
        Long remainingMillis = redis.getExpire(tokenKey(hash(token)), TimeUnit.MILLISECONDS);
        if (remainingMillis == null || remainingMillis < 0) {
            return Duration.ZERO;
        }
        return Duration.ofMillis(remainingMillis);
    }

    /** Mencabut satu token (logout per device). */
    public void revoke(String token) {
        String hash = hash(token);
        String key = tokenKey(hash);
        String userId = redis.opsForValue().get(key);
        if (userId != null) {
            redis.opsForSet().remove(userKey(Long.valueOf(userId)), hash);
        }
        redis.delete(key);
    }

    /** Mencabut seluruh token milik user (logout semua device / akun dinonaktifkan). */
    public void revokeAll(Long userId) {
        String userKey = userKey(userId);
        Set<String> hashes = redis.opsForSet().members(userKey);
        if (hashes != null && !hashes.isEmpty()) {
            List<String> keys = hashes.stream().map(AuthTokenRepository::tokenKey).toList();
            redis.delete(keys);
        }
        redis.delete(userKey);
    }

    private void extendIfShorter(String key, Duration ttl) {
        Long currentSeconds = redis.getExpire(key, TimeUnit.SECONDS);
        if (currentSeconds == null || currentSeconds < 0 || currentSeconds < ttl.toSeconds()) {
            redis.expire(key, ttl);
        }
    }

    private static String tokenKey(String hash) {
        return TOKEN_KEY_PREFIX + hash;
    }

    private static String userKey(Long userId) {
        return USER_KEY_PREFIX + userId;
    }

    private static String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 tidak tersedia", ex);
        }
    }
}
