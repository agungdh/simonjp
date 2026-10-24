package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.Kategori;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KategoriRepository extends JpaRepository<Kategori, Long> {

    Optional<Kategori> findByUuid(UUID uuid);

    @Query("SELECT k FROM Kategori k WHERE (:cursorId IS NULL OR k.id < :cursorId) ORDER BY k.id DESC")
    List<Kategori> findPage(@Param("cursorId") Long cursorId, org.springframework.data.domain.Pageable pageable);
}