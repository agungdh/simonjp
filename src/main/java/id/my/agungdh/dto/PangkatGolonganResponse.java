package id.my.agungdh.dto;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Data pangkat golongan.")
public record PangkatGolonganResponse(
        @Schema(description = "Identitas publik pangkat golongan.") UUID uuid,
        @Schema(description = "Jenjang kepangkatan.", example = "Penata") String jenjang,
        @Schema(description = "Nama pangkat.", example = "Penata Muda") String pangkat,
        @Schema(description = "Golongan.", example = "III") String golongan,
        @Schema(description = "Ruang.", example = "a") String ruang) {
}
