package id.my.agungdh.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * DTO masukan untuk {@code POST /contoh/data}.
 *
 * <p>Hanya field yang boleh diisi client yang ada di sini; {@code id}, {@code dibuatPada}, dan
 * {@code catatanInternal} di-generate oleh server.
 */
@Schema(name = "SalamRequest", description = "Payload untuk membuat data salam baru")
public record SalamRequest(
        @Schema(description = "Nama pengirim", required = true) String nama,
        @Schema(description = "Isi pesan", required = true) String pesan) {
}
