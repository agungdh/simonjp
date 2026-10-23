package id.my.agungdh.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Mengisi kolom audit secara server-side di satu tempat. Kolom {@code *_at} (timestamp) diisi
 * bila diketahui; kolom {@code *_by} hanya diisi dari principal yang sedang login. Bila tidak
 * ada principal (auth belum ada / tidak diketahui), kolom {@code *_by} dibiarkan {@code null}
 * ("unknown") — tidak pernah dibuat nilai palsu seperti {@code 0L}.
 */
public class AuditListener {

    @PrePersist
    void prePersist(BaseEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();

        if (entity.getUuid() == null) {
            entity.setUuid(UUID.randomUUID());
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        // createdBy: null bila tidak ada principal
        entity.setUpdatedAt(now);
        // updatedBy: null bila tidak ada principal
    }

    @PreUpdate
    void preUpdate(BaseEntity entity) {
        entity.setUpdatedAt(OffsetDateTime.now());
        // updatedBy: null bila tidak ada principal
    }
}
