package id.my.agungdh.domain;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contoh model domain yang memakai Lombok dan dipetakan ke tabel {@code salam}.
 *
 * <p>{@code @Data} membuat getter/setter/toString/equals/hashCode, {@code @Builder} membuat
 * builder, dan dua anotasi constructor menghilangkan boilerplate constructor.
 */
@Entity
@Table(name = "salam")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Salam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nama;
    private String pesan;

    /** Field internal, tidak pernah diekspos lewat {@code SalamDTO}. */
    @Column(name = "catatan_internal")
    private String catatanInternal;

    @Column(name = "dibuat_pada")
    private OffsetDateTime dibuatPada;
}
