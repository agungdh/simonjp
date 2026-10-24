package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record ErrorResponse(
        @Schema(description = "Pesan error") String message,
        @Schema(description = "Map field -> daftar pesan error validasi; null jika bukan error validasi")
        Map<String, List<String>> errors
) {
    public ErrorResponse(String message) {
        this(message, null);
    }
}