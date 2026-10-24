package id.my.agungdh.simonjp.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

/**
 * Akses ke principal yang sedang login, dipakai untuk audit dan endpoint {@code /auth/me}.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Optional<AppUserPrincipal> principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Optional<Long> id() {
        return principal().map(AppUserPrincipal::id);
    }

    public static AppUserPrincipal require() {
        return principal().orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Autentikasi diperlukan"));
    }
}
