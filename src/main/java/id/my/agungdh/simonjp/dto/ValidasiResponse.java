package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ValidasiResponse(
        @Schema(description = "Pesan") String message,
        @Schema(description = "Nama") String nama,
        @Schema(description = "Email") String email,
        @Schema(description = "Umur") Integer umur,
        @Schema(description = "Harga") BigDecimal harga,
        @Schema(description = "Stok") Integer stok
) {
}