package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Pegawai lengkap dengan bidang, khusus untuk response auth ({@code /auth/login},
 * {@code /auth/token}, {@code /auth/me}). Endpoint {@code /pegawai} tetap memakai
 * {@link PegawaiResponse} yang lebih ramping.
 */
public record UserPegawaiResponse(
        @Schema(description = "UUID publik pegawai") UUID uuid,
        @Schema(description = "Nama pegawai", example = "Budi") String nama,
        @Schema(description = "NIP pegawai", example = "123456") String nip,
        @Schema(description = "Email pegawai") String email,
        @Schema(description = "Bidang pegawai") BidangResponse bidang
) {
}
