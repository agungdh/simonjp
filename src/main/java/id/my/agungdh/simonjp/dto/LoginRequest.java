package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Username akun", example = "admin")
        @NotBlank
        String username,

        @Schema(description = "Password akun", example = "rahasia")
        @NotBlank
        String password
) {
}
