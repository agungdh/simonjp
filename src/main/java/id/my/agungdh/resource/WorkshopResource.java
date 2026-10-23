package id.my.agungdh.resource;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import id.my.agungdh.dto.WorkshopRequest;
import id.my.agungdh.dto.WorkshopResponse;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.service.WorkshopService;
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

@Path("/workshop")
@RunOnVirtualThread
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Workshop", description = "Data Workshop.")
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class WorkshopResource {

    private final WorkshopService service;

    @GET
    @Operation(summary = "List Workshop dengan cursor pagination.")
    public CursorPageDTO<WorkshopResponse> list(
            @QueryParam("search") String search,
            @QueryParam("pegawaiUuid") UUID pegawaiUuid,
            @QueryParam("dariTanggal") LocalDate dariTanggal,
            @QueryParam("sampaiTanggal") LocalDate sampaiTanggal,
            @QueryParam("tahun") Integer tahun,
            @QueryParam("cursor") UUID cursor,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        return service.list(search, pegawaiUuid, dariTanggal, sampaiTanggal, tahun, cursor, limit);
    }

    @GET
    @Path("/{uuid}")
    @Operation(summary = "Ambil satu Workshop berdasarkan uuid.")
    public WorkshopResponse get(@PathParam("uuid") UUID uuid) {
        return service.get(uuid);
    }

    @POST
    @Operation(summary = "Buat Workshop baru.")
    public Response create(@Valid WorkshopRequest request) {
        WorkshopResponse body = service.create(request);
        return Response.created(URI.create("/workshop/" + body.uuid())).entity(body).build();
    }

    @PUT
    @Path("/{uuid}")
    @Operation(summary = "Ganti seluruh data Workshop (PUT full replace).")
    public WorkshopResponse update(@PathParam("uuid") UUID uuid, @Valid WorkshopRequest request) {
        return service.update(uuid, request);
    }

    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Soft delete Workshop.")
    public Response delete(@PathParam("uuid") UUID uuid) {
        service.delete(uuid);
        return Response.noContent().build();
    }
}
