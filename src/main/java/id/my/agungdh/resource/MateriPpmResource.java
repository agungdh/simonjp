package id.my.agungdh.resource;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import id.my.agungdh.dto.MateriPpmRequest;
import id.my.agungdh.dto.MateriPpmResponse;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.service.MateriPpmService;
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

@Path("/materi-ppm")
@RunOnVirtualThread
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "MateriPpm", description = "Data materi ppm.")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class MateriPpmResource {

    private final MateriPpmService service;

    @GET
    @Operation(summary = "List materi ppm dengan cursor pagination.")
    public CursorPageDTO<MateriPpmResponse> list(
            @QueryParam("search") String search,
            @QueryParam("dariTanggal") LocalDate dariTanggal,
            @QueryParam("sampaiTanggal") LocalDate sampaiTanggal,
            @QueryParam("tahun") Integer tahun,
            @QueryParam("cursor") UUID cursor,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        return service.list(search, dariTanggal, sampaiTanggal, tahun, cursor, limit);
    }

    @GET
    @Path("/{uuid}")
    @Operation(summary = "Ambil satu materi ppm berdasarkan uuid.")
    public MateriPpmResponse get(@PathParam("uuid") UUID uuid) {
        return service.get(uuid);
    }

    @POST
    @Operation(summary = "Buat materi ppm baru.")
    public Response create(@Valid MateriPpmRequest request) {
        MateriPpmResponse body = service.create(request);
        return Response.created(URI.create("/materi-ppm/" + body.uuid())).entity(body).build();
    }

    @PUT
    @Path("/{uuid}")
    @Operation(summary = "Ganti seluruh data materi ppm (PUT full replace).")
    public MateriPpmResponse update(@PathParam("uuid") UUID uuid, @Valid MateriPpmRequest request) {
        return service.update(uuid, request);
    }

    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Soft delete materi ppm.")
    public Response delete(@PathParam("uuid") UUID uuid) {
        service.delete(uuid);
        return Response.noContent().build();
    }
}
