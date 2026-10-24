package id.my.agungdh.simonjp;

import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.repository.UserAccountRepository;
import id.my.agungdh.simonjp.service.SeedService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Setiap akun wajib punya pegawai, jadi seeder harus membuat pegawai admin lebih dulu.
 */
@SpringBootTest(properties = "app.auth.bcrypt-strength=4")
@Transactional
class SeedServiceTest {

    @Autowired
    private SeedService seedService;

    @Autowired
    private UserAccountRepository userRepository;

    @Test
    void seedsAdminAccountWithPegawai() {
        seedService.seed();

        UserAccount user = userRepository.findByUsernameIgnoreCase("admin").orElseThrow();

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.getPegawai()).isNotNull();
        assertThat(user.getPegawai().getNama()).isEqualTo("Administrator");
    }
}
