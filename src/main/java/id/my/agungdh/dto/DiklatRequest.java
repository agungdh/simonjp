package id.my.agungdh.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload create/update diklat (PUT full replace).")
public record DiklatRequest(
        @NotNull(message = "pegawaiUuid wajib diisi")
        @Schema(description = "UUID pegawai pemilik data.", required = true) UUID pegawaiUuid,

        @NotNull(message = "jenisPelatihanUuid wajib diisi")
        @Schema(description = "UUID jenis pelatihan.", required = true) UUID jenisPelatihanUuid,

        @NotBlank(message = "nomorSurat wajib diisi")
        @Size(max = 255, message = "nomorSurat maksimal 255 karakter")
        @Schema(description = "Nomor surat.", examples = {"001/2026"}, required = true) String nomorSurat,

        @NotBlank(message = "materiPengembangan wajib diisi")
        @Size(max = 255, message = "materiPengembangan maksimal 255 karakter")
        @Schema(description = "Materi pengembangan.", examples = {"Materi X"}, required = true) String materiPengembangan,

        @NotNull(message = "dariTanggalPelaksanaan wajib diisi")
        @Schema(description = "Dari tanggal pelaksanaan.", required = true) LocalDate dariTanggalPelaksanaan,

        @NotNull(message = "sampaiTanggalPelaksanaan wajib diisi")
        @Schema(description = "Sampai tanggal pelaksanaan.", required = true) LocalDate sampaiTanggalPelaksanaan,

        @NotNull(message = "jumlahJamPelatihan wajib diisi")
        @Schema(description = "Jumlah jam pelatihan.", required = true) Integer jumlahJamPelatihan) {
}
