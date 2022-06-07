package it.unict.affinity;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import it.unict.telemetry.TelemetryService;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class TrafficAwareAffinityController extends AffinityController {

    private static final Logger log = LoggerFactory.getLogger(TrafficAwareAffinityController.class);

    @RestClient
    TelemetryService telemetryService;

    @Override
    public void updateAffinities(List<Deployment> deploymentList, String topologyKey) {
        deploymentList.forEach(d -> {
            List<WeightedPodAffinityTerm> podAffinityTerms = getPodAffinityTerms(d, topologyKey);
            d.getSpec()
                    .getTemplate()
                    .getSpec()
                    .setAffinity(new AffinityBuilder()
                            .withPodAffinity(new PodAffinityBuilder()
                                    .withPreferredDuringSchedulingIgnoredDuringExecution(podAffinityTerms)
                                    .build())
                            .build());
        });
        log.info("--------------------------------");
    }

    private List<WeightedPodAffinityTerm> getPodAffinityTerms(Deployment deployment, String topologyKey) {
        List<WeightedPodAffinityTerm> podAffinityTerms = new ArrayList<>();

        Map<String,Float> trafficValues = telemetryService.getAvgSvcTraffic(
                deployment.getMetadata().getLabels().get("app"),
                deployment.getMetadata().getLabels().get("svc")
        ).await().indefinitely();

        if(!trafficValues.isEmpty()) {
            Integer highest = Collections.max(trafficValues.values()).intValue();
            Integer lowest = Collections.min(trafficValues.values()).intValue();
            Integer oldRange = highest - lowest;
            Integer newRange = maxWeight - minWeight;

            trafficValues.entrySet().forEach(entry -> {
                log.info("{}-{}: {}",
                        deployment.getMetadata().getLabels().get("svc"),
                        entry.getKey(),
                        entry.getValue()
                );
                Integer weight = (oldRange == 0) ? minWeight : ((entry.getValue().intValue() - lowest) * newRange / oldRange) + minWeight;
                WeightedPodAffinityTerm wpat = new WeightedPodAffinityTermBuilder()
                        .withWeight(weight)
                        .withPodAffinityTerm(new PodAffinityTermBuilder()
                                .withLabelSelector(new LabelSelectorBuilder()
                                        .withMatchLabels(
                                                new HashMap<>() {{
                                                    put("app", deployment.getMetadata().getLabels().get("app"));
                                                    put("svc", entry.getKey());
                                                }}
                                        ).build())
                                .withTopologyKey(topologyKey)
                                .build())
                        .build();
                podAffinityTerms.add(wpat);
            });
        }

        log.info("-----");
        return podAffinityTerms;
    }
}
