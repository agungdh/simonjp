package id.my.agungdh.simonjp.config;

import id.my.agungdh.simonjp.security.CurrentUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Mengisi {@code created_by}/{@code updated_by} dari principal yang sedang login.
 * Domain hanya memakai anotasi Spring Data, jadi arah dependensi tetap aman.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditConfig {

    @Bean
    AuditorAware<Long> auditorAware() {
        return CurrentUser::id;
    }
}
