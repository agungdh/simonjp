package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.PangkatGolongan;
import id.my.agungdh.simonjp.domain.Pegawai;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementasi query dinamis untuk {@link PegawaiRepositoryCustom}.
 *
 * <p>Semua filter, pencarian, urutan, dan limit dikerjakan di database. Search adalah satu kata
 * kunci untuk FE, jadi kolom teks pegawai digabung dengan kolom teks {@code bidang} dan
 * {@code pangkat_golongan} lewat {@code LEFT JOIN}; predikatnya ada di {@code WHERE}. Semua join
 * memakai {@code LEFT} supaya bentuk query seragam: relasi yang {@code NULL} tidak cocok dengan
 * pola search, tetapi pegawai tetap bisa ditemukan lewat kolomnya sendiri. Filter exact
 * {@code bidangUuid}/{@code pangkatGolonganUuid} tetap benar karena {@code uuid = ?} menolak
 * {@code NULL}.
 *
 * <p>Query ID dan query fetch dipisah supaya join untuk search tidak terduplikasi dengan join dari
 * fetch graph. Query fetch hanya mengambil baris yang sudah terpilih, jadi urutan dan limit tetap
 * ditentukan database.
 */
public class PegawaiRepositoryImpl implements PegawaiRepositoryCustom {

    /** Harus sama dengan {@code SearchTerm.ESCAPE}. */
    private static final char LIKE_ESCAPE = '!';

    private final EntityManager entityManager;

    public PegawaiRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Pegawai> findPage(PegawaiFilter filter, Long cursorId, int limit) {
        return fetchByIds(findIds(filter, cursorId, limit));
    }

    private List<Long> findIds(PegawaiFilter filter, Long cursorId, int limit) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<Pegawai> root = criteria.from(Pegawai.class);
        criteria.select(root.<Long>get("id"));

        List<Predicate> predicates = new ArrayList<>();
        if (cursorId != null) {
            predicates.add(cb.lessThan(root.get("id"), cursorId));
        }
        if (filter.status() != null) {
            predicates.add(cb.equal(root.get("status"), filter.status()));
        }
        if (filter.kategoriJabatan() != null) {
            predicates.add(cb.equal(root.get("kategoriJabatan"), filter.kategoriJabatan()));
        }
        if (filter.kategoriKebutuhanJamPelatihan() != null) {
            predicates.add(cb.equal(root.get("kategoriKebutuhanJamPelatihan"),
                    filter.kategoriKebutuhanJamPelatihan()));
        }

        String pattern = filter.searchPattern();
        Join<Pegawai, Bidang> bidang = null;
        Join<Pegawai, PangkatGolongan> pangkat = null;
        // Semua join LEFT supaya seragam dan mudah dibaca. Filter exact tetap aman karena
        // `uuid = ?` menolak NULL, jadi baris tanpa relasi otomatis tidak lolos.
        if (pattern != null || filter.bidangUuid() != null) {
            bidang = root.join("bidang", JoinType.LEFT);
        }
        if (pattern != null || filter.pangkatGolonganUuid() != null) {
            pangkat = root.join("pangkatGolongan", JoinType.LEFT);
        }
        if (filter.bidangUuid() != null) {
            predicates.add(cb.equal(bidang.get("uuid"), filter.bidangUuid()));
        }
        if (filter.pangkatGolonganUuid() != null) {
            predicates.add(cb.equal(pangkat.get("uuid"), filter.pangkatGolonganUuid()));
        }
        if (pattern != null) {
            predicates.add(cb.or(
                    like(cb, root.get("nama"), pattern),
                    like(cb, root.get("nip"), pattern),
                    like(cb, root.get("email"), pattern),
                    like(cb, root.get("jabatan"), pattern),
                    like(cb, root.get("peran"), pattern),
                    like(cb, bidang.get("nama"), pattern),
                    like(cb, bidang.get("deskripsi"), pattern),
                    like(cb, pangkat.get("jenjang"), pattern),
                    like(cb, pangkat.get("pangkat"), pattern),
                    like(cb, pangkat.get("golongan"), pattern),
                    like(cb, pangkat.get("ruang"), pattern)
            ));
        }

        criteria.where(predicates.toArray(Predicate[]::new));
        criteria.orderBy(cb.desc(root.get("id")));
        return entityManager.createQuery(criteria).setMaxResults(limit).getResultList();
    }

    private List<Pegawai> fetchByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pegawai> criteria = cb.createQuery(Pegawai.class);
        Root<Pegawai> root = criteria.from(Pegawai.class);
        criteria.where(root.get("id").in(ids));
        criteria.orderBy(cb.desc(root.get("id")));

        TypedQuery<Pegawai> query = entityManager.createQuery(criteria);
        // open-in-view dimatikan; relasi response di-fetch sekaligus supaya tidak N+1.
        query.setHint("jakarta.persistence.fetchgraph", fetchGraph());
        return query.getResultList();
    }

    private Predicate like(CriteriaBuilder cb, Expression<String> expression, String pattern) {
        return cb.like(cb.lower(expression), pattern, LIKE_ESCAPE);
    }

    private EntityGraph<Pegawai> fetchGraph() {
        EntityGraph<Pegawai> graph = entityManager.createEntityGraph(Pegawai.class);
        graph.addAttributeNodes("bidang", "pangkatGolongan", "account");
        return graph;
    }
}
