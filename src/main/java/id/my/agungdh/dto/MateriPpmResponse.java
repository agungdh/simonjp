package id.my.agungdh.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Data materi ppm.")
public record MateriPpmResponse(
        @Schema(description = "Identitas publik materi ppm.") UUID uuid,
        @Schema(description = "Nomor surat.") String nomorSurat,
        @Schema(description = "Nama pemateri.") String namaPemateri,
        @Schema(description = "Materi pengembangan.") String materiPengembangan,
        @Schema(description = "Tanggal pelaksanaan.") LocalDate tanggalPelaksanaan,
        @Schema(description = "Link materi.") String linkMateri,
        @Schema(description = "Link dokumentasi.") String linkDokumentasi) {
}
