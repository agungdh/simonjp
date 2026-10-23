package id.my.agungdh.resource;

import java.net.URI;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.dto.JenisPelatihanRequest;
import id.my.agungdh.dto.JenisPelatihanResponse;
import id.my.agungdh.service.JenisPelatihanService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/jenis-pelatihan")
@RunOnVirtualThread
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "JenisPelatihan", description = "Master jenis pelatihan.")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class JenisPelatihanResource {

    private final JenisPelatihanService service;

    @GET
    @Operation(summary = "List jenis pelatihan dengan cursor pagination.")
    public CursorPageDTO<JenisPelatihanResponse> list(
            @QueryParam("search") String search,
            @QueryParam("cursor") UUID cursor,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        return service.list(search, cursor, limit);
    }

    @GET
    @Path("/{uuid}")
    @Operation(summary = "Ambil satu jenis pelatihan berdasarkan uuid.")
    public JenisPelatihanResponse get(@PathParam("uuid") UUID uuid) {
        return service.get(uuid);
    }

    @POST
    @Operation(summary = "Buat jenis pelatihan baru.")
    public Response create(@Valid JenisPelatihanRequest request) {
        JenisPelatihanResponse body = service.create(request);
        return Response.created(URI.create("/jenis-pelatihan/" + body.uuid())).entity(body).build();
    }

    @PUT
    @Path("/{uuid}")
    @Operation(summary = "Ganti seluruh data jenis pelatihan (PUT full replace).")
    public JenisPelatihanResponse update(@PathParam("uuid") UUID uuid, @Valid JenisPelatihanRequest request) {
        return service.update(uuid, request);
    }

    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Soft delete jenis pelatihan.")
    public Response delete(@PathParam("uuid") UUID uuid) {
        service.delete(uuid);
        return Response.noContent().build();
    }
}
