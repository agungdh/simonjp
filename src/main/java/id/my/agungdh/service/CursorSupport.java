package id.my.agungdh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.repository.QuerySupport;

/**
 * Aturan cursor pagination yang sama untuk semua resource: limit default 20, maksimum 100, dan
 * pengambilan {@code limit + 1} baris untuk menentukan {@code hasMore}.
 */
final class CursorSupport {

    static final int DEFAULT_LIMIT = 20;
    static final int MAX_LIMIT = 100;

    private CursorSupport() {
    }

    static int normalizeLimit(int limit) {
        if (limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    static String normalizeSearch(String search) {
        return QuerySupport.trimToNull(search);
    }

    /** Potong baris ekstra, lalu tentukan cursor halaman berikutnya. */
    static <T> CursorPageDTO<T> page(List<T> rows, int limit, Function<T, UUID> uuidExtractor) {
        boolean hasMore = rows.size() > limit;
        List<T> items = hasMore ? new ArrayList<>(rows.subList(0, limit)) : rows;
        UUID nextCursor = hasMore && !items.isEmpty() ? uuidExtractor.apply(items.get(items.size() - 1)) : null;
        return new CursorPageDTO<>(items, nextCursor, hasMore);
    }
}
