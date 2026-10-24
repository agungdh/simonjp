package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record PegawaiResponse(
        @Schema(description = "UUID publik pegawai")
        UUID uuid,

        @Schema(description = "Nama pegawai")
        String nama,

        @Schema(description = "NIP pegawai")
        String nip,

        @Schema(description = "Email pegawai")
        String email,

        @Schema(description = "UUID bidang pegawai")
        UUID bidangUuid
) {
}