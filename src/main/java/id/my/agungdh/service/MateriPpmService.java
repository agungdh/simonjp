package id.my.agungdh.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import id.my.agungdh.domain.MateriPpm;
import id.my.agungdh.dto.MateriPpmRequest;
import id.my.agungdh.dto.MateriPpmResponse;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.mapper.MateriPpmMapper;
import id.my.agungdh.repository.MateriPpmFilter;
import id.my.agungdh.repository.MateriPpmRepository;
import id.my.agungdh.repository.QuerySupport;
import id.my.agungdh.service.exception.BadRequestException;
import id.my.agungdh.service.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MateriPpmService {

    private final MateriPpmRepository repository;
    private final MateriPpmMapper mapper;

    @Transactional
    public CursorPageDTO<MateriPpmResponse> list(
            String search,
            java.time.LocalDate dariTanggal,
            java.time.LocalDate sampaiTanggal,
            Integer tahun,
            UUID cursor,
            int limit) {

        MateriPpmFilter filter = new MateriPpmFilter(
                CursorSupport.normalizeSearch(search),
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
    public MateriPpmResponse get(UUID uuid) {
        return mapper.toResponse(find(uuid));
    }

    @Transactional
    public MateriPpmResponse create(MateriPpmRequest request) {
        MateriPpm entity = mapper.toEntity(request);
        entity.setNomorSurat(QuerySupport.trimToNull(request.nomorSurat()));
        entity.setNamaPemateri(QuerySupport.trimToNull(request.namaPemateri()));
        entity.setMateriPengembangan(require(request.materiPengembangan(), "materiPengembangan"));
        entity.setTanggalPelaksanaan(request.tanggalPelaksanaan());
        entity.setLinkMateri(QuerySupport.trimToNull(request.linkMateri()));
        entity.setLinkDokumentasi(QuerySupport.trimToNull(request.linkDokumentasi()));
        repository.persist(entity);

        return mapper.toResponse(entity);
    }

    @Transactional
    public MateriPpmResponse update(UUID uuid, MateriPpmRequest request) {
        MateriPpm entity = find(uuid);
        mapper.updateEntity(request, entity);
        entity.setNomorSurat(QuerySupport.trimToNull(request.nomorSurat()));
        entity.setNamaPemateri(QuerySupport.trimToNull(request.namaPemateri()));
        entity.setMateriPengembangan(require(request.materiPengembangan(), "materiPengembangan"));
        entity.setTanggalPelaksanaan(request.tanggalPelaksanaan());
        entity.setLinkMateri(QuerySupport.trimToNull(request.linkMateri()));
        entity.setLinkDokumentasi(QuerySupport.trimToNull(request.linkDokumentasi()));

        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID uuid) {
        MateriPpm entity = find(uuid);
        entity.setDeletedAt(OffsetDateTime.now());
    }

    private MateriPpm find(UUID uuid) {
        return repository.findActiveByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("MateriPpm tidak ditemukan."));
    }

    private String require(String value, String field) {
        String normalized = QuerySupport.trimToNull(value);
        if (normalized == null) {
            throw new BadRequestException(field + " wajib diisi.");
        }
        return normalized;
    }
}
