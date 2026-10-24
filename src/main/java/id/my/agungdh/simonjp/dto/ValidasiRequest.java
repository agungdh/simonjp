package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ValidasiRequest(
        @Schema(description = "Nama", example = "")
        @NotBlank
        @Size(min = 2, max = 50)
        String nama,

        @Schema(description = "Email", example = "bukan-email")
        @NotBlank
        @Email
        String email,

        @Schema(description = "Umur", example = "10")
        @NotNull
        @Min(17)
        @Max(65)
        Integer umur,

        @Schema(description = "Harga", example = "-5")
        @NotNull
        @Positive
        BigDecimal harga,

        @Schema(description = "Stok", example = "-1")
        @NotNull
        @PositiveOrZero
        Integer stok
) {
}