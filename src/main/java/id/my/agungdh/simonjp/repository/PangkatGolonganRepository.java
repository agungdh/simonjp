package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.PangkatGolongan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PangkatGolonganRepository extends JpaRepository<PangkatGolongan, Long> {

    Optional<PangkatGolongan> findByUuid(UUID uuid);

    /** Pengecekan duplikat; {@code ruang} boleh null sehingga dibandingkan lewat coalesce. */
    @Query("""
            SELECT pg FROM PangkatGolongan pg
            WHERE lower(pg.pangkat) = lower(:pangkat)
              AND lower(pg.golongan) = lower(:golongan)
              AND lower(coalesce(pg.ruang, '')) = lower(coalesce(:ruang, ''))
            """)
    Optional<PangkatGolongan> findDuplicate(@Param("pangkat") String pangkat,
                                            @Param("golongan") String golongan,
                                            @Param("ruang") String ruang);

    @Query("SELECT pg FROM PangkatGolongan pg WHERE (:cursorId IS NULL OR pg.id < :cursorId) ORDER BY pg.id DESC")
    List<PangkatGolongan> findPage(@Param("cursorId") Long cursorId,
                                   org.springframework.data.domain.Pageable pageable);
}
