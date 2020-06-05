package dev.kameshs.commands;

import java.io.File;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.kameshs.actions.ImageBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;


@Command(name = "apply", mixinStandardHelpOptions = true,
    description = "Applies a Kube minfest with jbang", version = "0.0.1")
public class ApplyCommand implements Runnable {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ApplyCommand.class);

  @Option(names = {"-f", "--file"}, required = true,
      description = "The Kubernetes manifest that has jbang URI ")
  private File mainfest;

  @Inject
  KubernetesClient client;

  @Inject
  ImageBuilder imageBuilder;

  public void run() {
    //TODO Identify the kind and load the right model
    Deployment deployment = client.apps().deployments().load(mainfest).get();
    Container container =
        deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
    String imageURI = container.getImage();
    LOGGER.debug("Image URI {}", imageURI);
    try {
      imageBuilder.build(imageURI);
    } catch (Exception e) {
      LOGGER.error("Error applying the manifest", e);
    }
  }
}
