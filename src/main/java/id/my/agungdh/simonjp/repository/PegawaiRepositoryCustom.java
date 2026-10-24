package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.Pegawai;

import java.util.List;

/**
 * Query list pegawai dengan filter dinamis. Predicate hanya dibangun untuk filter yang dikirim,
 * supaya PostgreSQL tetap bisa memakai index (termasuk GIN trigram untuk search).
 */
public interface PegawaiRepositoryCustom {

    /**
     * @param filter   filter aktif; semua field null berarti tanpa filter
     * @param cursorId posisi keyset ({@code id < cursorId}), {@code null} untuk halaman pertama
     * @param limit    jumlah baris yang diambil (service meminta {@code pageSize + 1})
     */
    List<Pegawai> findPage(PegawaiFilter filter, Long cursorId, int limit);
}
