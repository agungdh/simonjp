package id.my.agungdh.simonjp.auth;

import com.jayway.jsonpath.JsonPath;
import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.repository.AuthTokenRepository;
import id.my.agungdh.simonjp.repository.BidangRepository;
import id.my.agungdh.simonjp.repository.PegawaiRepository;
import id.my.agungdh.simonjp.repository.UserAccountRepository;
import id.my.agungdh.simonjp.service.PegawaiService;
import jakarta.persistence.EntityManager;
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

import java.time.OffsetDateTime;
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
    private PegawaiRepository pegawaiRepository;

    @Autowired
    private BidangRepository bidangRepository;

    @Autowired
    private AuthTokenRepository tokenRepository;

    @Autowired
    private PegawaiService pegawaiService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String username;
    private Long userId;
    private String bidangNama;
    private UUID pegawaiUuid;

    @BeforeEach
    void setUp() {
        username = "test-" + UUID.randomUUID();

        Bidang bidang = new Bidang();
        bidang.setNama("Bidang Test " + UUID.randomUUID());
        bidang.setDeskripsi("Deskripsi bidang test");
        bidangRepository.save(bidang);
        bidangNama = bidang.getNama();

        Pegawai pegawai = new Pegawai();
        pegawai.setNama("Test Admin");
        pegawai.setNip("NIP-" + UUID.randomUUID());
        pegawai.setEmail("test-" + UUID.randomUUID() + "@example.com");
        pegawai.setBidang(bidang);
        pegawaiRepository.save(pegawai);
        pegawaiUuid = pegawai.getUuid();

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(Role.ADMIN);
        user.setPegawai(pegawai);
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
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.pegawai.uuid").value(pegawaiUuid.toString()))
                .andExpect(jsonPath("$.pegawai.nama").value("Test Admin"))
                .andExpect(jsonPath("$.pegawai.bidang.nama").value(bidangNama));
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
    void softDeletedUserCannotLogin() throws Exception {
        UserAccount deleted = new UserAccount();
        deleted.setUsername("deleted-" + UUID.randomUUID());
        deleted.setPasswordHash(passwordEncoder.encode(PASSWORD));
        deleted.setRole(Role.PEGAWAI);
        Pegawai pegawai = new Pegawai();
        pegawai.setNama("Deleted User");
        pegawai.setNip("NIP-" + UUID.randomUUID());
        pegawaiRepository.save(pegawai);
        deleted.setPegawai(pegawai);
        deleted.setDeletedAt(OffsetDateTime.now());
        userRepository.save(deleted);

        Csrf csrf = csrf();
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(CSRF_HEADER, csrf.token())
                        .cookie(csrf.cookie())
                        .content(loginBody(deleted.getUsername())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Username atau password salah"));
    }

    @Test
    void deletingPegawaiSoftDeletesUserAndRevokesTokens() throws Exception {
        Cookie token = loginCookie();
        assertThat(tokenRepository.resolve(token.getValue())).isPresent();

        pegawaiService.delete(pegawaiUuid);
        entityManager.flush();
        entityManager.clear();

        assertThat(userRepository.findByUsernameIgnoreCase(username)).isEmpty();
        assertThat(tokenRepository.resolve(token.getValue())).isEmpty();

        mvc.perform(get("/auth/me").cookie(token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void softDeletedAccountCannotUseExistingToken() throws Exception {
        Cookie token = loginCookie();

        UserAccount user = userRepository.findById(userId).orElseThrow();
        user.setDeletedAt(OffsetDateTime.now());
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Token masih ada di Valkey, tapi akun yang sudah soft deleted harus ditolak.
        assertThat(tokenRepository.resolve(token.getValue())).isPresent();
        mvc.perform(get("/auth/me").cookie(token))
                .andExpect(status().isUnauthorized());
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
