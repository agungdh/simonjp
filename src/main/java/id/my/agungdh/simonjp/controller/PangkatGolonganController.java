package id.my.agungdh.simonjp.controller;

import id.my.agungdh.simonjp.config.ApiBadRequest;
import id.my.agungdh.simonjp.config.ApiForbidden;
import id.my.agungdh.simonjp.config.ApiInternalError;
import id.my.agungdh.simonjp.config.ApiInvalidCursor;
import id.my.agungdh.simonjp.config.ApiNotFound;
import id.my.agungdh.simonjp.config.ApiUnauthorized;
import id.my.agungdh.simonjp.config.ApiValidationError;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.dto.PangkatGolonganRequest;
import id.my.agungdh.simonjp.dto.PangkatGolonganResponse;
import id.my.agungdh.simonjp.service.PangkatGolonganService;
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
@RequestMapping("/pangkat-golongan")
@Tag(name = "Pangkat Golongan")
@RequiredArgsConstructor
public class PangkatGolonganController {

    private final PangkatGolonganService service;

    @GetMapping
    @Operation(summary = "Daftar pangkat/golongan (cursor pagination)",
            description = "search = substring case-insensitive (3-100 karakter) pada "
                    + "jenjang/pangkat/golongan/ruang.")
    @ApiResponse(responseCode = "200", description = "Daftar pangkat/golongan")
    @ApiInvalidCursor
    @ApiUnauthorized
    @ApiInternalError
    public CursorPageDTO<PangkatGolonganResponse> list(
            @Parameter(description = "Pencarian %kata% pada jenjang/pangkat/golongan/ruang")
            @RequestParam(required = false) String search,
            @Parameter(description = "Cursor UUID baris terakhir halaman sebelumnya")
            @RequestParam(required = false) UUID cursor) {
        return service.list(cursor, search);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buat pangkat/golongan baru")
    @ApiResponse(responseCode = "201", description = "Pangkat/golongan dibuat")
    @ApiValidationError
    @ApiUnauthorized
    @ApiForbidden
    @ApiInternalError
    public ResponseEntity<PangkatGolonganResponse> create(@Valid @RequestBody PangkatGolonganRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Ambil pangkat/golongan berdasarkan UUID")
    @ApiResponse(responseCode = "200", description = "Pangkat/golongan ditemukan")
    @ApiBadRequest
    @ApiUnauthorized
    @ApiNotFound
    @ApiInternalError
    public PangkatGolonganResponse get(@PathVariable UUID uuid) {
        return service.get(uuid);
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Perbarui pangkat/golongan (full replace)")
    @ApiResponse(responseCode = "200", description = "Pangkat/golongan diperbarui")
    @ApiValidationError
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiInternalError
    public PangkatGolonganResponse update(@PathVariable UUID uuid,
                                          @Valid @RequestBody PangkatGolonganRequest request) {
        return service.update(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hapus pangkat/golongan (soft delete)")
    @ApiResponse(responseCode = "204", description = "Pangkat/golongan dihapus")
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
