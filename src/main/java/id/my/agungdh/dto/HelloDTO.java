package id.my.agungdh.dto;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Response DTO untuk {@code GET /hello}.
 *
 * <p>DTO sengaja dibuat sebagai {@code record} supaya immutable dan tidak perlu boilerplate.
 */
@Schema(name = "HelloDTO", description = "Sapaan beserta informasi thread yang melayani request")
public record HelloDTO(
        @Schema(description = "Pesan sapaan") String message,
        @Schema(description = "Waktu response dibuat") OffsetDateTime timestamp,
        @Schema(description = "Nama thread yang mengeksekusi request") String thread,
        @Schema(description = "true jika request dilayani virtual thread") boolean virtualThread) {
}
