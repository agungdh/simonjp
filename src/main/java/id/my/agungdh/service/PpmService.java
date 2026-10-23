package id.my.agungdh.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import id.my.agungdh.domain.Pegawai;
import id.my.agungdh.domain.Ppm;
import id.my.agungdh.dto.PpmRequest;
import id.my.agungdh.dto.PpmResponse;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.mapper.PpmMapper;
import id.my.agungdh.repository.PegawaiRepository;
import id.my.agungdh.repository.PpmFilter;
import id.my.agungdh.repository.PpmRepository;
import id.my.agungdh.repository.QuerySupport;
import id.my.agungdh.service.exception.BadRequestException;
import id.my.agungdh.service.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PpmService {

    private final PpmRepository repository;
    private final PegawaiRepository pegawaiRepository;
    private final PpmMapper mapper;

    @Transactional
    public CursorPageDTO<PpmResponse> list(
            String search,
            UUID pegawaiUuid,
            java.time.LocalDate dariTanggal,
            java.time.LocalDate sampaiTanggal,
            Integer tahun,
            UUID cursor,
            int limit) {

        PpmFilter filter = new PpmFilter(
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
    public PpmResponse get(UUID uuid) {
        return mapper.toResponse(find(uuid));
    }

    @Transactional
    public PpmResponse create(PpmRequest request) {
        Ppm entity = mapper.toEntity(request);
        entity.setPegawai(resolvePegawai(request.pegawaiUuid()));
        entity.setNomorSurat(require(request.nomorSurat(), "nomorSurat"));
        entity.setMateriPengembangan(require(request.materiPengembangan(), "materiPengembangan"));
        entity.setTanggalPelaksanaan(request.tanggalPelaksanaan());
        entity.setJumlahJamPelatihan(request.jumlahJamPelatihan());
        repository.persist(entity);

        return mapper.toResponse(entity);
    }

    @Transactional
    public PpmResponse update(UUID uuid, PpmRequest request) {
        Ppm entity = find(uuid);
        mapper.updateEntity(request, entity);
        entity.setPegawai(resolvePegawai(request.pegawaiUuid()));
        entity.setNomorSurat(require(request.nomorSurat(), "nomorSurat"));
        entity.setMateriPengembangan(require(request.materiPengembangan(), "materiPengembangan"));
        entity.setTanggalPelaksanaan(request.tanggalPelaksanaan());
        entity.setJumlahJamPelatihan(request.jumlahJamPelatihan());

        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID uuid) {
        Ppm entity = find(uuid);
        entity.setDeletedAt(OffsetDateTime.now());
    }

    private Ppm find(UUID uuid) {
        return repository.findActiveByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Ppm tidak ditemukan."));
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
