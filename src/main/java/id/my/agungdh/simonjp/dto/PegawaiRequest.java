package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import id.my.agungdh.simonjp.validation.ExistingBidang;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record PegawaiRequest(
        @Schema(description = "Nama pegawai", example = "Budi")
        @NotBlank
        String nama,

        @Schema(description = "NIP pegawai", example = "123456")
        @NotBlank
        String nip,

        @Schema(description = "Email pegawai")
        String email,

        @Schema(description = "UUID bidang pegawai")
        @ExistingBidang
        UUID bidangUuid
) {
}