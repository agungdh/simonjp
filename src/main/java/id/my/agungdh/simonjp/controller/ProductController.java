package id.my.agungdh.simonjp.controller;

import id.my.agungdh.simonjp.config.ApiBadRequest;
import id.my.agungdh.simonjp.config.ApiInternalError;
import id.my.agungdh.simonjp.config.ApiInvalidCursor;
import id.my.agungdh.simonjp.config.ApiNotFound;
import id.my.agungdh.simonjp.config.ApiValidationError;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.dto.ProductRequest;
import id.my.agungdh.simonjp.dto.ProductResponse;
import id.my.agungdh.simonjp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/product")
@Tag(name = "Product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @GetMapping
    @Operation(summary = "Daftar produk (cursor pagination)")
    @ApiResponse(responseCode = "200", description = "Daftar produk")
    @ApiInvalidCursor
    @ApiInternalError
    public CursorPageDTO<ProductResponse> list(@RequestParam(required = false) UUID cursor) {
        return service.list(cursor);
    }

    @PostMapping
    @Operation(summary = "Buat produk baru")
    @ApiResponse(responseCode = "201", description = "Produk dibuat")
    @ApiValidationError
    @ApiInternalError
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Ambil produk berdasarkan UUID")
    @ApiResponse(responseCode = "200", description = "Produk ditemukan")
    @ApiBadRequest
    @ApiNotFound
    @ApiInternalError
    public ProductResponse get(@PathVariable UUID uuid) {
        return service.get(uuid);
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Perbarui produk (full replace)")
    @ApiResponse(responseCode = "200", description = "Produk diperbarui")
    @ApiValidationError
    @ApiNotFound
    @ApiInternalError
    public ProductResponse update(@PathVariable UUID uuid, @Valid @RequestBody ProductRequest request) {
        return service.update(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Hapus produk (soft delete)")
    @ApiResponse(responseCode = "204", description = "Produk dihapus")
    @ApiBadRequest
    @ApiNotFound
    @ApiInternalError
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        service.delete(uuid);
        return ResponseEntity.noContent().build();
    }
}
