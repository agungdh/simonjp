package id.my.agungdh.dto;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import id.my.agungdh.enums.KategoriJabatan;
import id.my.agungdh.enums.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.enums.StatusPegawai;
import id.my.agungdh.enums.TipePegawai;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload create/update pegawai (PUT full replace).")
public record PegawaiRequest(
        @NotBlank(message = "nip wajib diisi")
        @Pattern(regexp = "^[0-9]+$", message = "nip hanya boleh berisi angka")
        @Size(max = 30, message = "nip maksimal 30 karakter")
        @Schema(description = "NIP pegawai, hanya angka.", examples = {"198001012005011001"}, required = true) String nip,

        @NotBlank(message = "nama wajib diisi")
        @Size(max = 255, message = "nama maksimal 255 karakter")
        @Schema(description = "Nama pegawai.", examples = {"Budi Santoso"}, required = true) String nama,

        @NotNull(message = "tipe wajib diisi")
        @Schema(description = "Tipe pegawai.", examples = {"PEGAWAI"}, required = true) TipePegawai tipe,

        @NotNull(message = "status wajib diisi")
        @Schema(description = "Status pegawai.", examples = {"AKTIF"}, required = true) StatusPegawai status,

        @NotNull(message = "bidangUuid wajib diisi")
        @Schema(description = "UUID bidang tempat pegawai bertugas.", required = true) UUID bidangUuid,

        @Schema(description = "UUID pangkat golongan; null jika tidak ada.") UUID pangkatGolonganUuid,

        @Size(max = 255, message = "jabatan maksimal 255 karakter")
        @Schema(description = "Jabatan pegawai.", examples = {"Auditor"}) String jabatan,

        @Size(max = 255, message = "peran maksimal 255 karakter")
        @Schema(description = "Peran pegawai.") String peran,

        @Schema(description = "Kategori jabatan; null jika tidak ada.") KategoriJabatan kategoriJabatan,

        @Schema(description = "Kategori kebutuhan jam pelatihan; null jika tidak ada.") KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan) {
}
