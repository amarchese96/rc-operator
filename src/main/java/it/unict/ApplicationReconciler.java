package it.unict;

import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import it.unict.affinity.AffinityController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class ApplicationReconciler implements Reconciler<Application> {

  private static final Logger log = LoggerFactory.getLogger(ApplicationReconciler.class);
  private final KubernetesClient client;

  @Inject
  private AffinityController affinityController;

  public ApplicationReconciler(KubernetesClient client) {
    this.client = client;
  }

  @Override
  public UpdateControl<Application> reconcile(Application resource, Context context) {
    DeploymentList deploymentList = client
            .apps()
            .deployments()
            .inNamespace(resource.getSpec().getNamespace())
            .withLabel("app", resource.getSpec().getName())
            .list();

    affinityController.updateAffinities(deploymentList.getItems(), resource.getSpec().getTopologyKey());

    deploymentList.getItems().forEach(d -> {
      client.apps().deployments().inNamespace(d.getMetadata().getNamespace()).patch(d);
    });

    return UpdateControl.<Application>noUpdate().rescheduleAfter(resource.getSpec().getRescheduleDelay(), TimeUnit.SECONDS);
  }
}

