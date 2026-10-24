package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ErrorResponse(
        @Schema(description = "Pesan error") String message,
        @Schema(description = "Daftar error validasi per field; null jika bukan error validasi")
        List<FieldError> errors
) {
    public record FieldError(
            @Schema(description = "Nama field yang gagal validasi", example = "nama") String field,
            @Schema(description = "Pesan error validasi", example = "must not be blank") String message
    ) {
    }

    public ErrorResponse(String message) {
        this(message, null);
    }
}