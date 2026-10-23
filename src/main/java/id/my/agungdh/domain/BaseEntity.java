package id.my.agungdh.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.SQLRestriction;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Basis semua entity: identitas internal ({@code id}), identitas publik ({@code uuid}), dan audit.
 * {@code @SQLRestriction} membuat query Hibernate otomatis menyaring baris yang sudah soft delete.
 *
 * <p>Kolom audit bersifat opsional dan informasional: hanya untuk penelusuran saat melihat langsung
 * ke database. Baris hasil impor dari aplikasi lama boleh tidak punya nilai audit sama sekali.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditListener.class)
@SQLRestriction("deleted_at IS NULL")
public abstract class BaseEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "uuid", nullable = false, updatable = false)
    private UUID uuid;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;
}
