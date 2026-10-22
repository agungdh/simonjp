package id.my.agungdh.domain;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contoh model domain yang memakai Lombok.
 *
 * <p>{@code @Data} membuat getter/setter/toString/equals/hashCode, {@code @Builder} membuat
 * builder, dan dua anotasi constructor menghilangkan boilerplate constructor.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Salam {

    private Long id;
    private String nama;
    private String pesan;

    /** Field internal, tidak pernah diekspos lewat {@code SalamDTO}. */
    private String catatanInternal;

    private OffsetDateTime dibuatPada;
}
