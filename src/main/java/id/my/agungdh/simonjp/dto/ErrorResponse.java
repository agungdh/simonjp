package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "HTTP status code") int status,
        @Schema(description = "Pesan error") String message
) {
}