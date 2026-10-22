package id.my.agungdh.dto;

import java.time.OffsetDateTime;

/**
 * DTO keluaran untuk data salam.
 *
 * <p>Perhatikan: {@code catatanInternal} milik entity sengaja tidak ada di sini, jadi otomatis
 * tidak ikut terkirim ke client.
 */
public record SalamDTO(
        Long id,
        String nama,
        String pesan,
        OffsetDateTime dibuatPada) {
}
