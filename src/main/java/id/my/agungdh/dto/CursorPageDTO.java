package id.my.agungdh.dto;

import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Satu halaman hasil cursor pagination.")
public record CursorPageDTO<T>(
        @Schema(description = "Baris pada halaman ini.") List<T> items,
        @Schema(description = "UUID baris terakhir, dipakai sebagai cursor halaman berikutnya; null kalau tidak ada lagi.") UUID nextCursor,
        @Schema(description = "True jika masih ada halaman berikutnya.") boolean hasMore) {
}
