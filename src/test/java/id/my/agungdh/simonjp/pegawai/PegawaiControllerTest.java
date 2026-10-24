package id.my.agungdh.simonjp.pegawai;

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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
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
class PegawaiControllerTest {

    private static final String ADMIN_USERNAME = "admin-pegawai-test";
    private static final String PEGAWAI_USERNAME = "pegawai-pegawai-test";
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

    private String adminBearer;
    private String pegawaiBearer;
    private Long adminUserId;
    private Long pegawaiUserId;
    private UUID bidangUuid;
    private UUID pangkatGolonganUuid;

    @BeforeEach
    void setUp() throws Exception {
        Bidang bidang = new Bidang();
        bidang.setNama("Bidang Test " + UUID.randomUUID());
        bidangRepository.save(bidang);
        bidangUuid = bidang.getUuid();

        PangkatGolongan pangkat = new PangkatGolongan();
        pangkat.setJenjang("Jenjang Test " + UUID.randomUUID());
        pangkat.setPangkat("Pangkat Test " + UUID.randomUUID());
        pangkat.setGolongan("III");
        pangkat.setRuang("a");
        pangkatGolonganRepository.save(pangkat);
        pangkatGolonganUuid = pangkat.getUuid();

        adminUserId = createAccount(ADMIN_USERNAME, "Admin Test", Role.ADMIN).getId();
        pegawaiUserId = createAccount(PEGAWAI_USERNAME, "Pegawai Test", Role.PEGAWAI).getId();

        adminBearer = login(ADMIN_USERNAME, PASSWORD);
        pegawaiBearer = login(PEGAWAI_USERNAME, PASSWORD);
    }

    @AfterEach
    void tearDown() {
        tokenRepository.revokeAll(adminUserId);
        tokenRepository.revokeAll(pegawaiUserId);
    }

