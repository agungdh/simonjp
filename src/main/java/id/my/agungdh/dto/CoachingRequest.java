package id.my.agungdh.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload create/update Coaching (PUT full replace).")
public record CoachingRequest(
        @NotNull(message = "pegawaiUuid wajib diisi")
        @Schema(description = "UUID pegawai pemilik data.", required = true) UUID pegawaiUuid,

        @NotBlank(message = "materiPengembangan wajib diisi")
        @Size(max = 255, message = "materiPengembangan maksimal 255 karakter")
        @Schema(description = "Materi pengembangan.", examples = {"Materi X"}, required = true) String materiPengembangan,

        @NotNull(message = "tanggalPelaksanaan wajib diisi")
        @Schema(description = "Tanggal pelaksanaan.", required = true) LocalDate tanggalPelaksanaan,

        @NotNull(message = "jumlahJam wajib diisi")
        @Schema(description = "Jumlah jam.", required = true) Integer jumlahJam) {
}
