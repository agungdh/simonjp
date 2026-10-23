package id.my.agungdh.resource;

import java.net.URI;
import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import id.my.agungdh.domain.enums.KategoriJabatan;
import id.my.agungdh.domain.enums.KategoriKebutuhanJamPelatihan;
import id.my.agungdh.domain.enums.StatusPegawai;
import id.my.agungdh.domain.enums.TipePegawai;
import id.my.agungdh.dto.CursorPageDTO;
import id.my.agungdh.dto.PegawaiRequest;
import id.my.agungdh.dto.PegawaiResponse;
import id.my.agungdh.service.PegawaiService;
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

@Path("/pegawai")
@RunOnVirtualThread
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Pegawai", description = "Data pegawai.")
public class PegawaiResource {

    private final PegawaiService service;

    @Inject
    public PegawaiResource(PegawaiService service) {
        this.service = service;
    }

    @GET
    @Operation(summary = "List pegawai dengan cursor pagination dan filter.")
    public CursorPageDTO<PegawaiResponse> list(
            @QueryParam("search") String search,
            @QueryParam("tipe") TipePegawai tipe,
            @QueryParam("status") StatusPegawai status,
            @QueryParam("bidangUuid") UUID bidangUuid,
            @QueryParam("pangkatGolonganUuid") UUID pangkatGolonganUuid,
            @QueryParam("kategoriJabatan") KategoriJabatan kategoriJabatan,
            @QueryParam("kategoriKebutuhanJamPelatihan") KategoriKebutuhanJamPelatihan kategoriKebutuhanJamPelatihan,
            @QueryParam("cursor") UUID cursor,
            @QueryParam("limit") @DefaultValue("20") int limit) {
        return service.list(search, tipe, status, bidangUuid, pangkatGolonganUuid, kategoriJabatan,
                kategoriKebutuhanJamPelatihan, cursor, limit);
    }

    @GET
    @Path("/{uuid}")
    @Operation(summary = "Ambil satu pegawai berdasarkan uuid.")
    public PegawaiResponse get(@PathParam("uuid") UUID uuid) {
        return service.get(uuid);
    }

    @POST
    @Operation(summary = "Buat pegawai baru.")
    public Response create(@Valid PegawaiRequest request) {
        PegawaiResponse body = service.create(request);
        return Response.created(URI.create("/pegawai/" + body.uuid())).entity(body).build();
    }

    @PUT
    @Path("/{uuid}")
    @Operation(summary = "Ganti seluruh data pegawai (PUT full replace).")
    public PegawaiResponse update(@PathParam("uuid") UUID uuid, @Valid PegawaiRequest request) {
        return service.update(uuid, request);
    }

    @DELETE
    @Path("/{uuid}")
    @Operation(summary = "Soft delete pegawai.")
    public Response delete(@PathParam("uuid") UUID uuid) {
        service.delete(uuid);
        return Response.noContent().build();
    }
}
