package id.my.agungdh.simonjp.dto;

import id.my.agungdh.simonjp.domain.KategoriJabatan;
import id.my.agungdh.simonjp.domain.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.simonjp.domain.PegawaiStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Pegawai lengkap dengan bidang dan pangkat/golongan, khusus untuk response auth
 * ({@code /auth/login}, {@code /auth/token}, {@code /auth/me}). Endpoint {@code /pegawai}
 * tetap memakai {@link PegawaiResponse}.
 */
public record UserPegawaiResponse(
        @Schema(description = "UUID publik pegawai") UUID uuid,
        @Schema(description = "Nama pegawai", example = "Budi") String nama,
        @Schema(description = "NIP pegawai", example = "123456") String nip,
        @Schema(description = "Email pegawai") String email,
        @Schema(description = "Jabatan pegawai") String jabatan,
        @Schema(description = "Peran pegawai") String peran,
        @Schema(description = "Status kepegawaian") PegawaiStatus status,
        @Schema(description = "Kategori jabatan") KategoriJabatan kategoriJabatan,
        @Schema(description = "Kategori kebutuhan jam pelatihan") KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan,
        @Schema(description = "Bidang pegawai") BidangResponse bidang,
        @Schema(description = "Pangkat/golongan pegawai") PangkatGolonganResponse pangkatGolongan
) {
}
