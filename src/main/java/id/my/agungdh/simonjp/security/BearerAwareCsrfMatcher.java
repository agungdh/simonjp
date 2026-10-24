package id.my.agungdh.simonjp.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

/**
 * CSRF hanya relevan untuk request yang membawa cookie. Request dengan
 * {@code Authorization: Bearer} (mobile) dan endpoint {@code /auth/token}
 * (belum punya cookie sama sekali) dikecualikan.
 */
@Component
@RequiredArgsConstructor
public class BearerAwareCsrfMatcher implements RequestMatcher {

    private static final String BEARER_LOGIN_PATH = "/auth/token";

    private final TokenExtractor tokenExtractor;

    @Override
    public boolean matches(HttpServletRequest request) {
        if (!CsrfFilter.DEFAULT_CSRF_MATCHER.matches(request)) {
            return false;
        }
        if (tokenExtractor.isBearer(request)) {
            return false;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !BEARER_LOGIN_PATH.equals(path);
    }
}
