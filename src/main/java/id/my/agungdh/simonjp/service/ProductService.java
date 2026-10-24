package id.my.agungdh.simonjp.service;

import id.my.agungdh.simonjp.domain.Product;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.dto.ProductRequest;
import id.my.agungdh.simonjp.dto.ProductResponse;
import id.my.agungdh.simonjp.mapper.ProductMapper;
import id.my.agungdh.simonjp.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final int pageSize;

    public ProductService(ProductRepository repository, ProductMapper mapper,
                          @Value("${app.page-size:20}") int pageSize) {
        this.repository = repository;
        this.mapper = mapper;
        this.pageSize = pageSize;
    }

    public ProductResponse create(ProductRequest request) {
        Product entity = mapper.toEntity(request);
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
        mapper.updateEntity(request, entity);
        repository.save(entity);
        return mapper.toResponse(entity);
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