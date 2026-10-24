package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.domain.UserAccount;
import id.my.agungdh.simonjp.repository.AuthTokenRepository;
import id.my.agungdh.simonjp.repository.PegawaiRepository;
import id.my.agungdh.simonjp.repository.UserAccountRepository;
import id.my.agungdh.simonjp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Operasi tulis untuk akun user. Password selalu di-hash di sini, tidak pernah dari request.
 */
@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserAccountRepository repository;
    private final PegawaiRepository pegawaiRepository;
    private final AuthTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Membuat admin awal beserta pegawainya. Setiap akun wajib terhubung ke pegawai,
     * jadi bootstrap membuat pegawai lebih dulu. Idempoten terhadap username.
     */
    @Transactional
    public void ensureAdmin(String username, String rawPassword, String pegawaiNama, Role role) {
        if (repository.findByUsernameIgnoreCase(username).isPresent()) {
            return;
        }
        Pegawai pegawai = new Pegawai();
        pegawai.setNama(pegawaiNama);
        pegawaiRepository.save(pegawai);

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setPegawai(pegawai);
        repository.save(user);
    }

    /**
     * Soft delete akun milik pegawai dan cabut semua tokennya. Dipanggil saat pegawai
     * dihapus supaya akun tidak bisa dipakai lagi.
     */
    @Transactional
    public void softDeleteByPegawai(Pegawai pegawai) {
        repository.findByPegawai(pegawai).ifPresent(user -> {
            user.setDeletedAt(OffsetDateTime.now());
            CurrentUser.id().ifPresent(user::setDeletedBy);
            repository.save(user);
            tokenRepository.revokeAll(user.getId());
        });
    }
}
