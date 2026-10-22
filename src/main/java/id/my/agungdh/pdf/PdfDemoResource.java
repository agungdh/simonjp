package id.my.agungdh.pdf;

import java.io.IOException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@Path("/pdf")
public class PdfDemoResource {

    @GET
    @Path("/contoh")
    @Produces("application/pdf")
    public Response contoh() throws IOException {
        log.infof("GET /pdf/contoh running on %s", Thread.currentThread());
        byte[] pdf = PdfDemoGenerator.build();
        return Response.ok(pdf)
                .type("application/pdf")
                .header("Content-Disposition", "inline; filename=\"contoh.pdf\"")
                .build();
    }
}
