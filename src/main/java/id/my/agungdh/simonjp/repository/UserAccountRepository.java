package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.domain.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * Pencarian login; memakai expression index {@code lower(username)}.
     * Baris yang sudah soft deleted otomatis tersaring oleh {@code @SQLRestriction}.
     */
    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    /** Akun milik satu pegawai, dipakai saat pegawai dihapus. */
    Optional<UserAccount> findByPegawai(Pegawai pegawai);

    /**
     * Akun beserta pegawai dan bidangnya. Dipakai endpoint {@code /auth/me} karena
     * {@code open-in-view} dimatikan sehingga relasi lazy tidak bisa dimuat di controller.
     */
    @EntityGraph(attributePaths = {"pegawai", "pegawai.bidang"})
    @Query("SELECT u FROM UserAccount u WHERE u.id = :id")
    Optional<UserAccount> findWithRelationsById(@Param("id") Long id);
}
