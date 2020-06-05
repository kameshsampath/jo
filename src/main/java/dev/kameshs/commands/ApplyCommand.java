package dev.kameshs.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.kameshs.actions.ImageBuilder;
import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
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
  private File manifest;

  @Inject
  @Named("kubernetes-client")
  KubernetesClient kubernetesClient;

  @Inject
  @Named("knative-client")
  KnativeClient knativeClient;

  @Inject
  ImageBuilder imageBuilder;

  @Override
  public void run() {

    List<HasMetadata> metadataResources;

    try (InputStream fin = new FileInputStream(manifest)) {
      metadataResources = kubernetesClient.load(fin).get();
      if (metadataResources != null) {

        metadataResources.stream()
            .filter(m -> "Deployment".equalsIgnoreCase(m.getKind()))
            .map(Deployment.class::cast)
            .forEach(this::doDeployment);
      }
    } catch (IOException e) {
      LOGGER.error("Error applying manifests", e);
    }

  }

  protected void doDeployment(Deployment deployment) {

    var currentNamespace = kubernetesClient.getNamespace();

    if (currentNamespace == null) {
      currentNamespace = "default";
    }

    var manifestNamespace =
        deployment.getMetadata().getNamespace() == null ? currentNamespace
            : deployment
                .getMetadata().getNamespace();

    Container container =
        deployment.getSpec()
            .getTemplate()
            .getSpec()
            .getContainers()
            .get(0);

    String imageURI = container.getImage();

    LOGGER.debug("Image URI {}", imageURI);

    try {
      Optional<String> builtImage = imageBuilder.newBuild(imageURI);
      if (builtImage.isPresent()) {

        LOGGER.debug("Built Image URI {}", builtImage.get());

        container.setImage(builtImage.get());

        deployment.getSpec()
            .getTemplate()
            .getSpec()
            .getContainers()
            .set(0, container);

        kubernetesClient.apps()
            .deployments()
            .inNamespace(manifestNamespace)
            .createOrReplace(deployment);

        LOGGER.info("Applied Deployment {} in Namespace {}  ",
            deployment.getMetadata().getName(), currentNamespace);
      }

    } catch (Exception e) {
      LOGGER.error("Error applying the manifest", e);
    }
  }

}
