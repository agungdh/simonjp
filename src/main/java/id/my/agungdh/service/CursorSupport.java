package id.my.agungdh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import id.my.agungdh.domain.BaseEntity;
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

    /** Potong baris ekstra, lalu tentukan cursor halaman berikutnya dari {@code uuid} baris terakhir. */
    static <T extends BaseEntity> CursorPageDTO<T> page(List<T> rows, int limit) {
        boolean hasMore = rows.size() > limit;
        List<T> items = hasMore ? new ArrayList<>(rows.subList(0, limit)) : rows;
        T last = items.isEmpty() ? null : items.getLast();
        UUID nextCursor = hasMore && last != null ? last.getUuid() : null;
        return new CursorPageDTO<>(items, nextCursor, hasMore);
    }
}
