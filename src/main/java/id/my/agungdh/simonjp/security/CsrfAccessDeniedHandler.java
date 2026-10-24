package id.my.agungdh.simonjp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handler khusus kegagalan CSRF, supaya request anonim yang CSRF-nya salah tidak
 * berubah menjadi 401 (perilaku default ExceptionTranslationFilter).
 */
@Component
public class CsrfAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        SecurityErrorWriter.write(response, HttpStatus.FORBIDDEN, "CSRF token tidak valid");
    }
}
