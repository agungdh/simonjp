package id.my.agungdh.resource;

import java.util.List;

import id.my.agungdh.dto.SalamDTO;
import id.my.agungdh.dto.SalamRequest;
import id.my.agungdh.service.SalamService;
import io.quarkus.logging.Log;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Contoh endpoint yang selalu berbicara dalam DTO record, bukan entity.
 */
@Path("/contoh/data")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DataResource {

    private final SalamService salamService;

    public DataResource(SalamService salamService) {
        this.salamService = salamService;
    }

    @GET
    public List<SalamDTO> semua() {
        Log.info("GET /contoh/data");
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
