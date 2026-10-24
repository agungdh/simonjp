package id.my.agungdh.simonjp.config;

import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Membuat admin awal dari environment. Tidak ada password default yang di-hardcode:
 * jika diaktifkan tetapi kredensial kosong, startup digagalkan.
 */
@Component
@ConditionalOnProperty(prefix = "app.auth.bootstrap", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class AuthBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AuthBootstrap.class);

    private final AuthProperties properties;
    private final UserAccountService userAccountService;

    @Override
    public void run(ApplicationArguments args) {
        AuthProperties.Bootstrap bootstrap = properties.bootstrap();
        if (isBlank(bootstrap.username()) || isBlank(bootstrap.password())) {
            throw new IllegalStateException(
                    "app.auth.bootstrap.enabled=true tetapi APP_ADMIN_USERNAME/APP_ADMIN_PASSWORD kosong");
        }
        userAccountService.ensureAdmin(bootstrap.username().trim(), bootstrap.password(),
                isBlank(bootstrap.pegawaiNama()) ? "Administrator" : bootstrap.pegawaiNama(), Role.ADMIN);
        log.info("Bootstrap admin siap untuk username {}", bootstrap.username());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
