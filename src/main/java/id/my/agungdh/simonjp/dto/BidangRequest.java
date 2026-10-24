package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record BidangRequest(
        @Schema(description = "Nama bidang", example = "IT")
        @NotBlank
        String nama,

        @Schema(description = "Deskripsi bidang")
        String deskripsi
) {
}