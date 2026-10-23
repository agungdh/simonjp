package id.my.agungdh.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import id.my.agungdh.domain.AuditListener;
import id.my.agungdh.domain.PangkatGolongan;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.dto.PangkatGolonganRequest;
import id.my.agungdh.dto.PangkatGolonganResponse;
import id.my.agungdh.mapper.PangkatGolonganMapper;
import id.my.agungdh.repository.PangkatGolonganRepository;
import id.my.agungdh.repository.QuerySupport;
import id.my.agungdh.service.exception.BadRequestException;
import id.my.agungdh.service.exception.ConflictException;
import id.my.agungdh.service.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PangkatGolonganService {

    private final PangkatGolonganRepository pangkatGolonganRepository;
    private final PangkatGolonganMapper mapper;

    @Inject
    public PangkatGolonganService(PangkatGolonganRepository pangkatGolonganRepository, PangkatGolonganMapper mapper) {
        this.pangkatGolonganRepository = pangkatGolonganRepository;
        this.mapper = mapper;
    }

    @Transactional
    public CursorPageDTO<PangkatGolonganResponse> list(String search, UUID cursor, int limit) {
        String normalizedSearch = CursorSupport.normalizeSearch(search);
        int size = CursorSupport.normalizeLimit(limit);

        Long cursorId = null;
        if (cursor != null) {
            cursorId = pangkatGolonganRepository.findFilteredIdByUuid(cursor, normalizedSearch)
                    .orElseThrow(() -> new BadRequestException("Cursor tidak valid."));
        }

        var rows = pangkatGolonganRepository.list(normalizedSearch, cursorId, size + 1);
        var page = CursorSupport.page(rows, size, PangkatGolongan::getUuid);

        return new CursorPageDTO<>(
                page.items().stream().map(mapper::toResponse).toList(),
                page.nextCursor(),
                page.hasMore());
    }

    @Transactional
    public PangkatGolonganResponse get(UUID uuid) {
        return mapper.toResponse(find(uuid));
    }

    @Transactional
    public PangkatGolonganResponse create(PangkatGolonganRequest request) {
        PangkatGolonganRequest normalized = normalize(request);
        if (pangkatGolonganRepository.existsByKode(normalized.golongan(), normalized.ruang(), null)) {
            throw new ConflictException("Pangkat golongan sudah ada.");
        }

        PangkatGolongan entity = mapper.toEntity(normalized);
        pangkatGolonganRepository.persist(entity);

        return mapper.toResponse(entity);
    }

    @Transactional
    public PangkatGolonganResponse update(UUID uuid, PangkatGolonganRequest request) {
        PangkatGolongan entity = find(uuid);
        PangkatGolonganRequest normalized = normalize(request);

        if (pangkatGolonganRepository.existsByKode(normalized.golongan(), normalized.ruang(), entity.getId())) {
            throw new ConflictException("Pangkat golongan sudah ada.");
        }

        mapper.updateEntity(normalized, entity);

        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID uuid) {
        PangkatGolongan entity = find(uuid);

        if (pangkatGolonganRepository.isUsedByActivePegawai(entity.getId())) {
            throw new ConflictException("Pangkat golongan masih dipakai pegawai aktif.");
        }

        entity.setDeletedAt(OffsetDateTime.now());
        entity.setDeletedBy(AuditListener.SYSTEM_USER_ID);
    }

    private PangkatGolongan find(UUID uuid) {
        return pangkatGolonganRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Pangkat golongan tidak ditemukan."));
    }

    private PangkatGolonganRequest normalize(PangkatGolonganRequest request) {
        return new PangkatGolonganRequest(
                required(request.jenjang(), "jenjang"),
                required(request.pangkat(), "pangkat"),
                required(request.golongan(), "golongan"),
                required(request.ruang(), "ruang"));
    }

    private String required(String value, String field) {
        String normalized = QuerySupport.trimToNull(value);
        if (normalized == null) {
            throw new BadRequestException(field + " wajib diisi.");
        }
        return normalized;
    }
}
