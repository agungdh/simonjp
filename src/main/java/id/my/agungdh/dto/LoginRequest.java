package id.my.agungdh.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload login.")
public record LoginRequest(
        @NotBlank(message = "username wajib diisi")
        @Schema(description = "Username.", required = true) String username,

        @NotBlank(message = "password wajib diisi")
        @Schema(description = "Password.", required = true) String password) {
}
