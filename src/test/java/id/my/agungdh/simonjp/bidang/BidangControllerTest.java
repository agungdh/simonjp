package id.my.agungdh.simonjp.bidang;

import com.jayway.jsonpath.JsonPath;
import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.PegawaiStatus;
import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.repository.AuthTokenRepository;
import id.my.agungdh.simonjp.repository.BidangRepository;
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
class BidangControllerTest {

    private static final String PASSWORD = "rahasia123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BidangRepository bidangRepository;

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
        adminUserId = createAccount("admin-bidang-test", Role.ADMIN).getId();
        pegawaiUserId = createAccount("pegawai-bidang-test", Role.PEGAWAI).getId();

        adminBearer = login("admin-bidang-test");
        pegawaiBearer = login("pegawai-bidang-test");
    }

    @AfterEach
    void tearDown() {
        tokenRepository.revokeAll(adminUserId);
        tokenRepository.revokeAll(pegawaiUserId);
    }

    @Test
    void adminCanCrudBidang() throws Exception {
        String nama = "Bidang " + UUID.randomUUID();
        String uuid = create(nama, "Deskripsi awal");

        mvc.perform(get("/bidang/" + uuid).header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama").value(nama))
                .andExpect(jsonPath("$.deskripsi").value("Deskripsi awal"));

        mvc.perform(get("/bidang").header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].uuid").value(uuid));

        mvc.perform(put("/bidang/" + uuid)
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(nama, "Deskripsi baru")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deskripsi").value("Deskripsi baru"));

        mvc.perform(delete("/bidang/" + uuid).header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isNoContent());

        mvc.perform(get("/bidang/" + uuid).header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isNotFound());
        assertThat(bidangRepository.findByUuid(UUID.fromString(uuid))).isEmpty();
    }

    @Test
    void nonAdminCannotCreate() throws Exception {
        mvc.perform(post("/bidang")
                        .header("Authorization", "Bearer " + pegawaiBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Bidang " + UUID.randomUUID(), "Deskripsi")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Akses ditolak"));
    }

    @Test
    void nonAdminCannotUpdate() throws Exception {
        String nama = "Bidang " + UUID.randomUUID();
        String uuid = create(nama, "Deskripsi awal");

        mvc.perform(put("/bidang/" + uuid)
                        .header("Authorization", "Bearer " + pegawaiBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(nama, "Diubah pegawai")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Akses ditolak"));

        Bidang bidang = bidangRepository.findByUuid(UUID.fromString(uuid)).orElseThrow();
        assertThat(bidang.getDeskripsi()).isEqualTo("Deskripsi awal");
    }

    @Test
    void nonAdminCannotDelete() throws Exception {
        String uuid = create("Bidang " + UUID.randomUUID(), "Deskripsi awal");

        mvc.perform(delete("/bidang/" + uuid).header("Authorization", "Bearer " + pegawaiBearer))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Akses ditolak"));

        assertThat(bidangRepository.findByUuid(UUID.fromString(uuid))).isPresent();
    }

    @Test
    void pegawaiCanReadListAndGet() throws Exception {
        String nama = "Bidang " + UUID.randomUUID();
        String uuid = create(nama, "Deskripsi awal");

        mvc.perform(get("/bidang").header("Authorization", "Bearer " + pegawaiBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].uuid").value(uuid));

        mvc.perform(get("/bidang/" + uuid).header("Authorization", "Bearer " + pegawaiBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama").value(nama));
    }

    @Test
    void searchMatchesPartialNamaCaseInsensitive() throws Exception {
        String token = "Bidang" + UUID.randomUUID().toString().substring(0, 8);
        Bidang bidang = new Bidang();
        bidang.setNama(token + " Test");
        bidang.setDeskripsi("Deskripsi " + token);
        bidangRepository.save(bidang);

        mvc.perform(get("/bidang")
                        .param("search", token.substring(0, 10).toUpperCase())
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].uuid").value(bidang.getUuid().toString()));
    }

    @Test
    void searchMatchesDeskripsi() throws Exception {
        String token = "Keterangan" + UUID.randomUUID().toString().substring(0, 8);
        Bidang bidang = new Bidang();
        bidang.setNama("Bidang " + UUID.randomUUID());
        bidang.setDeskripsi(token + " khusus");
        bidangRepository.save(bidang);

        mvc.perform(get("/bidang")
                        .param("search", token)
                        .header("Authorization", "Bearer " + pegawaiBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].uuid").value(bidang.getUuid().toString()));
    }

    @Test
    void searchRejectsTooShortTerm() throws Exception {
        mvc.perform(get("/bidang")
                        .param("search", "ab")
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pencarian minimal 3 karakter"));
    }

    private String create(String nama, String deskripsi) throws Exception {
        MvcResult result = mvc.perform(post("/bidang")
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(nama, deskripsi)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.uuid");
    }

    private static String body(String nama, String deskripsi) {
        return """
                {"nama":"%s","deskripsi":"%s"}"""
                .formatted(nama, deskripsi);
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

    private String login(String username) throws Exception {
        MvcResult result = mvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}""".formatted(username, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }
}
