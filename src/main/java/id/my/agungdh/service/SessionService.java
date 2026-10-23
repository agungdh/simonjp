package id.my.agungdh.service;

import java.time.Duration;
import java.util.UUID;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SessionService {

    private static final String PREFIX = "session:";
    private static final int TTL_SECONDS = 86400;

    @Inject
    RedisDataSource redisDataSource;

    public String create(String username) {
        String token = UUID.randomUUID().toString();
        ValueCommands<String, String> commands = redisDataSource.value(String.class);
        commands.setex(PREFIX + token, TTL_SECONDS, username);
        return token;
    }

    public String findUsername(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        ValueCommands<String, String> commands = redisDataSource.value(String.class);
        return commands.get(PREFIX + token);
    }

    public void delete(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        redisDataSource.key().del(PREFIX + token);
    }
}
