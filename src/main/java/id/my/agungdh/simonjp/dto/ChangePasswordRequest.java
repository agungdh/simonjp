package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @Schema(description = "Password saat ini", example = "rahasia123")
        @NotBlank
        String currentPassword,

        @Schema(description = "Password baru", example = "rahasia456")
        @NotBlank
        @Size(min = 8, max = 72)
        String newPassword
) {
}
