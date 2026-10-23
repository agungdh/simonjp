package id.my.agungdh.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Data diklat.")
public record DiklatResponse(
        @Schema(description = "Identitas publik diklat.") UUID uuid,
        @Schema(description = "Pegawai pemilik data.") PegawaiResponse pegawai,
        @Schema(description = "Jenis pelatihan.") JenisPelatihanResponse jenisPelatihan,
        @Schema(description = "Nomor surat.") String nomorSurat,
        @Schema(description = "Materi pengembangan.") String materiPengembangan,
        @Schema(description = "Dari tanggal pelaksanaan.") LocalDate dariTanggalPelaksanaan,
        @Schema(description = "Sampai tanggal pelaksanaan.") LocalDate sampaiTanggalPelaksanaan,
        @Schema(description = "Jumlah jam pelatihan.") Integer jumlahJamPelatihan,
        @Schema(description = "Nama file (jika ada).") String filename) {
}
