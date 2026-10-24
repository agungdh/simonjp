package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.validation.Exists;
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
        @Exists(value = Bidang.class, message = "Bidang tidak ditemukan")
        UUID bidangUuid
) {
}