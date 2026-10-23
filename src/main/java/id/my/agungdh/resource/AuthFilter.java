package id.my.agungdh.resource;

import java.io.IOException;

import id.my.agungdh.domain.AppUser;
import id.my.agungdh.service.AppUserService;
import id.my.agungdh.service.SessionService;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

@Provider
@ApplicationScoped
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    private static final String SESSION_COOKIE = "SESSION";

    @Inject
    SessionService sessionService;
    @Inject
    AppUserService appUserService;

    @Inject
    @ConfigProperty(name = "app.auth.enabled", defaultValue = "true")
    boolean authEnabled;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (!authEnabled) {
            return;
        }
        String path = requestContext.getUriInfo().getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        // Normalize: strip leading slash already; ensure no leading slash
        String normalized = "/" + path;

        if (isPublic(normalized, requestContext.getMethod())) {
            return;
        }

        Cookie cookie = requestContext.getCookies().get(SESSION_COOKIE);
        String token = (cookie == null) ? null : cookie.getValue();
        String username = sessionService.findUsername(token);

        if (username == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(java.util.Map.of("message", "Belum login."))
                    .build());
            return;
        }

        AppUser user = appUserService.findByUsername(username);
        if (user == null) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(java.util.Map.of("message", "Session tidak valid."))
                    .build());
            return;
        }

        requestContext.setProperty("currentUser", user);
    }

    private boolean isPublic(String path, String method) {
        if (path.equals("/login") && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.equals("/healthcheck")) {
            return true;
        }
        if (path.startsWith("/q/") || path.startsWith("/openapi")) {
            return true;
        }
        return false;
    }
}
