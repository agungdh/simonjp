package id.my.agungdh.simonjp.profile;

import com.jayway.jsonpath.JsonPath;
import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.PangkatGolongan;
import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.PegawaiStatus;
import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.repository.AuthTokenRepository;
import id.my.agungdh.simonjp.repository.BidangRepository;
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
class ProfileControllerTest {

    private static final String PASSWORD = "rahasia123";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PegawaiRepository pegawaiRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private BidangRepository bidangRepository;

    @Autowired
    private PangkatGolonganRepository pangkatGolonganRepository;

    @Autowired
    private AuthTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String username;
    private Long userId;
    private UUID bidangUuid;
    private UUID pangkatGolonganUuid;
    private String bearer;

    @BeforeEach
    void setUp() throws Exception {
        Bidang bidang = new Bidang();
        bidang.setNama("Bidang Profil " + UUID.randomUUID());
        bidangRepository.save(bidang);
        bidangUuid = bidang.getUuid();

        PangkatGolongan pangkat = new PangkatGolongan();
        pangkat.setJenjang("Jenjang Profil " + UUID.randomUUID());
        pangkat.setPangkat("Pangkat Profil " + UUID.randomUUID());
        pangkat.setGolongan("III");
        pangkat.setRuang("b");
        pangkatGolonganRepository.save(pangkat);
        pangkatGolonganUuid = pangkat.getUuid();

        username = uniqueNip();

        Pegawai pegawai = new Pegawai();
        pegawai.setNama("Profil Test");
        pegawai.setNip(username);
        pegawai.setEmail("profil@example.com");
        pegawai.setJabatan("Auditor Muda");
        pegawai.setStatus(PegawaiStatus.AKTIF);
        pegawai.setBidang(bidang);
        pegawaiRepository.save(pegawai);

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(Role.PEGAWAI);
        user.setPegawai(pegawai);
        pegawai.setAccount(user);
        userAccountRepository.save(user);
        userId = user.getId();

        bearer = login(username, PASSWORD);
    }

    @AfterEach
    void tearDown() {
        tokenRepository.revokeAll(userId);
    }

    @Test
    void getProfileReturnsOwnData() throws Exception {
        mvc.perform(get("/profile").header("Authorization", "Bearer " + bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nip").value(username))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.nama").value("Profil Test"))
                .andExpect(jsonPath("$.role").value("PEGAWAI"));
    }

    @Test
    void updateProfileSyncsUsernameAndCannotChangeRoleOrStatus() throws Exception {
        String newNip = uniqueNip();

        mvc.perform(put("/profile")
                        .header("Authorization", "Bearer " + bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nama": "Nama Baru",
                                  "nip": "%s",
                                  "email": "baru@example.com",
                                  "jabatan": "Auditor Madya",
                                  "peran": "Ketua",
                                  "kategoriJabatan": "STRUKTURAL",
                                  "kategoriKebutuhanJamPelatihan": "PEJABAT",
                                  "bidangUuid": "%s",
                                  "pangkatGolonganUuid": "%s",
                                  "role": "ADMIN",
                                  "status": "NON_AKTIF"
                                }""".formatted(newNip, bidangUuid, pangkatGolonganUuid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nama").value("Nama Baru"))
                .andExpect(jsonPath("$.nip").value(newNip))
                .andExpect(jsonPath("$.username").value(newNip))
                .andExpect(jsonPath("$.role").value("PEGAWAI"))
                .andExpect(jsonPath("$.status").value("AKTIF"));

        // Username ikut berubah, password lama tetap berlaku.
        assertThat(login(newNip, PASSWORD)).isNotBlank();
    }

    @Test
    void updateProfileWithDuplicateNipReturns400() throws Exception {
        Pegawai other = new Pegawai();
        other.setNama("Pegawai Lain");
        other.setNip(uniqueNip());
        other.setStatus(PegawaiStatus.AKTIF);
        pegawaiRepository.save(other);

        mvc.perform(put("/profile")
                        .header("Authorization", "Bearer " + bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nama":"Nama Baru","nip":"%s"}""".formatted(other.getNip())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("NIP sudah digunakan"));
    }

    @Test
    void changePasswordWithWrongCurrentPasswordReturns400() throws Exception {
        mvc.perform(put("/profile/password")
                        .header("Authorization", "Bearer " + bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"salah123","newPassword":"baru12345"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password saat ini salah"));
    }

    @Test
    void changePasswordRevokesSessionsAndUsesNewPassword() throws Exception {
        mvc.perform(put("/profile/password")
                        .header("Authorization", "Bearer " + bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"%s","newPassword":"baru12345"}""".formatted(PASSWORD)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/profile").header("Authorization", "Bearer " + bearer))
                .andExpect(status().isUnauthorized());

        bearer = login(username, "baru12345");
        mvc.perform(get("/profile").header("Authorization", "Bearer " + bearer))
                .andExpect(status().isOk());
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

    private static String uniqueNip() {
        return "NIP" + UUID.randomUUID().toString().replace("-", "").substring(0, 15);
    }
}
