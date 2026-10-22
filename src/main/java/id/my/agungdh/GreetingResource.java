package id.my.agungdh;

import java.time.OffsetDateTime;

import id.my.agungdh.dto.HelloDTO;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@RunOnVirtualThread
@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
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
