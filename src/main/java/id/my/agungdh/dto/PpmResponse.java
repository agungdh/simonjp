package id.my.agungdh.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Data ppm.")
public record PpmResponse(
        @Schema(description = "Identitas publik ppm.") UUID uuid,
        @Schema(description = "Pegawai pemilik data.") PegawaiResponse pegawai,
        @Schema(description = "Nomor surat.") String nomorSurat,
        @Schema(description = "Materi pengembangan.") String materiPengembangan,
        @Schema(description = "Tanggal pelaksanaan.") LocalDate tanggalPelaksanaan,
        @Schema(description = "Jumlah jam pelatihan.") Integer jumlahJamPelatihan) {
}
