package dev.kameshs;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static java.util.logging.Level.*;

/**
 * jo
 */
@Command(name = "apply", mixinStandardHelpOptions = true,
    description = "Applies a Kube minfest with jbang", version = "0.0.1")
public class App implements Callable<Integer> {

  private static final Logger LOGGER = Logger.getLogger(App.class.getName());

  @Option(names = {"-f", "--file"}, required = true,
      description = "The Kubernetes manifest that has jbang URI ")
  private File mainfest;

  final KubernetesClient client = new DefaultKubernetesClient();

  public static void main(String[] args) {
    int exitCode = new CommandLine(new App()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    //TODO Identify the kind and load the respective model
    Deployment deployment = client.apps().deployments().load(mainfest).get();
    Container container =
        deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
    String imageURI = container.getImage();
    LOGGER.log(INFO, "Image URI {0}", imageURI);
    return 0;
  }
}
