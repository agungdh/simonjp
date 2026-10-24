package id.my.agungdh.simonjp.controller;

import id.my.agungdh.simonjp.config.ApiBadRequest;
import id.my.agungdh.simonjp.config.ApiInternalError;
import id.my.agungdh.simonjp.config.ApiInvalidCursor;
import id.my.agungdh.simonjp.config.ApiNotFound;
import id.my.agungdh.simonjp.config.ApiValidationError;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.dto.KategoriRequest;
import id.my.agungdh.simonjp.dto.KategoriResponse;
import id.my.agungdh.simonjp.service.KategoriService;
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
@RequestMapping("/kategori")
@Tag(name = "Kategori")
@RequiredArgsConstructor
public class KategoriController {

    private final KategoriService service;

    @GetMapping
    @Operation(summary = "Daftar kategori (cursor pagination)")
    @ApiResponse(responseCode = "200", description = "Daftar kategori")
    @ApiInvalidCursor
    @ApiInternalError
    public CursorPageDTO<KategoriResponse> list(@RequestParam(required = false) UUID cursor) {
        return service.list(cursor);
    }

    @PostMapping
    @Operation(summary = "Buat kategori baru")
    @ApiResponse(responseCode = "201", description = "Kategori dibuat")
    @ApiValidationError
    @ApiInternalError
    public ResponseEntity<KategoriResponse> create(@Valid @RequestBody KategoriRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Ambil kategori berdasarkan UUID")
    @ApiResponse(responseCode = "200", description = "Kategori ditemukan")
    @ApiBadRequest
    @ApiNotFound
    @ApiInternalError
    public KategoriResponse get(@PathVariable UUID uuid) {
        return service.get(uuid);
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Perbarui kategori (full replace)")
    @ApiResponse(responseCode = "200", description = "Kategori diperbarui")
    @ApiValidationError
    @ApiNotFound
    @ApiInternalError
    public KategoriResponse update(@PathVariable UUID uuid, @Valid @RequestBody KategoriRequest request) {
        return service.update(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Hapus kategori (soft delete)")
    @ApiResponse(responseCode = "204", description = "Kategori dihapus")
    @ApiBadRequest
    @ApiNotFound
    @ApiInternalError
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        service.delete(uuid);
        return ResponseEntity.noContent().build();
    }
}
