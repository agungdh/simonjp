package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record KategoriRequest(
        @Schema(description = "Nama kategori", example = "Elektronik")
        @NotBlank
        String nama,

        @Schema(description = "Deskripsi kategori")
        String deskripsi
) {
}