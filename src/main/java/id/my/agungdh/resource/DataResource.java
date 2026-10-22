package id.my.agungdh.resource;

import java.util.List;

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
@Path("/contoh/data")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DataResource {

    private final SalamService salamService;

    @GET
    public List<SalamDTO> semua() {
        log.info("GET /contoh/data");
        return salamService.semua();
    }

    @GET
    @Path("/{id}")
    public SalamDTO satu(@PathParam("id") Long id) {
        return salamService.cari(id)
                .orElseThrow(() -> new NotFoundException("Salam dengan id " + id + " tidak ditemukan"));
    }

    @POST
    public Response tambah(SalamRequest request) {
        SalamDTO dibuat = salamService.tambah(request);
        return Response.status(Response.Status.CREATED)
                .entity(dibuat)
                .header("Location", "/contoh/data/" + dibuat.id())
                .build();
    }
}
