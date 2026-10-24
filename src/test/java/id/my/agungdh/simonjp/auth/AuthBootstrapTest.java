package id.my.agungdh.simonjp.auth;

import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Setiap akun wajib punya pegawai, jadi bootstrap admin harus membuat pegawai lebih dulu.
 */
@SpringBootTest(properties = {
        "app.auth.bcrypt-strength=4",
        "app.auth.bootstrap.enabled=true",
        "app.auth.bootstrap.username=bootstrap-admin-test",
        "app.auth.bootstrap.password=rahasia123",
        "app.auth.bootstrap.pegawai-nama=Admin Bootstrap"
})
@Transactional
class AuthBootstrapTest {

    @Autowired
    private UserAccountRepository userRepository;

    @Test
    void bootstrapCreatesAdminWithPegawai() {
        UserAccount user = userRepository.findByUsernameIgnoreCase("bootstrap-admin-test").orElseThrow();

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getPegawai()).isNotNull();
        assertThat(user.getPegawai().getNama()).isEqualTo("Admin Bootstrap");
    }
}
