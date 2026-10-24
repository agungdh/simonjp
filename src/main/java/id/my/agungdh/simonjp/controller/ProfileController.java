package id.my.agungdh.simonjp.controller;

import id.my.agungdh.simonjp.config.ApiInternalError;
import id.my.agungdh.simonjp.config.ApiUnauthorized;
import id.my.agungdh.simonjp.config.ApiValidationError;
import id.my.agungdh.simonjp.dto.ChangePasswordRequest;
import id.my.agungdh.simonjp.dto.PegawaiResponse;
import id.my.agungdh.simonjp.dto.ProfileUpdateRequest;
import id.my.agungdh.simonjp.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Profil milik user yang sedang login (bukan manajemen akun oleh admin).
 */
@RestController
@RequestMapping("/profile")
@Tag(name = "Profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService service;

    @GetMapping
    @Operation(summary = "Profil user yang sedang login")
    @ApiResponse(responseCode = "200", description = "Profil ditemukan")
    @ApiUnauthorized
    @ApiInternalError
    public PegawaiResponse get() {
        return service.get();
    }

    @PutMapping
    @Operation(summary = "Perbarui profil sendiri (full replace)",
            description = "Role dan status tidak bisa diubah lewat endpoint ini.")
    @ApiResponse(responseCode = "200", description = "Profil diperbarui")
    @ApiValidationError
    @ApiUnauthorized
    @ApiInternalError
    public PegawaiResponse update(@Valid @RequestBody ProfileUpdateRequest request) {
        return service.update(request);
    }

    @PutMapping("/password")
    @Operation(summary = "Ganti password sendiri",
            description = "Wajib menyertakan password saat ini. Semua sesi dicabut setelah berhasil.")
    @ApiResponse(responseCode = "204", description = "Password diperbarui")
    @ApiValidationError
    @ApiUnauthorized
    @ApiInternalError
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(request);
    }
}
