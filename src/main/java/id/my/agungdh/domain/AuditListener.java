package id.my.agungdh.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Mengisi kolom audit secara server-side di satu tempat. Selama auth belum ada, aksi pengguna
 * diwakili id {@code 0L}; nanti diganti principal lewat abstraksi {@code CurrentUser}.
 */
public class AuditListener {

    public static final long SYSTEM_USER_ID = 0L;

    @PrePersist
    void prePersist(BaseEntity entity) {
        OffsetDateTime now = OffsetDateTime.now();

        if (entity.getUuid() == null) {
            entity.setUuid(UUID.randomUUID());
        }
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        if (entity.getCreatedBy() == null) {
            entity.setCreatedBy(SYSTEM_USER_ID);
        }
        entity.setUpdatedAt(now);
        if (entity.getUpdatedBy() == null) {
            entity.setUpdatedBy(SYSTEM_USER_ID);
        }
    }

    @PreUpdate
    void preUpdate(BaseEntity entity) {
        entity.setUpdatedAt(OffsetDateTime.now());
        if (entity.getUpdatedBy() == null) {
            entity.setUpdatedBy(SYSTEM_USER_ID);
        }
    }
}
