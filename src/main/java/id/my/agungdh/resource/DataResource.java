package id.my.agungdh.resource;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import id.my.agungdh.dto.SalamDTO;
import id.my.agungdh.dto.SalamRequest;
import id.my.agungdh.service.SalamService;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

/**
 * Contoh endpoint yang selalu berbicara dalam DTO record, bukan entity.
 */
@JBossLog
@RequiredArgsConstructor
@RunOnVirtualThread
@Tag(name = "Salam", description = "CRUD in-memory yang hanya mengekspos DTO")
@Path("/contoh/data")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DataResource {

    private final SalamService salamService;

    @GET
    @Operation(summary = "Ambil semua data", description = "Mengembalikan seluruh data salam sebagai DTO.")
    @APIResponse(responseCode = "200", description = "Daftar data salam")
    public List<SalamDTO> semua() {
        log.info("GET /contoh/data");
        return salamService.semua();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Ambil satu data", description = "Mengembalikan satu data salam berdasarkan id.")
    @APIResponse(responseCode = "200", description = "Data salam ditemukan",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SalamDTO.class)))
    @APIResponse(responseCode = "404", description = "Id tidak ditemukan")
    public SalamDTO satu(
            @Parameter(description = "Id data salam", required = true) @PathParam("id") Long id) {
        return salamService.cari(id)
                .orElseThrow(() -> new NotFoundException("Salam dengan id " + id + " tidak ditemukan"));
    }

    @POST
    @Operation(summary = "Tambah data", description = "Membuat data salam baru dari request DTO.")
    @APIResponse(responseCode = "201", description = "Data berhasil dibuat",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = SalamDTO.class)))
    @APIResponse(responseCode = "400", description = "Request tidak valid")
    public Response tambah(SalamRequest request) {
        SalamDTO dibuat = salamService.tambah(request);
        return Response.status(Response.Status.CREATED)
                .entity(dibuat)
                .header("Location", "/contoh/data/" + dibuat.id())
                .build();
    }
}
