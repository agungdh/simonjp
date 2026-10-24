package id.my.agungdh.simonjp.repository;

import id.my.agungdh.simonjp.domain.Pegawai;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementasi query dinamis untuk {@link PegawaiRepositoryCustom}. Predicate dibangun per filter
 * (bukan {@code :param IS NULL OR ...}) agar PostgreSQL tidak memakai generic plan yang memaksa
 * seq scan saat filter dikirim.
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Pegawai> criteria = cb.createQuery(Pegawai.class);
        Root<Pegawai> root = criteria.from(Pegawai.class);

        List<Predicate> predicates = new ArrayList<>();
        if (cursorId != null) {
            predicates.add(cb.lessThan(root.get("id"), cursorId));
        }
        if (filter.searchPattern() != null) {
            String pattern = filter.searchPattern();
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("nama")), pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(root.get("nip")), pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(root.get("email")), pattern, LIKE_ESCAPE),
                    cb.like(cb.lower(root.get("jabatan")), pattern, LIKE_ESCAPE)
            ));
        }
        if (filter.status() != null) {
            predicates.add(cb.equal(root.get("status"), filter.status()));
        }
        if (filter.bidangUuid() != null) {
            predicates.add(cb.equal(root.get("bidang").get("uuid"), filter.bidangUuid()));
        }
        if (filter.pangkatGolonganUuid() != null) {
            predicates.add(cb.equal(root.get("pangkatGolongan").get("uuid"), filter.pangkatGolonganUuid()));
        }
        if (filter.kategoriJabatan() != null) {
            predicates.add(cb.equal(root.get("kategoriJabatan"), filter.kategoriJabatan()));
        }
        if (filter.kategoriKebutuhanJamPelatihan() != null) {
            predicates.add(cb.equal(root.get("kategoriKebutuhanJamPelatihan"),
                    filter.kategoriKebutuhanJamPelatihan()));
        }

        if (!predicates.isEmpty()) {
            criteria.where(predicates.toArray(Predicate[]::new));
        }
        criteria.orderBy(cb.desc(root.get("id")));

        TypedQuery<Pegawai> query = entityManager.createQuery(criteria);
        // open-in-view dimatikan; relasi response di-fetch sekaligus supaya tidak N+1.
        query.setHint("jakarta.persistence.fetchgraph", fetchGraph());
        query.setMaxResults(limit);
        return query.getResultList();
    }

    private EntityGraph<Pegawai> fetchGraph() {
        EntityGraph<Pegawai> graph = entityManager.createEntityGraph(Pegawai.class);
        graph.addAttributeNodes("bidang", "pangkatGolongan", "account");
        return graph;
    }
}
