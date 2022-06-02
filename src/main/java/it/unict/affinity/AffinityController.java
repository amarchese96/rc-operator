package it.unict.affinity;

import io.fabric8.kubernetes.api.model.apps.Deployment;

import java.util.List;

public abstract class AffinityController {

    protected final Integer minWeight = 1;

    protected final Integer maxWeight = 100;

    public abstract void updateAffinities(List<Deployment> deploymentList, String topologyKey);
}
