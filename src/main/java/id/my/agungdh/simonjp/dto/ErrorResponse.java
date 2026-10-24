package id.my.agungdh.simonjp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "Pesan error") String message,
        @Schema(description = "Map field -> daftar pesan error validasi; tidak dikirim jika bukan error validasi")
        Map<String, List<String>> fields
) {
    public ErrorResponse(String message) {
        this(message, null);
    }
}