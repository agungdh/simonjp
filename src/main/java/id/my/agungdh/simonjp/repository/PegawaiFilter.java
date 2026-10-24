package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.KategoriJabatan;
import id.my.agungdh.simonjp.domain.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.simonjp.domain.PegawaiStatus;

import java.util.UUID;

/**
 * Filter list pegawai. Semua field nullable; {@code null} berarti filter tersebut tidak dipakai.
 *
 * <p>{@code searchPattern} sudah di-escape dan siap dipakai untuk {@code LIKE ... ESCAPE '!'}.
 */
public record PegawaiFilter(
        String searchPattern,
        PegawaiStatus status,
        UUID bidangUuid,
        UUID pangkatGolonganUuid,
        KategoriJabatan kategoriJabatan,
        KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan
) {
}
