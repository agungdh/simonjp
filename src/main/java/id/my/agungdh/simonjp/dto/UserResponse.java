package id.my.agungdh.simonjp.dto;

import id.my.agungdh.simonjp.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record UserResponse(
        @Schema(description = "UUID user") UUID uuid,
        @Schema(description = "Username akun", example = "admin") String username,
        @Schema(description = "Role user") Role role,
        @Schema(description = "Pegawai pemilik akun") UserPegawaiResponse pegawai
) {
}
