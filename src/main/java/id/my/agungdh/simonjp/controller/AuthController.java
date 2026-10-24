package id.my.agungdh.simonjp.controller;

import id.my.agungdh.simonjp.config.ApiInternalError;
import id.my.agungdh.simonjp.config.ApiUnauthorized;
import id.my.agungdh.simonjp.config.ApiValidationError;
import id.my.agungdh.simonjp.dto.CsrfResponse;
import id.my.agungdh.simonjp.dto.LoginRequest;
import id.my.agungdh.simonjp.dto.TokenResponse;
import id.my.agungdh.simonjp.dto.UserResponse;
import id.my.agungdh.simonjp.security.AuthCookieFactory;
import id.my.agungdh.simonjp.security.TokenExtractor;
import id.my.agungdh.simonjp.service.AuthService;
import id.my.agungdh.simonjp.service.AuthenticatedSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

/**
 * Autentikasi berbasis opaque token. Browser memakai cookie HttpOnly, mobile memakai
 * header {@code Authorization: Bearer}; token dan store-nya sama.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieFactory cookieFactory;
    private final TokenExtractor tokenExtractor;

    @PostMapping("/login")
    @Operation(summary = "Login web, token dikirim lewat cookie HttpOnly",
            description = "Wajib mengirim header X-XSRF-TOKEN dari GET /auth/csrf. "
                    + "Token tidak pernah muncul di body response.")
    @ApiResponse(responseCode = "200", description = "Login berhasil")
    @ApiValidationError
    @ApiUnauthorized
    @ApiInternalError
    @SecurityRequirements
    public UserResponse login(@Valid @RequestBody LoginRequest request,
                              HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) {
        String previousToken = tokenExtractor.extract(httpRequest).orElse(null);
        AuthenticatedSession session = authService.loginWeb(request, previousToken);
        cookieFactory.addToken(httpResponse, session.token(), Duration.ofSeconds(session.expiresIn()));
        return session.user();
    }

    @PostMapping("/token")
    @Operation(summary = "Login mobile, token dikirim lewat body untuk header Bearer",
            description = "Tidak memakai cookie dan tidak memerlukan CSRF.")
    @ApiResponse(responseCode = "200", description = "Login berhasil")
    @ApiValidationError
    @ApiUnauthorized
    @ApiInternalError
    @SecurityRequirements
    public TokenResponse token(@Valid @RequestBody LoginRequest request) {
        AuthenticatedSession session = authService.loginMobile(request);
        return new TokenResponse(session.token(), session.expiresIn(), session.user());
    }

    @GetMapping("/me")
    @Operation(summary = "User yang sedang login")
    @ApiResponse(responseCode = "200", description = "User ditemukan")
    @ApiUnauthorized
    @ApiInternalError
    public UserResponse me() {
        return authService.me();
    }

    @GetMapping("/csrf")
    @Operation(summary = "Ambil CSRF token untuk header X-XSRF-TOKEN")
    @ApiResponse(responseCode = "200", description = "CSRF token dibuat")
    @ApiInternalError
    @SecurityRequirements
    public CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(csrfToken.getToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout: cabut token dan hapus cookie",
            description = "Request berbasis cookie wajib mengirim X-XSRF-TOKEN; "
                    + "request Bearer tidak memerlukannya.")
    @ApiResponse(responseCode = "204", description = "Logout berhasil")
    @ApiUnauthorized
    @ApiInternalError
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        tokenExtractor.extract(httpRequest).ifPresent(authService::logout);
        cookieFactory.clearToken(httpResponse);
        cookieFactory.clearCsrf(httpResponse);
    }
}
