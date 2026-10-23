package id.my.agungdh.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import id.my.agungdh.domain.Pegawai;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PegawaiRepository implements PanacheRepository<Pegawai> {

    public Optional<Pegawai> findActiveByUuid(UUID uuid) {
        String hql = "from Pegawai p join fetch p.bidang b left join fetch p.pangkatGolongan pg "
                + "where p.uuid = :uuid order by p.id";
        return find(hql, Map.of("uuid", uuid)).firstResultOptional();
    }

    public List<Pegawai> list(PegawaiFilter filter, Long cursorId, int limit) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (filter != null) {
            if (filter.search() != null && !filter.search().isBlank()) {
                String pattern = QuerySupport.likePattern(filter.search());
                conditions.add("(lower(p.nama) like :search escape '\\' or lower(p.nip) like :search escape '\\')");
                params.put("search", pattern);
            }
            if (filter.tipe() != null) {
                conditions.add("p.tipe = :tipe");
                params.put("tipe", filter.tipe());
            }
            if (filter.status() != null) {
                conditions.add("p.status = :status");
                params.put("status", filter.status());
            }
            if (filter.bidangUuid() != null) {
                conditions.add("p.bidang.uuid = :bidangUuid");
                params.put("bidangUuid", filter.bidangUuid());
            }
            if (filter.pangkatGolonganUuid() != null) {
                conditions.add("p.pangkatGolongan.uuid = :pangkatGolonganUuid");
                params.put("pangkatGolonganUuid", filter.pangkatGolonganUuid());
            }
            if (filter.kategoriJabatan() != null) {
                conditions.add("p.kategoriJabatan = :kategoriJabatan");
                params.put("kategoriJabatan", filter.kategoriJabatan());
            }
            if (filter.kategoriKebutuhanJamPelatihan() != null) {
                conditions.add("p.kategoriKebutuhanJamPelatihan = :kategoriKebutuhanJamPelatihan");
                params.put("kategoriKebutuhanJamPelatihan", filter.kategoriKebutuhanJamPelatihan());
            }
        }
        if (cursorId != null) {
            conditions.add("p.id < :cursorId");
            params.put("cursorId", cursorId);
        }

        String where = String.join(" and ", conditions);
        String hql = "from Pegawai p join fetch p.bidang b left join fetch p.pangkatGolongan pg"
                + (where.isEmpty() ? "" : " where " + where)
                + " order by p.id desc";

        return find(hql, params).page(Page.ofSize(limit)).list();
    }

    /** Cursor hanya valid kalau barisnya masih cocok dengan filter yang sedang aktif. */
    public Optional<Long> findFilteredIdByUuid(UUID uuid, PegawaiFilter filter) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        conditions.add("p.uuid = :uuid");
        params.put("uuid", uuid);

        if (filter != null) {
            if (filter.search() != null && !filter.search().isBlank()) {
                String pattern = QuerySupport.likePattern(filter.search());
                conditions.add("(lower(p.nama) like :search escape '\\' or lower(p.nip) like :search escape '\\')");
                params.put("search", pattern);
            }
            if (filter.tipe() != null) {
                conditions.add("p.tipe = :tipe");
                params.put("tipe", filter.tipe());
            }
            if (filter.status() != null) {
                conditions.add("p.status = :status");
                params.put("status", filter.status());
            }
            if (filter.bidangUuid() != null) {
                conditions.add("p.bidang.uuid = :bidangUuid");
                params.put("bidangUuid", filter.bidangUuid());
            }
            if (filter.pangkatGolonganUuid() != null) {
                conditions.add("p.pangkatGolongan.uuid = :pangkatGolonganUuid");
                params.put("pangkatGolonganUuid", filter.pangkatGolonganUuid());
            }
            if (filter.kategoriJabatan() != null) {
                conditions.add("p.kategoriJabatan = :kategoriJabatan");
                params.put("kategoriJabatan", filter.kategoriJabatan());
            }
            if (filter.kategoriKebutuhanJamPelatihan() != null) {
                conditions.add("p.kategoriKebutuhanJamPelatihan = :kategoriKebutuhanJamPelatihan");
                params.put("kategoriKebutuhanJamPelatihan", filter.kategoriKebutuhanJamPelatihan());
            }
        }

        String where = String.join(" and ", conditions);
        String hql = "from Pegawai p where " + where + " order by p.id";
        return find(hql, params).firstResultOptional().map(p -> p.getId());
    }

    public boolean existsByNip(String nip, Long excludeId) {
        if (excludeId == null) {
            return count("nip = ?1", nip) > 0;
        }
        return count("nip = ?1 and id <> ?2", nip, excludeId) > 0;
    }

    public boolean existsActiveByBidangId(Long bidangId) {
        return Pegawai.count("bidang.id = ?1", bidangId) > 0;
    }

    public boolean existsActiveByPangkatGolonganId(Long pangkatGolonganId) {
        return Pegawai.count("pangkatGolongan.id = ?1", pangkatGolonganId) > 0;
    }
}