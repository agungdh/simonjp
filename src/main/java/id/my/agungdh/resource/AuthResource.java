package id.my.agungdh.resource;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import id.my.agungdh.domain.AppUser;
import id.my.agungdh.dto.LoginRequest;
import id.my.agungdh.service.AppUserService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import lombok.RequiredArgsConstructor;

@Path("/")
@RunOnVirtualThread
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Autentikasi JWT di cookie.")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthResource {

    private final AppUserService appUserService;

    @Inject
    SecurityContext securityContext;

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Login, set cookie SESSION (JWT).")
    public Response login(@Valid LoginRequest request) {
        AppUser user = appUserService.findByUsername(request.username());
        if (!appUserService.verifyPassword(user, request.password())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Username atau password salah."))
                    .build();
        }

        String token = Jwt.upn(user.getUsername())
                .groups(user.getRole().name())
                .issuer("simonjp")
                .expiresIn(Duration.ofHours(24))
                .sign();

        NewCookie cookie = new NewCookie.Builder("SESSION")
                .value(token)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(86400)
                .sameSite(NewCookie.SameSite.LAX)
                .build();

        return Response.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "uuid", user.getUuid()))
                .cookie(cookie)
                .build();
    }

    @POST
    @Path("/logout")
    @PermitAll
    @Operation(summary = "Logout, hapus cookie.")
    public Response logout() {
        NewCookie cleared = new NewCookie.Builder("SESSION")
                .value("")
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(0)
                .sameSite(NewCookie.SameSite.LAX)
                .build();
        return Response.ok(Map.of("message", "Logged out.")).cookie(cleared).build();
    }

    @GET
    @Path("/user")
    @Operation(summary = "Info user yang sedang login.")
    public Response me() {
        if (securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Belum login."))
                    .build();
        }
        return Response.ok(Map.of(
                "username", securityContext.getUserPrincipal().getName(),
                "roles", securityContext.getUserPrincipal() instanceof JsonWebToken
                        ? ((JsonWebToken) securityContext.getUserPrincipal()).getGroups()
                        : Set.of()))
                .build();
    }
}
