package id.my.agungdh.simonjp.dto;

import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.KategoriJabatan;
import id.my.agungdh.simonjp.domain.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.simonjp.domain.PangkatGolongan;
import id.my.agungdh.simonjp.domain.PegawaiStatus;
import id.my.agungdh.simonjp.domain.Role;
import id.my.agungdh.simonjp.validation.Exists;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request create/update pegawai sekaligus akunnya (1 pegawai = 1 akun).
 *
 * <p>Username akun selalu sama dengan {@code nip}. {@code password} bersifat opsional:
 * saat create kosong berarti memakai NIP, saat update kosong berarti tidak diubah. Ini
 * pengecualian full-replace karena kredensial tidak pernah "dikosongkan".
 */
public record PegawaiRequest(
        @Schema(description = "Nama pegawai", example = "Budi")
        @NotBlank
        @Size(max = 255)
        String nama,

        @Schema(description = "NIP pegawai, juga dipakai sebagai username akun", example = "197001012000031001")
        @NotBlank
        @Size(max = 50)
        String nip,

        @Schema(description = "Email pegawai", example = "budi@example.com")
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "Jabatan pegawai", example = "Auditor Muda")
        @Size(max = 255)
        String jabatan,

        @Schema(description = "Peran pegawai", example = "Ketua Tim")
        @Size(max = 255)
        String peran,

        @Schema(description = "Status kepegawaian", example = "AKTIF")
        @NotNull
        PegawaiStatus status,

        @Schema(description = "Kategori jabatan", example = "FUNGSIONAL_AUDITOR")
        KategoriJabatan kategoriJabatan,

        @Schema(description = "Kategori kebutuhan jam pelatihan", example = "AUDITOR")
        KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan,

        @Schema(description = "UUID bidang pegawai")
        @Exists(value = Bidang.class, message = "Bidang tidak ditemukan")
        UUID bidangUuid,

        @Schema(description = "UUID pangkat/golongan pegawai")
        @Exists(value = PangkatGolongan.class, message = "Pangkat/golongan tidak ditemukan")
        UUID pangkatGolonganUuid,

        @Schema(description = "Role akun", example = "PEGAWAI")
        @NotNull
        Role role,

        @Schema(description = "Password akun; kosong saat create berarti memakai NIP, "
                + "kosong saat update berarti tidak diubah", example = "rahasia123")
        @Size(min = 8, max = 72)
        String password
) {
    public PegawaiRequest {
        if (password != null && password.isBlank()) {
            password = null;
        }
    }
}
