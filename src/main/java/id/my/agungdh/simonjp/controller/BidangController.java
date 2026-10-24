package id.my.agungdh.simonjp.controller;

import id.my.agungdh.simonjp.config.ApiBadRequest;
import id.my.agungdh.simonjp.config.ApiForbidden;
import id.my.agungdh.simonjp.config.ApiInternalError;
import id.my.agungdh.simonjp.config.ApiInvalidCursor;
import id.my.agungdh.simonjp.config.ApiNotFound;
import id.my.agungdh.simonjp.config.ApiUnauthorized;
import id.my.agungdh.simonjp.config.ApiValidationError;
import id.my.agungdh.simonjp.dto.BidangRequest;
import id.my.agungdh.simonjp.dto.BidangResponse;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.service.BidangService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/bidang")
@Tag(name = "Bidang")
@RequiredArgsConstructor
public class BidangController {

    private final BidangService service;

    @GetMapping
    @Operation(summary = "Daftar bidang (cursor pagination)",
            description = "search = substring case-insensitive (3-100 karakter) pada nama/deskripsi.")
    @ApiResponse(responseCode = "200", description = "Daftar bidang")
    @ApiInvalidCursor
    @ApiUnauthorized
    @ApiInternalError
    public CursorPageDTO<BidangResponse> list(
            @Parameter(description = "Pencarian %kata% pada nama/deskripsi")
            @RequestParam(required = false) String search,
            @Parameter(description = "Cursor UUID baris terakhir halaman sebelumnya")
            @RequestParam(required = false) UUID cursor) {
        return service.list(cursor, search);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buat bidang baru")
    @ApiResponse(responseCode = "201", description = "Bidang dibuat")
    @ApiValidationError
    @ApiUnauthorized
    @ApiForbidden
    @ApiInternalError
    public ResponseEntity<BidangResponse> create(@Valid @RequestBody BidangRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Ambil bidang berdasarkan UUID")
    @ApiResponse(responseCode = "200", description = "Bidang ditemukan")
    @ApiBadRequest
    @ApiUnauthorized
    @ApiNotFound
    @ApiInternalError
    public BidangResponse get(@PathVariable UUID uuid) {
        return service.get(uuid);
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Perbarui bidang (full replace)")
    @ApiResponse(responseCode = "200", description = "Bidang diperbarui")
    @ApiValidationError
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiInternalError
    public BidangResponse update(@PathVariable UUID uuid, @Valid @RequestBody BidangRequest request) {
        return service.update(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hapus bidang (soft delete)")
    @ApiResponse(responseCode = "204", description = "Bidang dihapus")
    @ApiBadRequest
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiInternalError
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        service.delete(uuid);
        return ResponseEntity.noContent().build();
    }
}
