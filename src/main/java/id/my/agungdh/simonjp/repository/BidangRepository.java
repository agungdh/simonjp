package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.Bidang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BidangRepository extends JpaRepository<Bidang, Long> {

    Optional<Bidang> findByUuid(UUID uuid);

    Optional<Bidang> findByNamaIgnoreCase(String nama);

    @Query("SELECT b FROM Bidang b WHERE (:cursorId IS NULL OR b.id < :cursorId) ORDER BY b.id DESC")
    List<Bidang> findPage(@Param("cursorId") Long cursorId, org.springframework.data.domain.Pageable pageable);

    /**
     * Pencarian substring case-insensitive. Pattern sudah di-escape oleh service dan memakai
     * escape char {@code '!'}.
     */
    @Query("""
            SELECT b FROM Bidang b
            WHERE (:cursorId IS NULL OR b.id < :cursorId)
              AND (lower(b.nama) LIKE :pattern ESCAPE '!'
                   OR lower(b.deskripsi) LIKE :pattern ESCAPE '!')
            ORDER BY b.id DESC
            """)
    List<Bidang> searchPage(@Param("pattern") String pattern,
                            @Param("cursorId") Long cursorId,
                            org.springframework.data.domain.Pageable pageable);
}
