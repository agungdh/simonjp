package id.my.agungdh.service;

import java.util.UUID;

import id.my.agungdh.domain.AppUser;
import id.my.agungdh.domain.Pegawai;
import id.my.agungdh.enums.UserRole;
import id.my.agungdh.repository.AppUserRepository;
import id.my.agungdh.repository.PegawaiRepository;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PegawaiRepository pegawaiRepository;

    @Transactional
    public AppUser create(String username, String password, UserRole role, UUID pegawaiUuid) {
        if (appUserRepository.existsByUsername(username, null)) {
            throw new IllegalArgumentException("Username sudah digunakan.");
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setRole(role);
        if (pegawaiUuid != null) {
            Pegawai p = pegawaiRepository.findActiveByUuid(pegawaiUuid)
                    .orElseThrow(() -> new IllegalArgumentException("Pegawai tidak ditemukan."));
            user.setPegawai(p);
        }
        appUserRepository.persist(user);
        return user;
    }

    public AppUser findByUsername(String username) {
        return appUserRepository.findActiveByUsername(username).orElse(null);
    }

    public boolean verifyPassword(AppUser user, String password) {
        if (user == null || password == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, user.getPassword());
        } catch (Exception e) {
            return false;
        }
    }
}
