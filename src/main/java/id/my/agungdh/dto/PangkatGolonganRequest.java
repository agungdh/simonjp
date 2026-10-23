package id.my.agungdh.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload create/update pangkat golongan (PUT full replace).")
public record PangkatGolonganRequest(
        @NotBlank(message = "jenjang wajib diisi")
        @Size(max = 50, message = "jenjang maksimal 50 karakter")
        @Schema(description = "Jenjang kepangkatan.", example = "Penata", required = true) String jenjang,

        @NotBlank(message = "pangkat wajib diisi")
        @Size(max = 100, message = "pangkat maksimal 100 karakter")
        @Schema(description = "Nama pangkat.", example = "Penata Muda", required = true) String pangkat,

        @NotBlank(message = "golongan wajib diisi")
        @Size(max = 10, message = "golongan maksimal 10 karakter")
        @Schema(description = "Golongan.", example = "III", required = true) String golongan,

        @NotBlank(message = "ruang wajib diisi")
        @Size(max = 10, message = "ruang maksimal 10 karakter")
        @Schema(description = "Ruang.", example = "a", required = true) String ruang) {
}
