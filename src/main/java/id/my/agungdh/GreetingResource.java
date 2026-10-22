package id.my.agungdh;

import java.time.OffsetDateTime;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import id.my.agungdh.dto.HelloDTO;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@RunOnVirtualThread
@Tag(name = "Hello", description = "Contoh endpoint sederhana yang berjalan di virtual thread")
@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Sapa", description = "Mengembalikan pesan sapaan beserta info thread yang melayani request.")
    @APIResponse(responseCode = "200", description = "Sapaan berhasil dibuat",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = HelloDTO.class)))
    public HelloDTO hello() {
        Thread thread = Thread.currentThread();
        log.infof("GET /hello running on %s", thread);
        return new HelloDTO(
                "Hello from Quarkus REST",
                OffsetDateTime.now(),
                thread.toString(),
                thread.isVirtual());
    }
}
