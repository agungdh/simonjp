package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PangkatGolonganRequest(
        @Schema(description = "Jenjang", example = "Ahli Muda")
        @NotBlank
        @Size(max = 100)
        String jenjang,

        @Schema(description = "Pangkat", example = "Penata Muda Tingkat I")
        @NotBlank
        @Size(max = 100)
        String pangkat,

        @Schema(description = "Golongan", example = "III")
        @NotBlank
        @Size(max = 20)
        String golongan,

        @Schema(description = "Ruang", example = "b")
        @Size(max = 20)
        String ruang
) {
}
