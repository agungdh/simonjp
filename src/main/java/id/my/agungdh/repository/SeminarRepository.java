package id.my.agungdh.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import id.my.agungdh.domain.Seminar;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SeminarRepository implements PanacheRepository<Seminar> {

    public Optional<Seminar> findActiveByUuid(UUID uuid) {
        return find("uuid = :uuid order by id", Map.of("uuid", uuid)).firstResultOptional();
    }

    public List<Seminar> list(SeminarFilter filter, Long cursorId, int limit) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new java.util.HashMap<>();

        if (filter != null) {
            if (filter.search() != null && !filter.search().isBlank()) {
                conditions.add("lower(materiPengembangan) like :search escape '\\'");
                params.put("search", QuerySupport.likePattern(filter.search()));
            }
            if (filter.pegawaiUuid() != null) {
                conditions.add("pegawai.uuid = :pegawaiUuid");
                params.put("pegawaiUuid", filter.pegawaiUuid());
            }
            if (filter.dariTanggal() != null) {
                conditions.add("tanggalPelaksanaan >= :dari");
                params.put("dari", filter.dariTanggal());
            }
            if (filter.sampaiTanggal() != null) {
                conditions.add("tanggalPelaksanaan <= :sampai");
                params.put("sampai", filter.sampaiTanggal());
            }
            if (filter.tahun() != null) {
                conditions.add("EXTRACT(YEAR FROM tanggalPelaksanaan) = :tahun");
                params.put("tahun", filter.tahun());
            }
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

    public Optional<Long> findFilteredIdByUuid(UUID uuid, SeminarFilter filter) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new java.util.HashMap<>();

        conditions.add("uuid = :uuid");
        params.put("uuid", uuid);

        if (filter != null) {
            if (filter.search() != null && !filter.search().isBlank()) {
                conditions.add("lower(materiPengembangan) like :search escape '\\'");
                params.put("search", QuerySupport.likePattern(filter.search()));
            }
            if (filter.pegawaiUuid() != null) {
                conditions.add("pegawai.uuid = :pegawaiUuid");
                params.put("pegawaiUuid", filter.pegawaiUuid());
            }
            if (filter.dariTanggal() != null) {
                conditions.add("tanggalPelaksanaan >= :dari");
                params.put("dari", filter.dariTanggal());
            }
            if (filter.sampaiTanggal() != null) {
                conditions.add("tanggalPelaksanaan <= :sampai");
                params.put("sampai", filter.sampaiTanggal());
            }
            if (filter.tahun() != null) {
                conditions.add("EXTRACT(YEAR FROM tanggalPelaksanaan) = :tahun");
                params.put("tahun", filter.tahun());
            }
        }

        String where = String.join(" and ", conditions);
        return find(where + " order by id", params).firstResultOptional().map(e -> e.getId());
    }
}
