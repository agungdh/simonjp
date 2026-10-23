package id.my.agungdh.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import id.my.agungdh.domain.Lc;
import id.my.agungdh.domain.Pegawai;
import id.my.agungdh.dto.LcRequest;
import id.my.agungdh.dto.LcResponse;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.mapper.LcMapper;
import id.my.agungdh.repository.LcFilter;
import id.my.agungdh.repository.LcRepository;
import id.my.agungdh.repository.PegawaiRepository;
import id.my.agungdh.repository.QuerySupport;
import id.my.agungdh.service.exception.BadRequestException;
import id.my.agungdh.service.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class LcService {

    private final LcRepository repository;
    private final PegawaiRepository pegawaiRepository;
    private final LcMapper mapper;

    @Transactional
    public CursorPageDTO<LcResponse> list(
            String search,
            UUID pegawaiUuid,
            java.time.LocalDate dariTanggal,
            java.time.LocalDate sampaiTanggal,
            Integer tahun,
            UUID cursor,
            int limit) {

        LcFilter filter = new LcFilter(
                CursorSupport.normalizeSearch(search),
                pegawaiUuid,
                dariTanggal,
                sampaiTanggal,
                tahun);
        int size = CursorSupport.normalizeLimit(limit);

        Long cursorId = null;
        if (cursor != null) {
            cursorId = repository.findFilteredIdByUuid(cursor, filter)
                    .orElseThrow(() -> new BadRequestException("Cursor tidak valid."));
        }

        var rows = repository.list(filter, cursorId, size + 1);
        var page = CursorSupport.page(rows, size);

        return new CursorPageDTO<>(
                page.items().stream().map(mapper::toResponse).toList(),
                page.nextCursor(),
                page.hasMore());
    }

    @Transactional
    public LcResponse get(UUID uuid) {
        return mapper.toResponse(find(uuid));
    }

    @Transactional
    public LcResponse create(LcRequest request) {
        Lc entity = mapper.toEntity(request);
        entity.setPegawai(resolvePegawai(request.pegawaiUuid()));
        entity.setMateriPengembangan(require(request.materiPengembangan(), "materiPengembangan"));
        entity.setTanggalPelaksanaan(request.tanggalPelaksanaan());
        entity.setJumlahJam(request.jumlahJam());
        repository.persist(entity);

        return mapper.toResponse(entity);
    }

    @Transactional
    public LcResponse update(UUID uuid, LcRequest request) {
        Lc entity = find(uuid);
        mapper.updateEntity(request, entity);
        entity.setPegawai(resolvePegawai(request.pegawaiUuid()));
        entity.setMateriPengembangan(require(request.materiPengembangan(), "materiPengembangan"));
        entity.setTanggalPelaksanaan(request.tanggalPelaksanaan());
        entity.setJumlahJam(request.jumlahJam());

        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID uuid) {
        Lc entity = find(uuid);
        entity.setDeletedAt(OffsetDateTime.now());
    }

    private Lc find(UUID uuid) {
        return repository.findActiveByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Lc tidak ditemukan."));
    }

    private Pegawai resolvePegawai(UUID uuid) {
        return pegawaiRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new BadRequestException("Pegawai tidak ditemukan."));
    }

    private String require(String value, String field) {
        String normalized = QuerySupport.trimToNull(value);
        if (normalized == null) {
            throw new BadRequestException(field + " wajib diisi.");
        }
        return normalized;
    }
}
