package id.my.agungdh.simonjp.pangkatgolongan;

import com.jayway.jsonpath.JsonPath;
import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.PegawaiStatus;
import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.repository.AuthTokenRepository;
import id.my.agungdh.simonjp.repository.PangkatGolonganRepository;
import id.my.agungdh.simonjp.repository.PegawaiRepository;
import id.my.agungdh.simonjp.repository.UserAccountRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.auth.bcrypt-strength=4",
        "app.auth.cookie-secure=false"
})
@AutoConfigureMockMvc
@Transactional
class PangkatGolonganControllerTest {

    private static final String PASSWORD = "rahasia123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PangkatGolonganRepository repository;

    @Autowired
    private PegawaiRepository pegawaiRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private AuthTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminBearer;
    private String pegawaiBearer;
    private Long adminUserId;
    private Long pegawaiUserId;

    @BeforeEach
    void setUp() throws Exception {
        adminUserId = createAccount("admin-pangkat-test", Role.ADMIN).getId();
        pegawaiUserId = createAccount("pegawai-pangkat-test", Role.PEGAWAI).getId();

        adminBearer = login("admin-pangkat-test", PASSWORD);
        pegawaiBearer = login("pegawai-pangkat-test", PASSWORD);
    }

    @AfterEach
    void tearDown() {
        tokenRepository.revokeAll(adminUserId);
        tokenRepository.revokeAll(pegawaiUserId);
    }

    @Test
    void adminCanCrudPangkatGolongan() throws Exception {
        String uuid = create("Ahli Muda", "Penata Muda", "III", "a");

        mvc.perform(get("/pangkat-golongan/" + uuid).header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pangkat").value("Penata Muda"))
                .andExpect(jsonPath("$.golongan").value("III"));

        mvc.perform(get("/pangkat-golongan").header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].uuid").value(uuid));

        mvc.perform(put("/pangkat-golongan/" + uuid)
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Ahli Madya", "Penata", "IV", "b")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jenjang").value("Ahli Madya"))
                .andExpect(jsonPath("$.golongan").value("IV"));

        mvc.perform(delete("/pangkat-golongan/" + uuid).header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isNoContent());

        mvc.perform(get("/pangkat-golongan/" + uuid).header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isNotFound());
        assertThat(repository.findByUuid(UUID.fromString(uuid))).isEmpty();
    }

    @Test
    void duplicateCombinationReturns400() throws Exception {
        String pangkat = "Pangkat Unik " + UUID.randomUUID();
        create("Jenjang", pangkat, "III", "a");

        mvc.perform(post("/pangkat-golongan")
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Jenjang Lain", pangkat, "III", "a")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pangkat/golongan sudah digunakan"));
    }

    @Test
    void nonAdminCannotCreate() throws Exception {
        mvc.perform(post("/pangkat-golongan")
                        .header("Authorization", "Bearer " + pegawaiBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Jenjang", "Pangkat " + UUID.randomUUID(), "III", "a")))
                .andExpect(status().isForbidden());
    }

    private String create(String jenjang, String pangkat, String golongan, String ruang) throws Exception {
        MvcResult result = mvc.perform(post("/pangkat-golongan")
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(jenjang, pangkat, golongan, ruang)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.uuid");
    }

    private static String body(String jenjang, String pangkat, String golongan, String ruang) {
        return """
                {"jenjang":"%s","pangkat":"%s","golongan":"%s","ruang":"%s"}"""
                .formatted(jenjang, pangkat, golongan, ruang);
    }

    private UserAccount createAccount(String username, Role role) {
        Pegawai pegawai = new Pegawai();
        pegawai.setNama("Akun " + role);
        pegawai.setNip("NIP" + UUID.randomUUID().toString().replace("-", "").substring(0, 15));
        pegawai.setStatus(PegawaiStatus.AKTIF);
        pegawaiRepository.save(pegawai);

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        user.setPegawai(pegawai);
        pegawai.setAccount(user);
        return userAccountRepository.save(user);
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}""".formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }
}
