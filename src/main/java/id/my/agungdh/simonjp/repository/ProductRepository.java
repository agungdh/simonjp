package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByUuid(UUID uuid);

    @Query("SELECT p FROM Product p WHERE (:cursorId IS NULL OR p.id < :cursorId) ORDER BY p.id DESC")
    List<Product> findPage(@Param("cursorId") Long cursorId, org.springframework.data.domain.Pageable pageable);
}