package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        @Schema(description = "UUID publik produk")
        UUID uuid,

        @Schema(description = "Nama produk")
        String nama,

        @Schema(description = "Deskripsi produk")
        String deskripsi,

        @Schema(description = "Harga produk")
        BigDecimal harga,

        @Schema(description = "Stok produk")
        Integer stok,

        @Schema(description = "UUID kategori produk")
        UUID kategoriUuid
) {
}