    @Test
    void createPegawaiCreatesAccountWithNipAsUsername() throws Exception {
        String nip = uniqueNip();

        mvc.perform(post("/pegawai")
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Pegawai Baru", nip, Role.PEGAWAI, PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nip").value(nip))
                .andExpect(jsonPath("$.username").value(nip))
                .andExpect(jsonPath("$.role").value("PEGAWAI"))
                .andExpect(jsonPath("$.status").value("AKTIF"))
                .andExpect(jsonPath("$.jabatan").value("Auditor"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        // Akun hasil create bisa langsung dipakai login.
        assertThat(login(nip, PASSWORD)).isNotBlank();
        assertThat(userAccountRepository.findByUsernameIgnoreCase(nip)).isPresent();
    }

    @Test
    void createWithoutPasswordUsesNip() throws Exception {
        String nip = uniqueNip();

        mvc.perform(post("/pegawai")
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Tanpa Password", nip, Role.PEGAWAI, null)))
                .andExpect(status().isCreated());

        assertThat(login(nip, nip)).isNotBlank();
    }

    @Test
    void createWithDuplicateNipReturns400() throws Exception {
        String nip = uniqueNip();
        mvc.perform(post("/pegawai")
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Pertama", nip, Role.PEGAWAI, PASSWORD)))
                .andExpect(status().isCreated());

        mvc.perform(post("/pegawai")
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Kedua", nip, Role.PEGAWAI, PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("NIP sudah digunakan"));
    }

    @Test
    void nonAdminCannotCreatePegawai() throws Exception {
        mvc.perform(post("/pegawai")
                        .header("Authorization", "Bearer " + pegawaiBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Ditolak", uniqueNip(), Role.PEGAWAI, PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Akses ditolak"));
    }

    @Test
    void updateSyncsUsernameWithNipAndKeepsPasswordWhenBlank() throws Exception {
        String nip = uniqueNip();
        String uuid = create(nip, PASSWORD);
        String newNip = uniqueNip();

        mvc.perform(put("/pegawai/" + uuid)
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Nama Diubah", newNip, Role.PEGAWAI, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nip").value(newNip))
                .andExpect(jsonPath("$.username").value(newNip));

        // Password lama tetap berlaku setelah NIP (username) berubah.
        assertThat(login(newNip, PASSWORD)).isNotBlank();
    }

    @Test
    void resetPasswordRevokesExistingTokens() throws Exception {
        String nip = uniqueNip();
        String uuid = create(nip, PASSWORD);
        String pegawaiToken = login(nip, PASSWORD);

        mvc.perform(put("/pegawai/" + uuid)
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Reset Password", nip, Role.PEGAWAI, "baru12345")))
                .andExpect(status().isOk());

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + pegawaiToken))
                .andExpect(status().isUnauthorized());
        assertThat(login(nip, "baru12345")).isNotBlank();
    }

    @Test
    void updateRoleTakesEffectOnNextRequest() throws Exception {
        String nip = uniqueNip();
        String uuid = create(nip, PASSWORD);
        String pegawaiToken = login(nip, PASSWORD);

        mvc.perform(put("/pegawai/" + uuid)
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Naik Role", nip, Role.ADMIN, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + pegawaiToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deleteSoftDeletesPegawaiAndAccountAndRevokesTokens() throws Exception {
        String nip = uniqueNip();
        String uuid = create(nip, PASSWORD);
        String pegawaiToken = login(nip, PASSWORD);

        mvc.perform(delete("/pegawai/" + uuid)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isNoContent());

        mvc.perform(get("/pegawai/" + uuid)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isNotFound());
        mvc.perform(get("/auth/me").header("Authorization", "Bearer " + pegawaiToken))
                .andExpect(status().isUnauthorized());
        assertThat(userAccountRepository.findByUsernameIgnoreCase(nip)).isEmpty();
    }

    @Test
    void adminCannotDeleteOwnAccount() throws Exception {
        UUID adminPegawaiUuid = userAccountRepository.findById(adminUserId)
                .orElseThrow()
                .getPegawai()
                .getUuid();

        mvc.perform(delete("/pegawai/" + adminPegawaiUuid)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tidak dapat menghapus akun sendiri"));
    }

    @Test
    void listReturnsAccountFields() throws Exception {
        String nip = uniqueNip();
        create(nip, PASSWORD);

        mvc.perform(get("/pegawai").header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].username").exists())
                .andExpect(jsonPath("$.items[0].role").exists());
    }

    @Test
    void searchMatchesPartialNameCaseInsensitive() throws Exception {
        String token = "Zulkifli" + UUID.randomUUID().toString().substring(0, 8);
        String uuid = create(token + " Pratama", uniqueNip(), PASSWORD);

        mvc.perform(get("/pegawai")
                        .param("search", token.substring(0, 10).toUpperCase())
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].uuid", hasItem(uuid)));
    }

    @Test
    void searchMatchesNipAndEmail() throws Exception {
        String nip = uniqueNip();
        String uuid = create(nip, PASSWORD);

        mvc.perform(get("/pegawai")
                        .param("search", nip.substring(3, 10))
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].uuid", hasItem(uuid)));

        mvc.perform(get("/pegawai")
                        .param("search", nip.toLowerCase() + "@example")
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].uuid", hasItem(uuid)));
    }

    @Test
    void searchRejectsTermsOutsideLengthLimits() throws Exception {
        mvc.perform(get("/pegawai")
                        .param("search", "ab")
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pencarian minimal 3 karakter"));

        mvc.perform(get("/pegawai")
                        .param("search", "a".repeat(101))
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pencarian maksimal 100 karakter"));
    }

    @Test
    void searchTreatsWildcardAsLiteral() throws Exception {
        String token = "a%b" + UUID.randomUUID().toString().substring(0, 6);
        String uuid = create(token, uniqueNip(), PASSWORD);

        mvc.perform(get("/pegawai")
                        .param("search", token)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].uuid").value(uuid));
    }

    @Test
    void searchCombinesWithStructuredFilters() throws Exception {
        String token = "Filter" + UUID.randomUUID().toString().substring(0, 8);
        String uuid = create(token, uniqueNip(), PASSWORD);

        mvc.perform(get("/pegawai")
                        .param("search", token)
                        .param("status", "AKTIF")
                        .param("bidangUuid", bidangUuid.toString())
                        .param("kategoriJabatan", "FUNGSIONAL_AUDITOR")
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].uuid", hasItem(uuid)));

        mvc.perform(get("/pegawai")
                        .param("search", token)
                        .param("status", "NON_AKTIF")
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void searchDoesNotReturnSoftDeletedPegawai() throws Exception {
        String token = "Terhapus" + UUID.randomUUID().toString().substring(0, 8);
        String uuid = create(token, uniqueNip(), PASSWORD);

        mvc.perform(delete("/pegawai/" + uuid)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isNoContent());

        mvc.perform(get("/pegawai")
                        .param("search", token)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void searchMatchesJoinedBidangAndKeepsBidangFilter() throws Exception {
        String token = "BidangSearch" + UUID.randomUUID().toString().substring(0, 8);
        Bidang bidang = new Bidang();
        bidang.setNama(token + " Test");
        bidang.setDeskripsi("Deskripsi " + token);
        bidangRepository.save(bidang);

        Pegawai pegawai = new Pegawai();
        pegawai.setNama("Pegawai Tanpa Token");
        pegawai.setNip(uniqueNip());
        pegawai.setStatus(PegawaiStatus.AKTIF);
        pegawai.setBidang(bidang);
        pegawaiRepository.save(pegawai);

        mvc.perform(get("/pegawai")
                        .param("search", token)
                        .param("bidangUuid", bidang.getUuid().toString())
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].uuid", hasItem(pegawai.getUuid().toString())));

        mvc.perform(get("/pegawai")
                        .param("search", token)
                        .param("bidangUuid", bidangUuid.toString())
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void searchMatchesJoinedPangkatGolongan() throws Exception {
        String token = "PangkatSearch" + UUID.randomUUID().toString().substring(0, 8);
        PangkatGolongan pangkat = new PangkatGolongan();
        pangkat.setJenjang("Jenjang " + token);
        pangkat.setPangkat("Pangkat " + UUID.randomUUID());
        pangkat.setGolongan("IV");
        pangkat.setRuang("b");
        pangkatGolonganRepository.save(pangkat);

        Pegawai pegawai = new Pegawai();
        pegawai.setNama("Pegawai Tanpa Token");
        pegawai.setNip(uniqueNip());
        pegawai.setStatus(PegawaiStatus.AKTIF);
        pegawai.setPangkatGolongan(pangkat);
        pegawaiRepository.save(pegawai);

        mvc.perform(get("/pegawai")
                        .param("search", token)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].uuid", hasItem(pegawai.getUuid().toString())));
    }

    @Test
    void searchMatchesPegawaiWithoutBidangOrPangkat() throws Exception {
        String token = "Yatim" + UUID.randomUUID().toString().substring(0, 8);
        Pegawai pegawai = new Pegawai();
        pegawai.setNama("Pegawai Tanpa Relasi");
        pegawai.setNip(uniqueNip());
        pegawai.setPeran(token);
        pegawai.setStatus(PegawaiStatus.AKTIF);
        pegawaiRepository.save(pegawai);

        // LEFT JOIN: relasi NULL tidak menghalangi pencocokan lewat kolom pegawai sendiri.
        mvc.perform(get("/pegawai")
                        .param("search", token)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].uuid", hasItem(pegawai.getUuid().toString())));
    }

    @Test
    void cursorMustMatchActiveFilter() throws Exception {
        String uuidA = create("Cursor A " + UUID.randomUUID(), uniqueNip(), PASSWORD);
        create("Cursor B " + UUID.randomUUID(), uniqueNip(), PASSWORD);

        mvc.perform(get("/pegawai")
                        .param("search", "Cursor B")
                        .param("cursor", uuidA)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cursor tidak valid"));
    }

    @Test
    void cursorWithinSameFilterReturnsNextPage() throws Exception {
        String token = "PageToken" + UUID.randomUUID().toString().substring(0, 6);
        List<UUID> uuids = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            Pegawai pegawai = new Pegawai();
            pegawai.setNama(token + " " + i);
            pegawai.setNip(uniqueNip());
            pegawai.setStatus(PegawaiStatus.AKTIF);
            pegawaiRepository.save(pegawai);
            uuids.add(pegawai.getUuid());
        }

        String firstPage = mvc.perform(get("/pegawai")
                        .param("search", token)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(20))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andReturn().getResponse().getContentAsString();

        String nextCursor = JsonPath.read(firstPage, "$.nextCursor");
        mvc.perform(get("/pegawai")
                        .param("search", token)
                        .param("cursor", nextCursor)
                        .header("Authorization", "Bearer " + adminBearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.items[0].uuid").value(uuids.get(0).toString()));
    }

    private String create(String nip, String password) throws Exception {
        return create("Pegawai " + nip, nip, password);
    }

    private String create(String nama, String nip, String password) throws Exception {
        MvcResult result = mvc.perform(post("/pegawai")
                        .header("Authorization", "Bearer " + adminBearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(nama, nip, Role.PEGAWAI, password)))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.uuid");
    }

    private String body(String nama, String nip, Role role, String password) {
        String passwordField = password == null ? "" : ",\"password\":\"%s\"".formatted(password);
        return """
                {
                  "nama": "%s",
                  "nip": "%s",
                  "email": "%s@example.com",
                  "jabatan": "Auditor",
                  "peran": "Anggota",
                  "status": "AKTIF",
                  "kategoriJabatan": "FUNGSIONAL_AUDITOR",
                  "kategoriKebutuhanJamPelatihan": "AUDITOR",
                  "bidangUuid": "%s",
                  "pangkatGolonganUuid": "%s",
                  "role": "%s"%s
                }""".formatted(nama, nip, nip.toLowerCase(), bidangUuid, pangkatGolonganUuid,
                role.name(), passwordField);
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

    private UserAccount createAccount(String username, String nama, Role role) {
        Pegawai pegawai = new Pegawai();
        pegawai.setNama(nama);
        pegawai.setNip(uniqueNip());
        pegawai.setEmail(username + "@example.com");
        pegawai.setStatus(PegawaiStatus.AKTIF);
        pegawai.setBidang(bidangRepository.findByUuid(bidangUuid).orElseThrow());
        pegawaiRepository.save(pegawai);

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        user.setPegawai(pegawai);
        pegawai.setAccount(user);
        return userAccountRepository.save(user);
    }

    private static String uniqueNip() {
        return "NIP" + UUID.randomUUID().toString().replace("-", "").substring(0, 15);
    }
}
