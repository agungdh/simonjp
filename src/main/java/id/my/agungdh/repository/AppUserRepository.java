package id.my.agungdh.repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import id.my.agungdh.domain.AppUser;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppUserRepository implements PanacheRepository<AppUser> {

    public Optional<AppUser> findActiveByUuid(UUID uuid) {
        return find("uuid = :uuid order by id", Map.of("uuid", uuid)).firstResultOptional();
    }

    public Optional<AppUser> findActiveByUsername(String username) {
        return find("username = :username order by id", Map.of("username", username)).firstResultOptional();
    }

    public boolean existsByUsername(String username, Long excludeId) {
        if (excludeId == null) {
            return count("username = ?1", username) > 0;
        }
        return count("username = ?1 and id <> ?2", username, excludeId) > 0;
    }
}
