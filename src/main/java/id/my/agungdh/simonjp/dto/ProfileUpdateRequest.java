package id.my.agungdh.simonjp.dto;

import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.KategoriJabatan;
import id.my.agungdh.simonjp.domain.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.simonjp.domain.PangkatGolongan;
import id.my.agungdh.simonjp.validation.Exists;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Perubahan profil milik user yang sedang login. Role dan status tidak bisa diubah
 * lewat endpoint ini supaya user tidak bisa menaikkan hak aksesnya sendiri.
 */
public record ProfileUpdateRequest(
        @Schema(description = "Nama pegawai", example = "Budi")
        @NotBlank
        @Size(max = 255)
        String nama,

        @Schema(description = "NIP pegawai, juga dipakai sebagai username akun")
        @NotBlank
        @Size(max = 50)
        String nip,

        @Schema(description = "Email pegawai")
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "Jabatan pegawai")
        @Size(max = 255)
        String jabatan,

        @Schema(description = "Peran pegawai")
        @Size(max = 255)
        String peran,

        @Schema(description = "Kategori jabatan")
        KategoriJabatan kategoriJabatan,

        @Schema(description = "Kategori kebutuhan jam pelatihan")
        KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan,

        @Schema(description = "UUID bidang pegawai")
        @Exists(value = Bidang.class, message = "Bidang tidak ditemukan")
        UUID bidangUuid,

        @Schema(description = "UUID pangkat/golongan pegawai")
        @Exists(value = PangkatGolongan.class, message = "Pangkat/golongan tidak ditemukan")
        UUID pangkatGolonganUuid
) {
}
