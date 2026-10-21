package id.my.agungdh.office;

import java.io.IOException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/excel")
public class ExcelDemoResource {

    @GET
    @Path("/contoh")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response contoh() throws IOException {
        byte[] xlsx = ExcelDemoGenerator.build();
        return Response.ok(xlsx)
                .type("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header("Content-Disposition", "inline; filename=\"contoh.xlsx\"")
                .build();
    }
}
