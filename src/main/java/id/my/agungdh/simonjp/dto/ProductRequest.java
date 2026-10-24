package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequest(
        @Schema(description = "Nama produk", example = "Laptop")
        @NotBlank
        String nama,

        @Schema(description = "Deskripsi produk")
        String deskripsi,

        @Schema(description = "Harga produk", example = "15000000")
        @NotNull
        @Positive
        BigDecimal harga,

        @Schema(description = "Stok produk", example = "10")
        @NotNull
        @PositiveOrZero
        Integer stok
) {
}