package id.my.agungdh.simonjp.auth;

import id.my.agungdh.simonjp.repository.AuthTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthTokenRepositoryTest {

    private static final Long USER_ID = 999_999L;

    @Autowired
    private AuthTokenRepository repository;

    @AfterEach
    void tearDown() {
        repository.revokeAll(USER_ID);
    }

    @Test
    void resolveReturnsUserIdAndRevokeRemovesToken() {
        String token = repository.issue(USER_ID, Duration.ofMinutes(5));

        assertThat(repository.resolve(token)).contains(USER_ID);

        repository.revoke(token);
        assertThat(repository.resolve(token)).isEmpty();
    }

    @Test
    void refreshExtendsTtlOnlyWhenRemainingIsBelowHalf() throws InterruptedException {
        Duration ttl = Duration.ofSeconds(4);
        String token = repository.issue(USER_ID, ttl);

        assertThat(repository.refresh(token, ttl)).isFalse();

        Thread.sleep(2_500);

        assertThat(repository.refresh(token, ttl)).isTrue();
        assertThat(repository.remaining(token)).isGreaterThan(Duration.ofSeconds(3));
    }

    @Test
    void revokeAllRemovesEveryTokenOfUser() {
        String first = repository.issue(USER_ID, Duration.ofMinutes(5));
        String second = repository.issue(USER_ID, Duration.ofMinutes(5));

        repository.revokeAll(USER_ID);

        assertThat(repository.resolve(first)).isEmpty();
        assertThat(repository.resolve(second)).isEmpty();
    }
}
