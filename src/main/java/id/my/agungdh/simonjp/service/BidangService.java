package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.config.PageProperties;
import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.dto.BidangRequest;
import id.my.agungdh.simonjp.dto.BidangResponse;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.mapper.BidangMapper;
import id.my.agungdh.simonjp.repository.BidangRepository;
import id.my.agungdh.simonjp.security.CurrentUser;
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
public class BidangService {

    private final BidangRepository repository;
    private final BidangMapper mapper;
    private final PageProperties pageProperties;

    public BidangResponse create(BidangRequest request) {
        if (repository.findByNamaIgnoreCase(request.nama()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama bidang sudah digunakan");
        }
        Bidang entity = mapper.toEntity(request);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public BidangResponse get(UUID uuid) {
        Bidang entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bidang tidak ditemukan"));
        return mapper.toResponse(entity);
    }

    public BidangResponse update(UUID uuid, BidangRequest request) {
        Bidang entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bidang tidak ditemukan"));
        repository.findByNamaIgnoreCase(request.nama())
                .filter(existing -> !existing.getUuid().equals(uuid))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama bidang sudah digunakan");
                });
        mapper.updateEntity(request, entity);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID uuid) {
        Bidang entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bidang tidak ditemukan"));
        entity.setDeletedAt(OffsetDateTime.now());
        CurrentUser.id().ifPresent(entity::setDeletedBy);
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public CursorPageDTO<BidangResponse> list(UUID cursor, String search) {
        SearchTerm term = SearchTerm.of(search);
        int size = pageProperties.pageSize();
        PageRequest pageable = PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "id"));
        Long cursorId = null;
        if (cursor != null) {
            Bidang cursorEntity = repository.findByUuid(cursor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cursor tidak valid"));
            // Cursor hanya valid untuk filter pencarian yang sama.
            if (term != null && !term.matchesAny(cursorEntity.getNama(), cursorEntity.getDeskripsi())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cursor tidak valid");
            }
            cursorId = cursorEntity.getId();
        }
        List<Bidang> rows = term == null
                ? repository.findPage(cursorId, pageable)
                : repository.searchPage(term.pattern(), cursorId, pageable);
        boolean hasMore = rows.size() > size;
        List<Bidang> items = hasMore ? rows.subList(0, size) : rows;
        UUID nextCursor = (hasMore && !items.isEmpty()) ? items.get(items.size() - 1).getUuid() : null;
        List<BidangResponse> responses = items.stream().map(mapper::toResponse).toList();
        return new CursorPageDTO<>(responses, nextCursor, hasMore);
    }
}
