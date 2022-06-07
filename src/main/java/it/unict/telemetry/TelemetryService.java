package it.unict.telemetry;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Map;

@RegisterRestClient(configKey = "telemetry-api")
public interface TelemetryService {

    @GET
    @Path("/metrics/svc/avg-traffic")
    Uni<Map<String,Float>> getAvgSvcTraffic(@QueryParam("app") String app, @QueryParam("svc") String service);
}
