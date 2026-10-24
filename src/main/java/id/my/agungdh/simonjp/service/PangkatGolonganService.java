package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.PangkatGolongan;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.dto.PangkatGolonganRequest;
import id.my.agungdh.simonjp.dto.PangkatGolonganResponse;
import id.my.agungdh.simonjp.mapper.PangkatGolonganMapper;
import id.my.agungdh.simonjp.repository.PangkatGolonganRepository;
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
public class PangkatGolonganService {

    private static final int PAGE_SIZE = 20;

    private final PangkatGolonganRepository repository;
    private final PangkatGolonganMapper mapper;

    public PangkatGolonganResponse create(PangkatGolonganRequest request) {
        repository.findDuplicate(request.pangkat(), request.golongan(), request.ruang())
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Pangkat/golongan sudah digunakan");
                });
        PangkatGolongan entity = mapper.toEntity(request);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public PangkatGolonganResponse get(UUID uuid) {
        PangkatGolongan entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pangkat/golongan tidak ditemukan"));
        return mapper.toResponse(entity);
    }

    public PangkatGolonganResponse update(UUID uuid, PangkatGolonganRequest request) {
        PangkatGolongan entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pangkat/golongan tidak ditemukan"));
        repository.findDuplicate(request.pangkat(), request.golongan(), request.ruang())
                .filter(existing -> !existing.getUuid().equals(uuid))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Pangkat/golongan sudah digunakan");
                });
        mapper.updateEntity(request, entity);
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID uuid) {
        PangkatGolongan entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pangkat/golongan tidak ditemukan"));
        entity.setDeletedAt(OffsetDateTime.now());
        CurrentUser.id().ifPresent(entity::setDeletedBy);
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public CursorPageDTO<PangkatGolonganResponse> list(UUID cursor) {
        int size = PAGE_SIZE;
        Long cursorId = null;
        if (cursor != null) {
            PangkatGolongan cursorEntity = repository.findByUuid(cursor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cursor tidak valid"));
            cursorId = cursorEntity.getId();
        }
        List<PangkatGolongan> rows = repository.findPage(cursorId,
                PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "id")));
        boolean hasMore = rows.size() > size;
        List<PangkatGolongan> items = hasMore ? rows.subList(0, size) : rows;
        UUID nextCursor = (hasMore && !items.isEmpty()) ? items.get(items.size() - 1).getUuid() : null;
        List<PangkatGolonganResponse> responses = items.stream().map(mapper::toResponse).toList();
        return new CursorPageDTO<>(responses, nextCursor, hasMore);
    }
}
