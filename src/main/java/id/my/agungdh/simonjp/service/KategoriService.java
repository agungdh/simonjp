package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Kategori;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.dto.KategoriRequest;
import id.my.agungdh.simonjp.dto.KategoriResponse;
import id.my.agungdh.simonjp.mapper.KategoriMapper;
import id.my.agungdh.simonjp.repository.KategoriRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class KategoriService {

    private static final int PAGE_SIZE = 20;

    private final KategoriRepository repository;
    private final KategoriMapper mapper;

    public KategoriResponse create(KategoriRequest request) {
        if (repository.findByNamaIgnoreCase(request.nama()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama kategori sudah digunakan");
        }
        Kategori entity = mapper.toEntity(request);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public KategoriResponse get(UUID uuid) {
        Kategori entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan"));
        return mapper.toResponse(entity);
    }

    public KategoriResponse update(UUID uuid, KategoriRequest request) {
        Kategori entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan"));
        repository.findByNamaIgnoreCase(request.nama())
                .filter(existing -> !existing.getUuid().equals(uuid))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama kategori sudah digunakan");
                });
        mapper.updateEntity(request, entity);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID uuid) {
        Kategori entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kategori tidak ditemukan"));
        entity.setDeletedAt(OffsetDateTime.now());
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public CursorPageDTO<KategoriResponse> list(UUID cursor) {
        int size = PAGE_SIZE;
        Long cursorId = null;
        if (cursor != null) {
            Kategori cursorEntity = repository.findByUuid(cursor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cursor tidak valid"));
            cursorId = cursorEntity.getId();
        }
        List<Kategori> rows = repository.findPage(cursorId, PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "id")));
        boolean hasMore = rows.size() > size;
        List<Kategori> items = hasMore ? rows.subList(0, size) : rows;
        UUID nextCursor = (hasMore && !items.isEmpty()) ? items.get(items.size() - 1).getUuid() : null;
        List<KategoriResponse> responses = items.stream().map(mapper::toResponse).toList();
        return new CursorPageDTO<>(responses, nextCursor, hasMore);
    }
}