package id.my.agungdh.simonjp.controller;

import id.my.agungdh.simonjp.dto.ErrorResponse;
import id.my.agungdh.simonjp.dto.ValidasiRequest;
import id.my.agungdh.simonjp.dto.ValidasiResponse;
import id.my.agungdh.simonjp.service.ValidasiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validasi berhasil",
                    content = @Content(schema = @Schema(implementation = ValidasiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validasi gagal",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "validasi-gagal", value = """
                                    {
                                      "message": "Validasi gagal",
                                      "errors": [
                                        {"field": "nama", "message": "must not be blank"},
                                        {"field": "email", "message": "must be a well-formed email address"},
                                        {"field": "umur", "message": "must be greater than or equal to 17"},
                                        {"field": "harga", "message": "must be greater than 0"},
                                        {"field": "stok", "message": "must be greater than or equal to 0"}
                                      ]
                                    }
                                    """)))
    })
    public ResponseEntity<ValidasiResponse> create(@Valid @RequestBody ValidasiRequest request) {
        return ResponseEntity.ok(service.create(request));
    }
}