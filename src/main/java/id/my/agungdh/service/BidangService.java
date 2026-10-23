package id.my.agungdh.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import id.my.agungdh.domain.AuditListener;
import id.my.agungdh.domain.Bidang;
import id.my.agungdh.dto.BidangRequest;
import id.my.agungdh.dto.BidangResponse;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.mapper.BidangMapper;
import id.my.agungdh.repository.BidangRepository;
import id.my.agungdh.repository.QuerySupport;
import id.my.agungdh.service.exception.BadRequestException;
import id.my.agungdh.service.exception.ConflictException;
import id.my.agungdh.service.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class BidangService {

    private final BidangRepository bidangRepository;
    private final BidangMapper mapper;

    @Inject
    public BidangService(BidangRepository bidangRepository, BidangMapper mapper) {
        this.bidangRepository = bidangRepository;
        this.mapper = mapper;
    }

    @Transactional
    public CursorPageDTO<BidangResponse> list(String search, UUID cursor, int limit) {
        String normalizedSearch = CursorSupport.normalizeSearch(search);
        int size = CursorSupport.normalizeLimit(limit);

        Long cursorId = null;
        if (cursor != null) {
            cursorId = bidangRepository.findFilteredIdByUuid(cursor, normalizedSearch)
                    .orElseThrow(() -> new BadRequestException("Cursor tidak valid."));
        }

        var rows = bidangRepository.list(normalizedSearch, cursorId, size + 1);
        var page = CursorSupport.page(rows, size);

        return new CursorPageDTO<>(
                page.items().stream().map(mapper::toResponse).toList(),
                page.nextCursor(),
                page.hasMore());
    }

    @Transactional
    public BidangResponse get(UUID uuid) {
        return mapper.toResponse(find(uuid));
    }

    @Transactional
    public BidangResponse create(BidangRequest request) {
        String nama = requireNama(request);
        if (bidangRepository.existsByNama(nama, null)) {
            throw new ConflictException("Bidang sudah ada.");
        }

        Bidang entity = mapper.toEntity(request);
        entity.setBidang(nama);
        bidangRepository.persist(entity);

        return mapper.toResponse(entity);
    }

    @Transactional
    public BidangResponse update(UUID uuid, BidangRequest request) {
        Bidang entity = find(uuid);
        String nama = requireNama(request);

        if (bidangRepository.existsByNama(nama, entity.getId())) {
            throw new ConflictException("Bidang sudah ada.");
        }

        mapper.updateEntity(request, entity);
        entity.setBidang(nama);

        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID uuid) {
        Bidang entity = find(uuid);

        if (bidangRepository.isUsedByActivePegawai(entity.getId())) {
            throw new ConflictException("Bidang masih dipakai pegawai aktif.");
        }

        entity.setDeletedAt(OffsetDateTime.now());
        entity.setDeletedBy(AuditListener.SYSTEM_USER_ID);
    }

    private Bidang find(UUID uuid) {
        return bidangRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Bidang tidak ditemukan."));
    }

    private String requireNama(BidangRequest request) {
        String nama = QuerySupport.trimToNull(request.bidang());
        if (nama == null) {
            throw new BadRequestException("bidang wajib diisi.");
        }
        return nama;
    }
}
