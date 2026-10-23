package id.my.agungdh.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Pesan error API.")
public record ErrorResponse(
        @Schema(description = "Penjelasan singkat error.", example = "Data tidak ditemukan.") String message) {
}
