package id.my.agungdh.simonjp.controller;

import id.my.agungdh.simonjp.config.ApiBadRequest;
import id.my.agungdh.simonjp.config.ApiForbidden;
import id.my.agungdh.simonjp.config.ApiInternalError;
import id.my.agungdh.simonjp.config.ApiInvalidCursor;
import id.my.agungdh.simonjp.config.ApiNotFound;
import id.my.agungdh.simonjp.config.ApiUnauthorized;
import id.my.agungdh.simonjp.config.ApiValidationError;
import id.my.agungdh.simonjp.domain.KategoriJabatan;
import id.my.agungdh.simonjp.domain.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.simonjp.domain.PegawaiStatus;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.dto.PegawaiRequest;
import id.my.agungdh.simonjp.dto.PegawaiResponse;
import id.my.agungdh.simonjp.service.PegawaiService;
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
@RequestMapping("/pegawai")
@Tag(name = "Pegawai")
@RequiredArgsConstructor
public class PegawaiController {

    private final PegawaiService service;

    @GetMapping
    @Operation(summary = "Daftar pegawai (cursor pagination)",
            description = "search = substring case-insensitive (3-100 karakter) pada kolom pegawai "
                    + "serta bidang dan pangkat/golongan yang di-join. Filter lain digabung dengan AND.")
    @ApiResponse(responseCode = "200", description = "Daftar pegawai")
    @ApiInvalidCursor
    @ApiUnauthorized
    @ApiInternalError
    public CursorPageDTO<PegawaiResponse> list(
            @Parameter(description = "Pencarian %kata% pada pegawai, bidang, dan pangkat/golongan")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter status kepegawaian")
            @RequestParam(required = false) PegawaiStatus status,
            @Parameter(description = "Filter UUID bidang")
            @RequestParam(required = false) UUID bidangUuid,
            @Parameter(description = "Filter UUID pangkat/golongan")
            @RequestParam(required = false) UUID pangkatGolonganUuid,
            @Parameter(description = "Filter kategori jabatan")
            @RequestParam(required = false) KategoriJabatan kategoriJabatan,
            @Parameter(description = "Filter kategori kebutuhan jam pelatihan")
            @RequestParam(required = false) KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan,
            @Parameter(description = "Cursor UUID baris terakhir halaman sebelumnya")
            @RequestParam(required = false) UUID cursor) {
        return service.list(cursor, search, status, bidangUuid, pangkatGolonganUuid,
                kategoriJabatan, kategoriKebutuhanJamPelatihan);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Buat pegawai baru beserta akunnya",
            description = "Username akun selalu NIP. Password kosong berarti memakai NIP.")
    @ApiResponse(responseCode = "201", description = "Pegawai dan akun dibuat")
    @ApiValidationError
    @ApiUnauthorized
    @ApiForbidden
    @ApiInternalError
    public ResponseEntity<PegawaiResponse> create(@Valid @RequestBody PegawaiRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Ambil pegawai berdasarkan UUID")
    @ApiResponse(responseCode = "200", description = "Pegawai ditemukan")
    @ApiBadRequest
    @ApiUnauthorized
    @ApiNotFound
    @ApiInternalError
    public PegawaiResponse get(@PathVariable UUID uuid) {
        return service.get(uuid);
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Perbarui pegawai dan akunnya (full replace)",
            description = "Username mengikuti NIP. Password kosong berarti tidak diubah.")
    @ApiResponse(responseCode = "200", description = "Pegawai diperbarui")
    @ApiValidationError
    @ApiUnauthorized
    @ApiForbidden
    @ApiNotFound
    @ApiInternalError
    public PegawaiResponse update(@PathVariable UUID uuid, @Valid @RequestBody PegawaiRequest request) {
        return service.update(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Hapus pegawai beserta akunnya (soft delete)",
            description = "Akun ikut di-soft delete dan semua tokennya dicabut.")
    @ApiResponse(responseCode = "204", description = "Pegawai dihapus")
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
