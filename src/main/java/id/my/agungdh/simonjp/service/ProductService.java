package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Kategori;
import id.my.agungdh.simonjp.domain.Product;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.dto.ProductRequest;
import id.my.agungdh.simonjp.dto.ProductResponse;
import id.my.agungdh.simonjp.mapper.ProductMapper;
import id.my.agungdh.simonjp.repository.KategoriRepository;
import id.my.agungdh.simonjp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final KategoriRepository kategoriRepository;

    @Value("${app.page-size:20}")
    private int pageSize;

    public ProductResponse create(ProductRequest request) {
        if (repository.findByNamaIgnoreCase(request.nama()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama produk sudah digunakan");
        }
        Product entity = mapper.toEntity(request);
        entity.setKategori(resolveKategori(request.kategoriUuid()));
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(UUID uuid) {
        Product entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produk tidak ditemukan"));
        return mapper.toResponse(entity);
    }

    public ProductResponse update(UUID uuid, ProductRequest request) {
        Product entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produk tidak ditemukan"));
        repository.findByNamaIgnoreCase(request.nama())
                .filter(existing -> !existing.getUuid().equals(uuid))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama produk sudah digunakan");
                });
        mapper.updateEntity(request, entity);
        entity.setKategori(resolveKategori(request.kategoriUuid()));
        repository.save(entity);
        return mapper.toResponse(entity);
    }

    private Kategori resolveKategori(UUID kategoriUuid) {
        if (kategoriUuid == null) {
            return null;
        }
        return kategoriRepository.findByUuid(kategoriUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kategori tidak ditemukan"));
    }

    public void delete(UUID uuid) {
        Product entity = repository.findByUuid(uuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produk tidak ditemukan"));
        entity.setDeletedAt(OffsetDateTime.now());
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public CursorPageDTO<ProductResponse> list(UUID cursor) {
        int size = pageSize;
        Long cursorId = null;
        if (cursor != null) {
            Product cursorEntity = repository.findByUuid(cursor)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cursor tidak valid"));
            cursorId = cursorEntity.getId();
        }
        List<Product> rows = repository.findPage(cursorId, PageRequest.of(0, size + 1, Sort.by(Sort.Direction.DESC, "id")));
        boolean hasMore = rows.size() > size;
        List<Product> items = hasMore ? rows.subList(0, size) : rows;
        UUID nextCursor = (hasMore && !items.isEmpty()) ? items.get(items.size() - 1).getUuid() : null;
        List<ProductResponse> responses = items.stream().map(mapper::toResponse).toList();
        return new CursorPageDTO<>(responses, nextCursor, hasMore);
    }
}