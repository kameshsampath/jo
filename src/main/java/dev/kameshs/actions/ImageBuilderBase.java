package dev.kameshs.actions;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.jkube.kit.build.service.docker.RegistryConfig;
import org.eclipse.jkube.kit.build.service.docker.ServiceHub;
import org.eclipse.jkube.kit.build.service.docker.ServiceHubFactory;
import org.eclipse.jkube.kit.build.service.docker.auth.AuthConfigFactory;
import org.eclipse.jkube.kit.common.JavaProject;
import org.eclipse.jkube.kit.common.KitLogger;
import org.eclipse.jkube.kit.common.util.Slf4jKitLogger;
import org.eclipse.jkube.kit.config.JKubeConfiguration;
import org.eclipse.jkube.kit.config.service.BuildServiceConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.kameshs.service.GitHubContentService;
import dev.kameshs.utils.ResolverUtil;

public abstract class ImageBuilderBase {

  private static final Logger LOGGER =
      LoggerFactory.getLogger("dev.kameshs.actions.ImageBuilder");

  @Inject
  protected ResolverUtil imageResolverUtil;

  @Inject
  @RestClient
  protected GitHubContentService ghContentService;

  @ConfigProperty(name = "dev.kameshs.jo-container-repo")
  protected Optional<String> joContainerRepo;

  protected KitLogger kitLogger;
  protected ServiceHub serviceHub;
  protected JKubeConfiguration configuration;
  protected BuildServiceConfig dockerBuildServiceConfig;

  protected ImageBuilderBase() {
    kitLogger = new Slf4jKitLogger(LOGGER);
    kitLogger.info(
        "Initiating default JKube configuration and required services...");
    kitLogger.info(" - Creating Docker Service Hub");
    serviceHub = new ServiceHubFactory().createServiceHub(kitLogger);
    kitLogger.info(" - Creating Docker Build Service Configuration");
    dockerBuildServiceConfig = BuildServiceConfig.builder().build();
    kitLogger.info(" - Creating configuration for JKube");
    configuration = JKubeConfiguration.builder()
        .project(JavaProject.builder()
            .baseDirectory(Paths.get("").toAbsolutePath().toFile()).build())
        .outputDirectory("target").build();
  }

  public abstract Optional<String> newBuild(String strImageUri)
      throws Exception;

  // TODO #7 Ability to configure the external registries
  protected RegistryConfig registryConfig() {
    return RegistryConfig
        .builder()
        .settings(Collections.emptyList())
        .authConfigFactory(new AuthConfigFactory(kitLogger))
        .build();
  }
}
