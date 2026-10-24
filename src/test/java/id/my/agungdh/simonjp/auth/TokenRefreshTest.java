package id.my.agungdh.simonjp.auth;

import com.jayway.jsonpath.JsonPath;
import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.repository.AuthTokenRepository;
import id.my.agungdh.simonjp.repository.UserAccountRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Memastikan sliding TTL pada cookie ikut mengirim ulang Set-Cookie. Tanpa ini,
 * browser menghapus cookie sesuai Max-Age lama walaupun token di Valkey diperpanjang.
 */
@SpringBootTest(properties = {
        "app.auth.bcrypt-strength=4",
        "app.auth.cookie-name=simonjp_refresh_token",
        "app.auth.cookie-secure=false",
        "app.auth.cookie-same-site=Lax",
        "app.auth.csrf-cookie-name=XSRF-TOKEN",
        "app.auth.web-token-ttl=4s",
        "app.auth.mobile-token-ttl=60s"
})
@AutoConfigureMockMvc
@Transactional
class TokenRefreshTest {

    private static final String TOKEN_COOKIE = "simonjp_refresh_token";
    private static final String CSRF_COOKIE = "XSRF-TOKEN";
    private static final String CSRF_HEADER = "X-XSRF-TOKEN";
    private static final String PASSWORD = "rahasia123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserAccountRepository userRepository;

    @Autowired
    private AuthTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String username;
    private Long userId;

    @BeforeEach
    void setUp() {
        username = "refresh-" + UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setNama("Refresh User");
        user.setRole(Role.VIEWER);
        user.setEnabled(true);
        userRepository.save(user);
        userId = user.getId();
    }

    @AfterEach
    void tearDown() {
        tokenRepository.revokeAll(userId);
    }

    @Test
    void slidingRefreshReissuesTokenCookie() throws Exception {
        Cookie token = loginCookie();

        Thread.sleep(2_200);

        mvc.perform(get("/auth/me").cookie(token))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(TOKEN_COOKIE))
                .andExpect(cookie().maxAge(TOKEN_COOKIE, 4));
    }

    private Cookie loginCookie() throws Exception {
        MvcResult csrfResult = mvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        String csrfToken = JsonPath.read(csrfResult.getResponse().getContentAsString(), "$.token");
        Cookie csrfCookie = csrfResult.getResponse().getCookie(CSRF_COOKIE);
        assertThat(csrfCookie).isNotNull();

        MvcResult login = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CSRF_HEADER, csrfToken)
                        .cookie(csrfCookie)
                        .content("""
                                {"username":"%s","password":"%s"}""".formatted(username, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        Cookie token = login.getResponse().getCookie(TOKEN_COOKIE);
        assertThat(token).isNotNull();
        return token;
    }
}
