package id.my.agungdh.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload create/update bidang (PUT full replace).")
public record BidangRequest(
        @NotBlank(message = "bidang wajib diisi")
        @Size(max = 255, message = "bidang maksimal 255 karakter")
        @Schema(description = "Nama bidang.", examples = {"BIDANG INVESTIGASI"}, required = true) String bidang) {
}
