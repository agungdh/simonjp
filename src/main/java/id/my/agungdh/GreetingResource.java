package id.my.agungdh;

import java.time.OffsetDateTime;

import id.my.agungdh.dto.HelloDTO;
import io.quarkus.logging.Log;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RunOnVirtualThread
    public HelloDTO hello() {
        Thread thread = Thread.currentThread();
        Log.infof("GET /hello running on %s", thread);
        return new HelloDTO(
                "Hello from Quarkus REST",
                OffsetDateTime.now(),
                thread.toString(),
                thread.isVirtual());
    }
}
