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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.auth.bcrypt-strength=4",
        "app.auth.cookie-name=simonjp_test_token",
        "app.auth.cookie-secure=false",
        "app.auth.cookie-same-site=Lax",
        "app.auth.csrf-cookie-name=XSRF-TOKEN"
})
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    private static final String TOKEN_COOKIE = "simonjp_test_token";
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
        username = "test-" + UUID.randomUUID();
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setNama("Test Admin");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        userRepository.save(user);
        userId = user.getId();
    }

    @AfterEach
    void tearDown() {
        tokenRepository.revokeAll(userId);
    }

    @Test
    void csrfEndpointReturnsTokenAndSetsHttpOnlyCookie() throws Exception {
        mvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(CSRF_COOKIE))
                .andExpect(cookie().httpOnly(CSRF_COOKIE, true))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginSetsHttpOnlyCookieAndDoesNotLeakTokenInBody() throws Exception {
        Csrf csrf = csrf();

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CSRF_HEADER, csrf.token())
                        .cookie(csrf.cookie())
                        .content(loginBody(username)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(TOKEN_COOKIE))
                .andExpect(cookie().httpOnly(TOKEN_COOKIE, true))
                .andExpect(cookie().sameSite(TOKEN_COOKIE, "Lax"))
                .andExpect(cookie().maxAge(TOKEN_COOKIE, 8 * 3600))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        Csrf csrf = csrf();

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CSRF_HEADER, csrf.token())
                        .cookie(csrf.cookie())
                        .content(loginBody(username, "password-salah")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Username atau password salah"));
    }

    @Test
    void loginWithoutCsrfTokenReturns403() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("CSRF token tidak valid"));
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Autentikasi diperlukan"));
    }

    @Test
    void cookieAuthenticatesRequest() throws Exception {
        Cookie token = loginCookie();

        mvc.perform(get("/auth/me").cookie(token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void bearerTokenAuthenticatesWithoutCsrf() throws Exception {
        MvcResult result = mvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiresIn").value(30L * 24 * 3600))
                .andReturn();

        String bearer = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        assertThat(bearer).isNotBlank();

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        mvc.perform(post("/auth/logout").header("Authorization", "Bearer " + bearer))
                .andExpect(status().isNoContent());

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + bearer))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cookieRequestWithoutCsrfTokenReturns403() throws Exception {
        Cookie token = loginCookie();

        mvc.perform(post("/auth/logout").cookie(token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("CSRF token tidak valid"));
    }

    @Test
    void logoutRevokesCookieToken() throws Exception {
        Cookie token = loginCookie();
        mvc.perform(get("/auth/me").cookie(token)).andExpect(status().isOk());

        Csrf csrf = csrf();
        mvc.perform(post("/auth/logout")
                        .cookie(token, csrf.cookie())
                        .header(CSRF_HEADER, csrf.token()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(TOKEN_COOKIE, 0));

        mvc.perform(get("/auth/me").cookie(token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void disabledUserCannotLogin() throws Exception {
        UserAccount disabled = new UserAccount();
        disabled.setUsername("disabled-" + UUID.randomUUID());
        disabled.setPasswordHash(passwordEncoder.encode(PASSWORD));
        disabled.setNama("Disabled");
        disabled.setRole(Role.VIEWER);
        disabled.setEnabled(false);
        userRepository.save(disabled);

        Csrf csrf = csrf();
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CSRF_HEADER, csrf.token())
                        .cookie(csrf.cookie())
                        .content(loginBody(disabled.getUsername())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Akun tidak aktif"));
    }

    private Csrf csrf() throws Exception {
        MvcResult result = mvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");
        Cookie cookie = result.getResponse().getCookie(CSRF_COOKIE);
        assertThat(cookie).isNotNull();
        return new Csrf(token, cookie);
    }

    private Cookie loginCookie() throws Exception {
        Csrf csrf = csrf();
        MvcResult result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CSRF_HEADER, csrf.token())
                        .cookie(csrf.cookie())
                        .content(loginBody(username)))
                .andExpect(status().isOk())
                .andReturn();
        Cookie token = result.getResponse().getCookie(TOKEN_COOKIE);
        assertThat(token).isNotNull();
        return token;
    }

    private static String loginBody(String username) {
        return loginBody(username, PASSWORD);
    }

    private static String loginBody(String username, String password) {
        return """
                {"username":"%s","password":"%s"}""".formatted(username, password);
    }

    private record Csrf(String token, Cookie cookie) {
    }
}
