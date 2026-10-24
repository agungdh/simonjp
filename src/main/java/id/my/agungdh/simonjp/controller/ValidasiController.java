package id.my.agungdh.simonjp.controller;

import id.my.agungdh.simonjp.config.ApiInternalError;
import id.my.agungdh.simonjp.config.ApiValidationError;
import id.my.agungdh.simonjp.dto.ValidasiRequest;
import id.my.agungdh.simonjp.dto.ValidasiResponse;
import id.my.agungdh.simonjp.service.ValidasiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/validasi")
@Tag(name = "Validasi")
@RequiredArgsConstructor
public class ValidasiController {

    private final ValidasiService service;

    @PostMapping
    @Operation(summary = "Contoh endpoint untuk uji validasi (bisa menghasilkan >1 error)")
    @ApiResponse(responseCode = "200", description = "Validasi berhasil",
            content = @Content(schema = @Schema(implementation = ValidasiResponse.class)))
    @ApiValidationError
    @ApiInternalError
    public ResponseEntity<ValidasiResponse> create(@Valid @RequestBody ValidasiRequest request) {
        return ResponseEntity.ok(service.create(request));
    }
}
