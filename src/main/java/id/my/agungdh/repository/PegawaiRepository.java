package id.my.agungdh.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import id.my.agungdh.domain.Pegawai;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ApplicationScoped
public class PegawaiRepository {

    private final EntityManager em;

    @Inject
    public PegawaiRepository(EntityManager em) {
        this.em = em;
    }

    public void persist(Pegawai pegawai) {
        em.persist(pegawai);
    }

    public Optional<Pegawai> findActiveByUuid(UUID uuid) {
        return em.createQuery("""
                select p from Pegawai p
                join fetch p.bidang
                left join fetch p.pangkatGolongan
                where p.uuid = :uuid
                order by p.id
                """, Pegawai.class)
                .setParameter("uuid", uuid)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    public List<Pegawai> list(PegawaiFilter filter, Long cursorId, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Pegawai> cq = cb.createQuery(Pegawai.class);
        Root<Pegawai> root = cq.from(Pegawai.class);
        root.fetch("bidang", JoinType.INNER);
        root.fetch("pangkatGolongan", JoinType.LEFT);

        List<Predicate> predicates = predicates(cb, root, filter);
        if (cursorId != null) {
            predicates.add(cb.lessThan(root.get("id"), cursorId));
        }

        cq.select(root).where(predicates.toArray(Predicate[]::new)).orderBy(cb.desc(root.get("id")));

        return em.createQuery(cq).setMaxResults(limit).getResultList();
    }

    /** Cursor hanya valid kalau barisnya masih cocok dengan filter yang sedang aktif. */
    public Optional<Long> findFilteredIdByUuid(UUID uuid, PegawaiFilter filter) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Pegawai> root = cq.from(Pegawai.class);

        List<Predicate> predicates = predicates(cb, root, filter);
        predicates.add(cb.equal(root.get("uuid"), uuid));

        cq.select(root.get("id")).where(predicates.toArray(Predicate[]::new)).orderBy(cb.asc(root.get("id")));

        return em.createQuery(cq).setMaxResults(1).getResultList().stream().findFirst();
    }

    public boolean existsByNip(String nip, Long excludeId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Pegawai> root = cq.from(Pegawai.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("nip"), nip));
        if (excludeId != null) {
            predicates.add(cb.notEqual(root.get("id"), excludeId));
        }

        cq.select(cb.count(root)).where(predicates.toArray(Predicate[]::new));

        return em.createQuery(cq).getSingleResult() > 0;
    }

    public boolean existsActiveByBidangId(Long bidangId) {
        return em.createQuery("select count(p) from Pegawai p where p.bidang.id = :bidangId", Long.class)
                .setParameter("bidangId", bidangId)
                .getSingleResult() > 0;
    }

    public boolean existsActiveByPangkatGolonganId(Long pangkatGolonganId) {
        return em.createQuery("select count(p) from Pegawai p where p.pangkatGolongan.id = :id", Long.class)
                .setParameter("id", pangkatGolonganId)
                .getSingleResult() > 0;
    }

    private List<Predicate> predicates(CriteriaBuilder cb, Root<Pegawai> root, PegawaiFilter filter) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter == null) {
            return predicates;
        }

        if (filter.search() != null && !filter.search().isBlank()) {
            String pattern = QuerySupport.likePattern(filter.search());
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("nama")), pattern, QuerySupport.LIKE_ESCAPE),
                    cb.like(cb.lower(root.get("nip")), pattern, QuerySupport.LIKE_ESCAPE)));
        }
        if (filter.tipe() != null) {
            predicates.add(cb.equal(root.get("tipe"), filter.tipe()));
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
            predicates.add(cb.equal(root.get("kategoriKebutuhanJamPelatihan"), filter.kategoriKebutuhanJamPelatihan()));
        }

        return predicates;
    }
}
