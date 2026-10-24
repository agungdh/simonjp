package id.my.agungdh.simonjp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record CursorPageDTO<T>(
        @Schema(description = "Daftar item pada halaman ini")
        List<T> items,
        @Schema(description = "Cursor untuk halaman berikutnya, null jika tidak ada")
        UUID nextCursor,
        @Schema(description = "Apakah masih ada halaman berikutnya")
        boolean hasMore
) {
}