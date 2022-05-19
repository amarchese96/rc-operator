package it.unict.affinity;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ApplicationScoped
public class RandomAffinityController implements AffinityController {

    @Override
    public void updateAffinities(List<Deployment> deploymentList, String topologyKey) {
        deploymentList.forEach(d -> {
            List<WeightedPodAffinityTerm> podAffinityTerms = new ArrayList<>();
            deploymentList.forEach(od -> {
                if(od.getMetadata().getName() != d.getMetadata().getName()) {
                    WeightedPodAffinityTerm wpat = new WeightedPodAffinityTermBuilder()
                            .withWeight((int)(Math.random()*100) + 1)
                            .withPodAffinityTerm(new PodAffinityTermBuilder()
                                    .withLabelSelector(new LabelSelectorBuilder()
                                            .withMatchLabels(
                                                    new HashMap<>() {{
                                                        put("app", d.getMetadata().getLabels().get("app"));
                                                        put("node", od.getMetadata().getLabels().get("node"));
                                                    }}
                                            ).build())
                                    .withTopologyKey(topologyKey)
                                    .build())
                            .build();
                    podAffinityTerms.add(wpat);
                }
            });
            d.getSpec()
                    .getTemplate()
                    .getSpec()
                    .setAffinity(new AffinityBuilder()
                            .withPodAffinity(new PodAffinityBuilder()
                                    .withPreferredDuringSchedulingIgnoredDuringExecution(podAffinityTerms)
                                    .build())
                            .build());
        });
    }
}
