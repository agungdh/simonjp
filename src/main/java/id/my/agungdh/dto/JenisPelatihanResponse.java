package id.my.agungdh.dto;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Data jenis pelatihan.")
public record JenisPelatihanResponse(
        @Schema(description = "Identitas publik jenis pelatihan.") UUID uuid,
        @Schema(description = "Nama jenis pelatihan.", examples = {"Diklat Teknis"}) String jenisPelatihan) {
}
