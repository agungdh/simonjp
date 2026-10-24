package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record KategoriResponse(
        @Schema(description = "UUID publik kategori")
        UUID uuid,

        @Schema(description = "Nama kategori")
        String nama,

        @Schema(description = "Deskripsi kategori")
        String deskripsi
) {
}