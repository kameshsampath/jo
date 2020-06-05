package dev.kameshs.utils;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import io.fabric8.knative.client.DefaultKnativeClient;
import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

@ApplicationScoped
public class ClientProducer {

  @Produces
  @Named("kubernetes-client")
  public KubernetesClient kubernetesClient() {
    return new DefaultKubernetesClient();
  }

  @Produces
  @Named("knative-client")
  public KnativeClient knativeClient() {
    return new DefaultKnativeClient();
  }
}
