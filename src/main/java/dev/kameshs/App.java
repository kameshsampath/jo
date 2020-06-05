package dev.kameshs;

import javax.inject.Inject;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;


/**
 * jo
 */
@QuarkusMain
public class App implements QuarkusApplication {

  @Inject
  CommandLine.IFactory factory;

  final KubernetesClient client = new DefaultKubernetesClient();

  public int run(String... args) {
    return new CommandLine(this, factory).execute(args);
  }
}
