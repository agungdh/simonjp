package id.my.agungdh.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import id.my.agungdh.domain.PangkatGolongan;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ApplicationScoped
public class PangkatGolonganRepository {

    private final EntityManager em;

    @Inject
    public PangkatGolonganRepository(EntityManager em) {
        this.em = em;
    }

    public void persist(PangkatGolongan pangkatGolongan) {
        em.persist(pangkatGolongan);
    }

    public Optional<PangkatGolongan> findActiveByUuid(UUID uuid) {
        return em.createQuery("select pg from PangkatGolongan pg where pg.uuid = :uuid order by pg.id", PangkatGolongan.class)
                .setParameter("uuid", uuid)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    public List<PangkatGolongan> list(String search, Long cursorId, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PangkatGolongan> cq = cb.createQuery(PangkatGolongan.class);
        Root<PangkatGolongan> root = cq.from(PangkatGolongan.class);

        List<Predicate> predicates = new ArrayList<>();
        addSearchPredicate(cb, root, predicates, search);
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
        Root<PangkatGolongan> root = cq.from(PangkatGolongan.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("uuid"), uuid));
        addSearchPredicate(cb, root, predicates, search);

        cq.select(root.get("id")).where(predicates.toArray(Predicate[]::new)).orderBy(cb.asc(root.get("id")));

        return em.createQuery(cq).setMaxResults(1).getResultList().stream().findFirst();
    }

    public boolean existsByKode(String golongan, String ruang, Long excludeId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PangkatGolongan> root = cq.from(PangkatGolongan.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(cb.lower(root.get("golongan")), golongan.toLowerCase(Locale.ROOT)));
        predicates.add(cb.equal(cb.lower(root.get("ruang")), ruang.toLowerCase(Locale.ROOT)));
        if (excludeId != null) {
            predicates.add(cb.notEqual(root.get("id"), excludeId));
        }

        cq.select(cb.count(root)).where(predicates.toArray(Predicate[]::new));

        return em.createQuery(cq).getSingleResult() > 0;
    }

    public boolean isUsedByActivePegawai(Long pangkatGolonganId) {
        return em.createQuery("select count(p) from Pegawai p where p.pangkatGolongan.id = :id", Long.class)
                .setParameter("id", pangkatGolonganId)
                .getSingleResult() > 0;
    }

    private void addSearchPredicate(CriteriaBuilder cb, Root<PangkatGolongan> root, List<Predicate> predicates, String search) {
        if (search == null || search.isBlank()) {
            return;
        }

        String pattern = QuerySupport.likePattern(search);
        predicates.add(cb.or(
                cb.like(cb.lower(root.get("jenjang")), pattern, QuerySupport.LIKE_ESCAPE),
                cb.like(cb.lower(root.get("pangkat")), pattern, QuerySupport.LIKE_ESCAPE),
                cb.like(cb.lower(root.get("golongan")), pattern, QuerySupport.LIKE_ESCAPE),
                cb.like(cb.lower(root.get("ruang")), pattern, QuerySupport.LIKE_ESCAPE)));
    }
}
