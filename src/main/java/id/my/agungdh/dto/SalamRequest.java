package id.my.agungdh.dto;

/**
 * DTO masukan untuk {@code POST /contoh/data}.
 *
 * <p>Hanya field yang boleh diisi client yang ada di sini; {@code id}, {@code dibuatPada}, dan
 * {@code catatanInternal} di-generate oleh server.
 */
public record SalamRequest(
        String nama,
        String pesan) {
}
