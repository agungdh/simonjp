package id.my.agungdh.simonjp.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * Kata kunci pencarian substring ({@code %kata%}) yang case-insensitive.
 *
 * <p>Input user dianggap literal: karakter wildcard LIKE ({@code %}, {@code _}) dan karakter escape
 * di-escape, jadi mengetik {@code %} tidak otomatis mencocokkan semua baris.
 *
 * <p>Batas minimal 3 karakter karena trigram butuh 3 karakter untuk bisa dipakai index; pattern
 * yang lebih pendek tetap benar tapi akan jatuh ke scan penuh.
 */
public final class SearchTerm {

    /**
     * Karakter escape LIKE. Harus sama dengan {@code ESCAPE '!'} di query repository dan
     * {@code PegawaiRepositoryImpl}.
     */
    public static final char ESCAPE = '!';

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 100;

    private final String needle;
    private final String pattern;

    private SearchTerm(String needle) {
        this.needle = needle;
        this.pattern = "%" + escape(needle) + "%";
    }

    /**
     * Normalisasi input pencarian.
     *
     * @return {@code null} kalau input null atau kosong, supaya pemanggil tidak perlu mengecek lagi
     * @throws ResponseStatusException 400 kalau panjang input di luar batas
     */
    public static SearchTerm of(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() < MIN_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pencarian minimal 3 karakter");
        }
        if (trimmed.length() > MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pencarian maksimal 100 karakter");
        }
        return new SearchTerm(trimmed.toLowerCase(Locale.ROOT));
    }

    /** Pattern siap pakai untuk {@code LIKE ... ESCAPE '!'}. */
    public String pattern() {
        return pattern;
    }

    /** Pencocokan di Java untuk validasi cursor; perilakunya harus sama dengan LIKE di database. */
    public boolean matches(String value) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    public boolean matchesAny(String... values) {
        for (String value : values) {
            if (matches(value)) {
                return true;
            }
        }
        return false;
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            if (c == ESCAPE || c == '%' || c == '_') {
                escaped.append(ESCAPE);
            }
            escaped.append(c);
        }
        return escaped.toString();
    }
}
