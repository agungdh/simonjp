package id.my.agungdh.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import id.my.agungdh.dto.PegawaiResponse;

@Schema(description = "Data Workshop.")
public record WorkshopResponse(
        @Schema(description = "Identitas publik Workshop.") UUID uuid,
        @Schema(description = "Pegawai pemilik data.") PegawaiResponse pegawai,
        @Schema(description = "Materi pengembangan.") String materiPengembangan,
        @Schema(description = "Tanggal pelaksanaan.") LocalDate tanggalPelaksanaan,
        @Schema(description = "Jumlah jam.") Integer jumlahJam,
        @Schema(description = "Nama file (jika ada).") String filename) {
}
