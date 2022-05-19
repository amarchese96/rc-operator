package it.unict.affinity;

import io.fabric8.kubernetes.api.model.apps.Deployment;

import java.util.List;

public interface AffinityController {

    void updateAffinities(List<Deployment> deploymentList, String topologyKey);
}
