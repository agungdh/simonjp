package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operasi tulis untuk akun user. Password selalu di-hash di sini, tidak pernah dari request.
 */
@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Membuat user jika username belum ada. Dipakai bootstrap admin dari environment.
     */
    @Transactional
    public void ensureUser(String username, String rawPassword, String nama, Role role) {
        if (repository.findByUsernameIgnoreCase(username).isPresent()) {
            return;
        }
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setNama(nama);
        user.setRole(role);
        user.setEnabled(true);
        repository.save(user);
    }
}
