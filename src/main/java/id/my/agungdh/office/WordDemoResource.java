package id.my.agungdh.office;

import java.io.IOException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/word")
public class WordDemoResource {

    @GET
    @Path("/contoh")
    @Produces("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    public Response contoh() throws IOException {
        byte[] docx = WordDemoGenerator.build();
        return Response.ok(docx)
                .type("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .header("Content-Disposition", "inline; filename=\"contoh.docx\"")
                .build();
    }
}
