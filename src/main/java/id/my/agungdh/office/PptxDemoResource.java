package id.my.agungdh.office;

import java.io.IOException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/ppt")
public class PptxDemoResource {

    private static final String CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.presentationml.presentation";

    @GET
    @Path("/contoh")
    @Produces(CONTENT_TYPE)
    public Response contoh() throws IOException {
        byte[] pptx = PptxDemoGenerator.build();
        return Response.ok(pptx)
                .type(CONTENT_TYPE)
                .header("Content-Disposition", "inline; filename=\"contoh.pptx\"")
                .build();
    }
}
