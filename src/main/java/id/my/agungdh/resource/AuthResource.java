package id.my.agungdh.resource;

import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import id.my.agungdh.domain.AppUser;
import id.my.agungdh.dto.LoginRequest;
import id.my.agungdh.service.AppUserService;
import id.my.agungdh.service.SessionService;
import io.smallrye.common.annotation.RunOnVirtualThread;
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
import lombok.RequiredArgsConstructor;

@Path("/")
@RunOnVirtualThread
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Autentikasi berbasis cookie + session Redis.")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AuthResource {

    private final AppUserService appUserService;
    private final SessionService sessionService;

    @POST
    @Path("/login")
    @Operation(summary = "Login, set cookie session.")
    public Response login(@Valid LoginRequest request) {
        AppUser user = appUserService.findByUsername(request.username());
        if (!appUserService.verifyPassword(user, request.password())) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Username atau password salah."))
                    .build();
        }

        String token = sessionService.create(user.getUsername());
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
    @Operation(summary = "Logout, hapus session.")
    public Response logout(@CookieParam("SESSION") Cookie sessionCookie) {
        String token = (sessionCookie == null) ? null : sessionCookie.getValue();
        sessionService.delete(token);

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
    public Response me(@CookieParam("SESSION") Cookie sessionCookie) {
        String token = (sessionCookie == null) ? null : sessionCookie.getValue();
        String username = sessionService.findUsername(token);
        if (username == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Belum login."))
                    .build();
        }
        AppUser user = appUserService.findByUsername(username);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Session tidak valid."))
                    .build();
        }
        return Response.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "uuid", user.getUuid()))
                .build();
    }
}
