package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.Pegawai;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PegawaiRepository extends JpaRepository<Pegawai, Long> {

    Optional<Pegawai> findByUuid(UUID uuid);

    Optional<Pegawai> findByNipIgnoreCase(String nip);

    boolean existsByNipIgnoreCase(String nip);

    /**
     * Pegawai beserta relasi yang dipakai response. Dipakai detail, update, dan profil
     * karena {@code open-in-view} dimatikan.
     */
    @EntityGraph(attributePaths = {"bidang", "pangkatGolongan", "account"})
    @Query("SELECT p FROM Pegawai p WHERE p.uuid = :uuid")
    Optional<Pegawai> findWithRelationsByUuid(@Param("uuid") UUID uuid);

    /** Pegawai milik akun yang sedang login; dipakai endpoint {@code /profile}. */
    @EntityGraph(attributePaths = {"bidang", "pangkatGolongan", "account"})
    Optional<Pegawai> findByAccountId(Long accountId);

    @EntityGraph(attributePaths = {"bidang", "pangkatGolongan", "account"})
    @Query("SELECT p FROM Pegawai p WHERE (:cursorId IS NULL OR p.id < :cursorId) ORDER BY p.id DESC")
    List<Pegawai> findPage(@Param("cursorId") Long cursorId, org.springframework.data.domain.Pageable pageable);
}
