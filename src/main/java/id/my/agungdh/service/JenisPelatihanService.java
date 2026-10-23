package id.my.agungdh.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import id.my.agungdh.domain.JenisPelatihan;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.dto.JenisPelatihanRequest;
import id.my.agungdh.dto.JenisPelatihanResponse;
import id.my.agungdh.mapper.JenisPelatihanMapper;
import id.my.agungdh.repository.JenisPelatihanRepository;
import id.my.agungdh.repository.QuerySupport;
import id.my.agungdh.service.exception.BadRequestException;
import id.my.agungdh.service.exception.ConflictException;
import id.my.agungdh.service.exception.NotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class JenisPelatihanService {

    private final JenisPelatihanRepository jenisPelatihanRepository;
    private final JenisPelatihanMapper mapper;

    @Transactional
    public CursorPageDTO<JenisPelatihanResponse> list(String search, UUID cursor, int limit) {
        String normalizedSearch = CursorSupport.normalizeSearch(search);
        int size = CursorSupport.normalizeLimit(limit);

        Long cursorId = null;
        if (cursor != null) {
            cursorId = jenisPelatihanRepository.findFilteredIdByUuid(cursor, normalizedSearch)
                    .orElseThrow(() -> new BadRequestException("Cursor tidak valid."));
        }

        var rows = jenisPelatihanRepository.list(normalizedSearch, cursorId, size + 1);
        var page = CursorSupport.page(rows, size);

        return new CursorPageDTO<>(
                page.items().stream().map(mapper::toResponse).toList(),
                page.nextCursor(),
                page.hasMore());
    }

    @Transactional
    public JenisPelatihanResponse get(UUID uuid) {
        return mapper.toResponse(find(uuid));
    }

    @Transactional
    public JenisPelatihanResponse create(JenisPelatihanRequest request) {
        String nama = requireNama(request);
        if (jenisPelatihanRepository.existsByNama(nama, null)) {
            throw new ConflictException("Jenis pelatihan sudah ada.");
        }

        JenisPelatihan entity = mapper.toEntity(request);
        entity.setJenisPelatihan(nama);
        jenisPelatihanRepository.persist(entity);

        return mapper.toResponse(entity);
    }

    @Transactional
    public JenisPelatihanResponse update(UUID uuid, JenisPelatihanRequest request) {
        JenisPelatihan entity = find(uuid);
        String nama = requireNama(request);

        if (jenisPelatihanRepository.existsByNama(nama, entity.getId())) {
            throw new ConflictException("Jenis pelatihan sudah ada.");
        }

        mapper.updateEntity(request, entity);
        entity.setJenisPelatihan(nama);

        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(UUID uuid) {
        JenisPelatihan entity = find(uuid);
        entity.setDeletedAt(OffsetDateTime.now());
    }

    private JenisPelatihan find(UUID uuid) {
        return jenisPelatihanRepository.findActiveByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Jenis pelatihan tidak ditemukan."));
    }

    private String requireNama(JenisPelatihanRequest request) {
        String nama = QuerySupport.trimToNull(request.jenisPelatihan());
        if (nama == null) {
            throw new BadRequestException("jenisPelatihan wajib diisi.");
        }
        return nama;
    }
}
