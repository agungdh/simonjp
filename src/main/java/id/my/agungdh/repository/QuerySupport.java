package id.my.agungdh.repository;

import java.util.Locale;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QuerySupport {

    public static final char LIKE_ESCAPE = '\\';

    /** Bungkus pencarian menjadi pola LIKE yang aman dari wildcard input pengguna. */
    public static String likePattern(String search) {
        String escaped = search.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    /** Normalisasi nilai teks dari request agar konsisten saat disimpan. */
    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
