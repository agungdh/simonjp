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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    private static final String USERNAME = "admin-bidang-test";

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

    private String bearer;
    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        userId = createAccount().getId();
        bearer = login();
    }

    @AfterEach
    void tearDown() {
        tokenRepository.revokeAll(userId);
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
                        .header("Authorization", "Bearer " + bearer))
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
                        .header("Authorization", "Bearer " + bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].uuid").value(bidang.getUuid().toString()));
    }

    @Test
    void searchRejectsTooShortTerm() throws Exception {
        mvc.perform(get("/bidang")
                        .param("search", "ab")
                        .header("Authorization", "Bearer " + bearer))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pencarian minimal 3 karakter"));
    }

    private UserAccount createAccount() {
        Pegawai pegawai = new Pegawai();
        pegawai.setNama("Akun Bidang");
        pegawai.setNip("NIP" + UUID.randomUUID().toString().replace("-", "").substring(0, 15));
        pegawai.setStatus(PegawaiStatus.AKTIF);
        pegawaiRepository.save(pegawai);

        UserAccount user = new UserAccount();
        user.setUsername(USERNAME);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(Role.ADMIN);
        user.setPegawai(pegawai);
        pegawai.setAccount(user);
        return userAccountRepository.save(user);
    }

    private String login() throws Exception {
        MvcResult result = mvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}""".formatted(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }
}
