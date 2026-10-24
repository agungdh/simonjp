package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record PangkatGolonganResponse(
        @Schema(description = "UUID publik pangkat/golongan")
        UUID uuid,

        @Schema(description = "Jenjang")
        String jenjang,

        @Schema(description = "Pangkat")
        String pangkat,

        @Schema(description = "Golongan")
        String golongan,

        @Schema(description = "Ruang")
        String ruang
) {
}
