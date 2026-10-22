package id.my.agungdh.pdf;

import java.io.IOException;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@Tag(name = "PDF", description = "Contoh generator PDF")
@Path("/pdf")
public class PdfDemoResource {

    @GET
    @Path("/contoh")
    @Produces("application/pdf")
    @Operation(summary = "Unduh contoh PDF", description = "Membangkitkan PDF contoh berisi font, gambar, dan chart.")
    @APIResponse(responseCode = "200", description = "File PDF berhasil dibuat",
            content = @Content(mediaType = "application/pdf",
                    schema = @Schema(type = SchemaType.STRING, format = "binary")))
    public Response contoh() throws IOException {
        log.infof("GET /pdf/contoh running on %s", Thread.currentThread());
        byte[] pdf = PdfDemoGenerator.build();
        return Response.ok(pdf)
                .type("application/pdf")
                .header("Content-Disposition", "inline; filename=\"contoh.pdf\"")
                .build();
    }
}
