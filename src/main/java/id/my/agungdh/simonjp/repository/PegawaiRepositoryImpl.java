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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementasi query dinamis untuk {@link PegawaiRepositoryCustom}. Predicate dibangun per filter
 * (bukan {@code :param IS NULL OR ...}) agar PostgreSQL tidak memakai generic plan yang memaksa
 * seq scan saat filter dikirim.
 *
 * <p>Search adalah satu kata kunci untuk FE, jadi kolom teks milik pegawai ikut digabung dengan
 * kolom teks {@code bidang} dan {@code pangkat_golongan}. OR lintas tabel tidak bisa memakai index
 * sekaligus, sehingga pencarian dipecah per sumber: ID dari kolom pegawai, ID dari bidang, dan ID
 * dari pangkat/golongan diambil terpisah (masing-masing memakai GIN trigram miliknya), lalu di-union
 * dan diambil {@code limit} teratas berdasarkan {@code id DESC}. Entity baru di-fetch setelahnya.
 *
 * <p>Cabang bidang/pangkat mencari ID master lebih dulu, lalu memakai {@code IN} literal. Dengan
 * begitu, kalau tidak ada master yang cocok, query pegawai tidak dijalankan sama sekali dan planner
 * tidak memindai seluruh tabel pegawai hanya untuk menemukan nol baris.
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
        if (filter.searchPattern() == null) {
            return fetchByIds(findIds(filter, cursorId, limit, null, List.of()));
        }

        Set<Long> matched = new HashSet<>(findIds(filter, cursorId, limit, null, List.of()));

        List<Long> bidangIds = findMasterIds(Bidang.class, filter.searchPattern(), "nama", "deskripsi");
        if (!bidangIds.isEmpty()) {
            matched.addAll(findIds(filter, cursorId, limit, "bidang", bidangIds));
        }

        List<Long> pangkatIds = findMasterIds(PangkatGolongan.class, filter.searchPattern(),
                "jenjang", "pangkat", "golongan", "ruang");
        if (!pangkatIds.isEmpty()) {
            matched.addAll(findIds(filter, cursorId, limit, "pangkatGolongan", pangkatIds));
        }

        List<Long> ids = matched.stream().sorted(Comparator.reverseOrder()).limit(limit).toList();
        return fetchByIds(ids);
    }

    /**
     * ID pegawai untuk satu sumber pencarian.
     *
     * @param searchRelation    {@code null} untuk kolom pegawai sendiri, atau nama relasi yang dicari
     * @param searchRelationIds ID master yang cocok; hanya dipakai kalau {@code searchRelation} diisi
     */
    private List<Long> findIds(PegawaiFilter filter, Long cursorId, int limit,
                               String searchRelation, List<Long> searchRelationIds) {
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

        if (filter.bidangUuid() != null || "bidang".equals(searchRelation)) {
            Join<Pegawai, Bidang> bidang = root.join("bidang", JoinType.INNER);
            if (filter.bidangUuid() != null) {
                predicates.add(cb.equal(bidang.get("uuid"), filter.bidangUuid()));
            }
            if ("bidang".equals(searchRelation)) {
                predicates.add(bidang.get("id").in(searchRelationIds));
            }
        }
        if (filter.pangkatGolonganUuid() != null || "pangkatGolongan".equals(searchRelation)) {
            Join<Pegawai, PangkatGolongan> pangkat = root.join("pangkatGolongan", JoinType.INNER);
            if (filter.pangkatGolonganUuid() != null) {
                predicates.add(cb.equal(pangkat.get("uuid"), filter.pangkatGolonganUuid()));
            }
            if ("pangkatGolongan".equals(searchRelation)) {
                predicates.add(pangkat.get("id").in(searchRelationIds));
            }
        }

        if (searchRelation == null && filter.searchPattern() != null) {
            String pattern = filter.searchPattern();
            predicates.add(cb.or(
                    like(cb, root.get("nama"), pattern),
                    like(cb, root.get("nip"), pattern),
                    like(cb, root.get("email"), pattern),
                    like(cb, root.get("jabatan"), pattern),
                    like(cb, root.get("peran"), pattern)
            ));
        }

        criteria.where(predicates.toArray(Predicate[]::new));
        criteria.orderBy(cb.desc(root.get("id")));
        return entityManager.createQuery(criteria).setMaxResults(limit).getResultList();
    }

    /** ID baris master yang kolom teksnya cocok dengan pola pencarian. */
    private <T> List<Long> findMasterIds(Class<T> type, String pattern, String... columns) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
        Root<T> root = criteria.from(type);
        criteria.select(root.<Long>get("id"));

        Predicate[] predicates = Arrays.stream(columns)
                .map(column -> like(cb, root.get(column), pattern))
                .toArray(Predicate[]::new);
        criteria.where(cb.or(predicates));
        return entityManager.createQuery(criteria).getResultList();
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
