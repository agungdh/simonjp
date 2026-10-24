package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.PegawaiStatus;
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
import java.util.ArrayList;
import java.util.List;

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
     * Membuat akun admin beserta pegawainya. Dipakai seeder profil {@code seed}; setiap akun
     * wajib terhubung ke pegawai, jadi pegawai dibuat lebih dulu. Idempoten terhadap username.
     */
    @Transactional
    public void ensureAdmin(String username, String rawPassword, String pegawaiNama, Role role) {
        if (repository.findByUsernameIgnoreCase(username).isPresent()) {
            return;
        }
        Pegawai pegawai = new Pegawai();
        pegawai.setNama(pegawaiNama);
        pegawai.setNip(username);
        pegawai.setStatus(PegawaiStatus.AKTIF);
        pegawaiRepository.save(pegawai);

        createFor(pegawai, role, rawPassword);
    }

    /**
     * Membuat akun untuk pegawai. Username selalu sama dengan NIP.
     */
    @Transactional
    public UserAccount createFor(Pegawai pegawai, Role role, String rawPassword) {
        UserAccount user = new UserAccount();
        user.setUsername(pegawai.getNip());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setPegawai(pegawai);
        pegawai.setAccount(user);
        return repository.save(user);
    }

    /**
     * Membuat akun untuk banyak pegawai sekaligus (seeder). Hash dihitung sekali supaya
     * seeding tetap cepat walau bcrypt-nya mahal.
     */
    @Transactional
    public void createForAll(List<Pegawai> pegawais, Role role, String rawPassword) {
        String passwordHash = passwordEncoder.encode(rawPassword);
        List<UserAccount> users = new ArrayList<>();
        for (Pegawai pegawai : pegawais) {
            UserAccount user = new UserAccount();
            user.setUsername(pegawai.getNip());
            user.setPasswordHash(passwordHash);
            user.setRole(role);
            user.setPegawai(pegawai);
            pegawai.setAccount(user);
            users.add(user);
        }
        repository.saveAll(users);
    }

    /**
     * Memperbarui akun; {@code rawPassword} null/blank berarti password tidak diubah.
     * Reset password mencabut semua token supaya sesi lama tidak bisa lanjut.
     */
    @Transactional
    public UserAccount updateFor(UserAccount user, String username, Role role, String rawPassword) {
        user.setUsername(username);
        user.setRole(role);
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            tokenRepository.revokeAll(user.getId());
        }
        return repository.save(user);
    }

    /** Ganti password sendiri; semua sesi dicabut sehingga user harus login ulang. */
    @Transactional
    public void changePassword(UserAccount user, String rawPassword) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        repository.save(user);
        tokenRepository.revokeAll(user.getId());
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
