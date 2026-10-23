package id.my.agungdh.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import id.my.agungdh.domain.Seminar;
import id.my.agungdh.domain.Pegawai;
import id.my.agungdh.dto.SeminarRequest;
import id.my.agungdh.dto.SeminarResponse;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.mapper.SeminarMapper;
import id.my.agungdh.repository.SeminarFilter;
import id.my.agungdh.repository.SeminarRepository;
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
public class SeminarService {

    private final SeminarRepository repository;
    private final PegawaiRepository pegawaiRepository;
    private final SeminarMapper mapper;

    @Transactional
    public CursorPageDTO<SeminarResponse> list(
            String search,
            UUID pegawaiUuid,
            java.time.LocalDate dariTanggal,
            java.time.LocalDate sampaiTanggal,
            Integer tahun,
            UUID cursor,
            int limit) {

        SeminarFilter filter = new SeminarFilter(
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
    public SeminarResponse get(UUID uuid) {
        return mapper.toResponse(find(uuid));
    }

    @Transactional
    public SeminarResponse create(SeminarRequest request) {
        Seminar entity = mapper.toEntity(request);
        entity.setPegawai(resolvePegawai(request.pegawaiUuid()));
        entity.setMateriPengembangan(require(request.materiPengembangan(), "materiPengembangan"));
        entity.setTanggalPelaksanaan(request.tanggalPelaksanaan());
        entity.setJumlahJam(request.jumlahJam());
        repository.persist(entity);

        return mapper.toResponse(entity);
    }

    @Transactional
    public SeminarResponse update(UUID uuid, SeminarRequest request) {
        Seminar entity = find(uuid);
        mapper.updateEntity(request, entity);
        entity.setPegawai(resolvePegawai(request.pegawaiUuid()));
        entity.setMateriPengembangan(require(request.materiPengembangan(), "materiPengembangan"));
        entity.setTanggalPelaksanaan(request.tanggalPelaksanaan());
        entity.setJumlahJam(request.jumlahJam());

        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID uuid) {
        Seminar entity = find(uuid);
        entity.setDeletedAt(OffsetDateTime.now());
    }

    private Seminar find(UUID uuid) {
        return repository.findActiveByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Seminar tidak ditemukan."));
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
