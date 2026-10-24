package id.my.agungdh.simonjp.controller;

import id.my.agungdh.simonjp.dto.BidangRequest;
import id.my.agungdh.simonjp.dto.BidangResponse;
import id.my.agungdh.simonjp.dto.CursorPageDTO;
import id.my.agungdh.simonjp.service.BidangService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/bidang")
@Tag(name = "Bidang")
@RequiredArgsConstructor
public class BidangController {

    private final BidangService service;

    @PostMapping
    @Operation(summary = "Buat bidang baru")
    public ResponseEntity<BidangResponse> create(@Valid @RequestBody BidangRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Ambil bidang berdasarkan UUID")
    public BidangResponse get(@PathVariable UUID uuid) {
        return service.get(uuid);
    }

    @PutMapping("/{uuid}")
    @Operation(summary = "Perbarui bidang (full replace)")
    public BidangResponse update(@PathVariable UUID uuid, @Valid @RequestBody BidangRequest request) {
        return service.update(uuid, request);
    }

    @DeleteMapping("/{uuid}")
    @Operation(summary = "Hapus bidang (soft delete)")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        service.delete(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Daftar bidang (cursor pagination)")
    public CursorPageDTO<BidangResponse> list(@RequestParam(required = false) UUID cursor) {
        return service.list(cursor);
    }
}