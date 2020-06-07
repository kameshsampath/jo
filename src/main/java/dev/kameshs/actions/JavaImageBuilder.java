package dev.kameshs.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.spi.connector.ArtifactDownload;
import org.eclipse.jkube.kit.build.service.docker.ImageConfiguration;
import org.eclipse.jkube.kit.common.Assembly;
import org.eclipse.jkube.kit.common.AssemblyConfiguration;
import org.eclipse.jkube.kit.common.AssemblyFileSet;
import org.eclipse.jkube.kit.config.image.build.Arguments;
import org.eclipse.jkube.kit.config.image.build.BuildConfiguration;
import org.eclipse.jkube.kit.config.resource.RuntimeMode;
import org.eclipse.jkube.kit.config.service.JKubeServiceHub;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.kameshs.data.JavaRepoInfo;
import dev.kameshs.data.JitPackBuild;
import dev.kameshs.service.JitPackBuildService;
import dev.kameshs.utils.URLUtils;

@ApplicationScoped
public class JavaImageBuilder extends ImageBuilderBase {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(JavaImageBuilder.class.getName());

  @Inject
  URLUtils urlUtils;

  @Inject
  @RestClient
  JitPackBuildService jitPackBuildService;

  public Optional<String> newBuild(String strImageUri) throws Exception {
    String imageName = null;
    URI uri = new URI(strImageUri);
    final String fromImage = imageResolverUtil.resolveFromImage(uri)
        .orElseThrow(() -> new IllegalStateException(
            "Unable to find base image for URI " + uri));
    final String path = uri.getPath();
    LOGGER.debug("Image Host {} and Path {}", uri.getHost(), path);

    Optional<JavaRepoInfo> rOptional = urlUtils.repoInfoFromURI(strImageUri);

    rOptional.orElseThrow(() -> new IllegalStateException(
        "Not able to infer Repo info form " + strImageUri));

    var repoInfo = rOptional.get();
    var groupId = "com.github." + repoInfo.owner;

    // Ensure Build is there and available for download
    JitPackBuild build =
        jitPackBuildService
            .buildByTag(groupId, repoInfo.name, repoInfo.ref);

    if ("ok".equalsIgnoreCase(build.status)) {

      Optional<File> downloadedSource = Optional.empty();

      if (downloadedSource.isPresent()) {

        LOGGER.debug("Script Name: {} ", "jbangExecScript");

        // Copy the script file to destination folder
        AssemblyFileSet fileSet = AssemblyFileSet.builder()
            .directory(downloadedSource.get()).fileMode("0777").build();

        AssemblyConfiguration scriptAssembly =
            AssemblyConfiguration
                .builder()
                .targetDir("/scripts")
                .inline(Assembly
                    .builder()
                    .fileSet(fileSet)
                    .build())
                .build();

        // Image build Configuration
        BuildConfiguration bc = BuildConfiguration
            .builder()
            .from(fromImage)
            .cmd(Arguments.builder()
                .execArgument("jbangExecScript")
                .build())
            .assembly(scriptAssembly)
            .port("8080")
            .build();

        final ImageConfiguration imageConfiguration =
            ImageConfiguration
                .builder()
                .name(imageName)
                .build(bc)
                .build();

        JKubeServiceHub jKubeServiceHub =
            JKubeServiceHub
                .builder()
                .log(kitLogger)
                .configuration(configuration)
                .platformMode(RuntimeMode.kubernetes)
                .dockerServiceHub(serviceHub)
                .buildServiceConfig(dockerBuildServiceConfig)
                .build();

        jKubeServiceHub.getBuildService().build(imageConfiguration);

        final String imageId = jKubeServiceHub
            .getDockerServiceHub()
            .getDockerAccess()
            .getImageId(imageName);


        kitLogger.debug("Docker image built successfully (%s)!", imageId);


        // Push the image to registry
        kitLogger.info("Pushing image (%s)  to registry", imageName);

        jKubeServiceHub
            .getDockerServiceHub()
            .getRegistryService()
            .pushImages(
                Collections.singletonList(imageConfiguration), 0,
                registryConfig(),
                false);
      }
    }
    return Optional.ofNullable(imageName);
  }

  // TODO #3 handle other sources, for now only GitHub
  private Optional<File> downloadArtifact(JavaRepoInfo repoInfo) {
    File file = null;
    return Optional.ofNullable(file);
  }

}
