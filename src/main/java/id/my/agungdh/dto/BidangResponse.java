package id.my.agungdh.dto;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Data bidang.")
public record BidangResponse(
        @Schema(description = "Identitas publik bidang.") UUID uuid,
        @Schema(description = "Nama bidang.", example = "BIDANG INVESTIGASI") String bidang) {
}
