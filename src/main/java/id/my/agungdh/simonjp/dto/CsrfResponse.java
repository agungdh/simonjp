package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CsrfResponse(
        @Schema(description = "Token untuk header X-XSRF-TOKEN") String token
) {
}
