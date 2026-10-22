package id.my.agungdh.dto;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO keluaran untuk data salam.
 *
 * <p>Perhatikan: {@code catatanInternal} milik entity sengaja tidak ada di sini, jadi otomatis
 * tidak ikut terkirim ke client.
 */
@Schema(name = "SalamDTO", description = "Data salam yang dikirim ke client")
public record SalamDTO(
        @Schema(description = "Id data", readOnly = true) Long id,
        @Schema(description = "Nama pengirim") String nama,
        @Schema(description = "Isi pesan") String pesan,
        @Schema(description = "Waktu data dibuat", readOnly = true) OffsetDateTime dibuatPada) {
}
