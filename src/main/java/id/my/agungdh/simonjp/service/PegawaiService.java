package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Bidang;
import id.my.agungdh.simonjp.domain.Pegawai;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.dto.PegawaiRequest;
import id.my.agungdh.simonjp.dto.PegawaiResponse;
import id.my.agungdh.simonjp.mapper.PegawaiMapper;
import id.my.agungdh.simonjp.repository.BidangRepository;
import id.my.agungdh.simonjp.repository.PegawaiRepository;
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
public class PegawaiService {

    private static final int PAGE_SIZE = 20;

    private final PegawaiRepository repository;
    private final PegawaiMapper mapper;
    private final BidangRepository bidangRepository;

    public PegawaiResponse create(PegawaiRequest request) {
        Pegawai entity = mapper.toEntity(request);
        entity.setBidang(resolveBidang(request.bidangUuid()));
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public PegawaiResponse get(UUID uuid) {
        Pegawai entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pegawai tidak ditemukan"));
        return mapper.toResponse(entity);
    }

    public PegawaiResponse update(UUID uuid, PegawaiRequest request) {
        Pegawai entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pegawai tidak ditemukan"));
        mapper.updateEntity(request, entity);
        entity.setBidang(resolveBidang(request.bidangUuid()));
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    public void delete(UUID uuid) {
        Pegawai entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pegawai tidak ditemukan"));
        entity.setDeletedAt(OffsetDateTime.now());
        CurrentUser.id().ifPresent(entity::setDeletedBy);
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public CursorPageDTO<PegawaiResponse> list(UUID cursor) {
        int size = PAGE_SIZE;
        Long cursorId = null;
        if (cursor != null) {
            Pegawai cursorEntity = repository.findByUuid(cursor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cursor tidak valid"));
            cursorId = cursorEntity.getId();
        }
        List<Pegawai> rows = repository.findPage(cursorId, PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "id")));
        boolean hasMore = rows.size() > size;
        List<Pegawai> items = hasMore ? rows.subList(0, size) : rows;
        UUID nextCursor = (hasMore && !items.isEmpty()) ? items.get(items.size() - 1).getUuid() : null;
        List<PegawaiResponse> responses = items.stream().map(mapper::toResponse).toList();
        return new CursorPageDTO<>(responses, nextCursor, hasMore);
    }

    private Bidang resolveBidang(UUID bidangUuid) {
        if (bidangUuid == null) {
            return null;
        }
        return bidangRepository.findByUuid(bidangUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bidang tidak ditemukan"));
    }
}
