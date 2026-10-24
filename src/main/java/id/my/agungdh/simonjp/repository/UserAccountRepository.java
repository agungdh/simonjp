package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * Pencarian login; memakai expression index {@code lower(username)}.
     * Baris yang sudah soft deleted otomatis tersaring oleh {@code @SQLRestriction}.
     */
    Optional<UserAccount> findByUsernameIgnoreCase(String username);
}
