package id.my.agungdh.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import id.my.agungdh.domain.Pegawai;
import id.my.agungdh.enums.UserRole;
import id.my.agungdh.repository.AppUserRepository;
import id.my.agungdh.repository.PegawaiRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class UserSeedService {

    @Inject
    AppUserRepository appUserRepository;

    @Inject
    AppUserService appUserService;

    @Inject
    PegawaiRepository pegawaiRepository;

    @Inject
    @ConfigProperty(name = "app.seed.enabled", defaultValue = "true")
    boolean seedEnabled;

    @Inject
    @ConfigProperty(name = "app.seed.admin-username", defaultValue = "admin")
    String adminUsername;

    @Inject
    @ConfigProperty(name = "app.seed.admin-password", defaultValue = "admin")
    String adminPassword;

    void onStart(@Observes StartupEvent ev) {
        if (!seedEnabled) {
            return;
        }
        if (appUserRepository.count() > 0) {
            return;
        }
        // Create an admin pegawai if none exists, then a user linked to it.
        Pegawai pegawai = pegawaiRepository.findAll().firstResult();
        java.util.UUID pegawaiUuid = (pegawai == null) ? null : pegawai.getUuid();
        try {
            appUserService.create(adminUsername, adminPassword, UserRole.ADMIN, pegawaiUuid);
        } catch (Exception e) {
            // ignore seeding failures (e.g. missing pegawai)
        }
    }
}
