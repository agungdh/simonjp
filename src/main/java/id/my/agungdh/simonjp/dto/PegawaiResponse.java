package id.my.agungdh.simonjp.dto;

import id.my.agungdh.simonjp.domain.KategoriJabatan;
import id.my.agungdh.simonjp.domain.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.simonjp.domain.PegawaiStatus;
import id.my.agungdh.simonjp.domain.Role;
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

        @Schema(description = "Jabatan pegawai")
        String jabatan,

        @Schema(description = "Peran pegawai")
        String peran,

        @Schema(description = "Status kepegawaian")
        PegawaiStatus status,

        @Schema(description = "Kategori jabatan")
        KategoriJabatan kategoriJabatan,

        @Schema(description = "Kategori kebutuhan jam pelatihan")
        KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan,

        @Schema(description = "UUID bidang pegawai")
        UUID bidangUuid,

        @Schema(description = "UUID pangkat/golongan pegawai")
        UUID pangkatGolonganUuid,

        @Schema(description = "Username akun (selalu sama dengan NIP)")
        String username,

        @Schema(description = "Role akun")
        Role role
) {
}
