package id.my.agungdh.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import id.my.agungdh.domain.Bidang;
import id.my.agungdh.domain.Pegawai;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BidangRepository implements PanacheRepository<Bidang> {

    public Optional<Bidang> findActiveByUuid(UUID uuid) {
        return find("uuid = :uuid order by id", Map.of("uuid", uuid)).firstResultOptional();
    }

    public List<Bidang> list(String search, Long cursorId, int limit) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new java.util.HashMap<>();

        if (search != null && !search.isBlank()) {
            conditions.add("lower(bidang) like :search escape '\\'");
            params.put("search", QuerySupport.likePattern(search));
        }
        if (cursorId != null) {
            conditions.add("id < :cursorId");
            params.put("cursorId", cursorId);
        }

        String where = String.join(" and ", conditions);
        String query = where.isEmpty() ? null : where + " order by id desc";

        if (query == null) {
            return findAll(Sort.descending("id")).page(Page.ofSize(limit)).list();
        }
        return find(query, params).page(Page.ofSize(limit)).list();
    }

    /** Cursor hanya valid kalau barisnya masih cocok dengan filter yang sedang aktif. */
    public Optional<Long> findFilteredIdByUuid(UUID uuid, String search) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new java.util.HashMap<>();

        conditions.add("uuid = :uuid");
        params.put("uuid", uuid);

        if (search != null && !search.isBlank()) {
            conditions.add("lower(bidang) like :search escape '\\'");
            params.put("search", QuerySupport.likePattern(search));
        }

        String where = String.join(" and ", conditions);
        return find(where + " order by id", params).firstResultOptional().map(b -> b.getId());
    }

    public boolean existsByNama(String nama, Long excludeId) {
        if (excludeId == null) {
            return count("lower(bidang) = ?1", nama.toLowerCase(Locale.ROOT)) > 0;
        }
        return count("lower(bidang) = ?1 and id <> ?2", nama.toLowerCase(Locale.ROOT), excludeId) > 0;
    }

    public boolean isUsedByActivePegawai(Long bidangId) {
        return Pegawai.count("bidang.id = ?1", bidangId) > 0;
    }
}