package id.my.agungdh.resource;

import java.net.URI;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.dto.PangkatGolonganRequest;
import id.my.agungdh.dto.PangkatGolonganResponse;
import id.my.agungdh.service.PangkatGolonganService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
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

@Path("/pangkat-golongan")
@RunOnVirtualThread
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Pangkat Golongan", description = "Master pangkat dan golongan pegawai.")
public class PangkatGolonganResource {

    private final PangkatGolonganService service;

    @Inject
    public PangkatGolonganResource(PangkatGolonganService service) {
        this.service = service;
    }

    @GET
    @Operation(summary = "List pangkat golongan dengan cursor pagination.")
    public CursorPageDTO<PangkatGolonganResponse> list(
            @QueryParam("search") String search,
            @QueryParam("cursor") UUID cursor,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        return service.list(search, cursor, limit);
    }

    @GET
    @Path("/{uuid}")
    @Operation(summary = "Ambil satu pangkat golongan berdasarkan uuid.")
    public PangkatGolonganResponse get(@PathParam("uuid") UUID uuid) {
        return service.get(uuid);
    }

    @POST
    @Operation(summary = "Buat pangkat golongan baru.")
    public Response create(@Valid PangkatGolonganRequest request) {
        PangkatGolonganResponse body = service.create(request);
        return Response.created(URI.create("/pangkat-golongan/" + body.uuid())).entity(body).build();
    }

    @PUT
    @Path("/{uuid}")
    @Operation(summary = "Ganti seluruh data pangkat golongan (PUT full replace).")
    public PangkatGolonganResponse update(@PathParam("uuid") UUID uuid, @Valid PangkatGolonganRequest request) {
        return service.update(uuid, request);
    }

    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Soft delete pangkat golongan.")
    public Response delete(@PathParam("uuid") UUID uuid) {
        service.delete(uuid);
        return Response.noContent().build();
    }
}
