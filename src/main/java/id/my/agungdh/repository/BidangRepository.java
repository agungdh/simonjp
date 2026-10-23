package id.my.agungdh.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import id.my.agungdh.domain.Bidang;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ApplicationScoped
public class BidangRepository {

    private final EntityManager em;

    @Inject
    public BidangRepository(EntityManager em) {
        this.em = em;
    }

    public void persist(Bidang bidang) {
        em.persist(bidang);
    }

    public Optional<Bidang> findActiveByUuid(UUID uuid) {        return em.createQuery("select b from Bidang b where b.uuid = :uuid order by b.id", Bidang.class)
                .setParameter("uuid", uuid)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    public List<Bidang> list(String search, Long cursorId, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Bidang> cq = cb.createQuery(Bidang.class);
        Root<Bidang> root = cq.from(Bidang.class);

        List<Predicate> predicates = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("bidang")), QuerySupport.likePattern(search), QuerySupport.LIKE_ESCAPE));
        }
        if (cursorId != null) {
            predicates.add(cb.lessThan(root.get("id"), cursorId));
        }

        cq.where(predicates.toArray(Predicate[]::new));
        cq.orderBy(cb.desc(root.get("id")));

        return em.createQuery(cq).setMaxResults(limit).getResultList();
    }

    /** Cursor hanya valid kalau barisnya masih cocok dengan filter yang sedang aktif. */
    public Optional<Long> findFilteredIdByUuid(UUID uuid, String search) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Bidang> root = cq.from(Bidang.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("uuid"), uuid));
        if (search != null && !search.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("bidang")), QuerySupport.likePattern(search), QuerySupport.LIKE_ESCAPE));
        }

        cq.select(root.get("id")).where(predicates.toArray(Predicate[]::new)).orderBy(cb.asc(root.get("id")));

        return em.createQuery(cq).setMaxResults(1).getResultList().stream().findFirst();
    }

    public boolean existsByNama(String nama, Long excludeId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Bidang> root = cq.from(Bidang.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(cb.lower(root.get("bidang")), nama.toLowerCase(Locale.ROOT)));
        if (excludeId != null) {
            predicates.add(cb.notEqual(root.get("id"), excludeId));
        }

        cq.select(cb.count(root)).where(predicates.toArray(Predicate[]::new));

        return em.createQuery(cq).getSingleResult() > 0;
    }

    public boolean isUsedByActivePegawai(Long bidangId) {
        return em.createQuery("select count(p) from Pegawai p where p.bidang.id = :bidangId", Long.class)
                .setParameter("bidangId", bidangId)
                .getSingleResult() > 0;
    }
}
