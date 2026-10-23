package id.my.agungdh.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload create/update jenis pelatihan (PUT full replace).")
public record JenisPelatihanRequest(
        @NotBlank(message = "jenisPelatihan wajib diisi")
        @Size(max = 255, message = "jenisPelatihan maksimal 255 karakter")
        @Schema(description = "Nama jenis pelatihan.", examples = {"Diklat Teknis"}, required = true) String jenisPelatihan) {
}
