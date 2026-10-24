package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "Opaque token untuk header Authorization: Bearer") String token,
        @Schema(description = "Masa berlaku token dalam detik (sliding)") long expiresIn,
        @Schema(description = "User pemilik token") UserResponse user
) {
}
