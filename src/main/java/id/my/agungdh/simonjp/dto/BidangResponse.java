package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record BidangResponse(
        @Schema(description = "UUID publik bidang")
        UUID uuid,

        @Schema(description = "Nama bidang")
        String nama,

        @Schema(description = "Deskripsi bidang")
        String deskripsi
) {
}