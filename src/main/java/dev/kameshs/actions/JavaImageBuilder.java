package dev.kameshs.actions;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.jkube.kit.build.service.docker.ImageConfiguration;
import org.eclipse.jkube.kit.common.Assembly;
import org.eclipse.jkube.kit.common.AssemblyConfiguration;
import org.eclipse.jkube.kit.common.AssemblyFileSet;
import org.eclipse.jkube.kit.config.image.build.BuildConfiguration;
import org.eclipse.jkube.kit.config.resource.RuntimeMode;
import org.eclipse.jkube.kit.config.service.JKubeServiceHub;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.kameshs.data.JavaRepoInfo;
import dev.kameshs.data.JitPackBuild;
import dev.kameshs.service.JitPackBuildService;
import dev.kameshs.service.MavenArtifactService;
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

  @Inject
  MavenArtifactService mavenArtifactService;

  public Optional<String> newBuild(String strImageUri) {
    String imageName = null;

    try {

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

      imageName =
          joContainerRepo.map(r -> String.join("/", r, repoInfo.name))
              .orElse("localhost:5000/" + repoInfo.name);

      // TODO - need to rest it well
      // Ensure Build is there and available for download
      LOGGER.info("Using version {}", repoInfo.ref);
      JitPackBuild build = jitPackBuildService
          .buildByTag(groupId, repoInfo.name, repoInfo.ref);

      if ("ok".equalsIgnoreCase(build.status)) {

        boolean isQuarkus = "quarkus".equals(uri.getScheme());

        Optional<File> downloadedArtifact =
            downloadArtifact(isQuarkus, repoInfo);

        downloadedArtifact.orElseThrow(() -> new IllegalStateException(
            "Unable to download image artifact for " + strImageUri));


        if (isNotMaster(repoInfo.ref)) {
          imageName = imageName + ":" + build.version;
        }

        if (downloadedArtifact.isPresent()) {

          // Copy the script file to destination folder
          AssemblyFileSet fileSet = AssemblyFileSet.builder()
              .directory(downloadedArtifact.get()).fileMode("0777").build();

          AssemblyConfiguration jarAssembly =
              AssemblyConfiguration
                  .builder()
                  .targetDir("/deployments")
                  .inline(Assembly
                      .builder()
                      .fileSet(fileSet)
                      .build())
                  .build();

          // Image build Configuration
          BuildConfiguration bc = BuildConfiguration
              .builder()
              .from(fromImage)
              .assembly(jarAssembly)
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
      } else {
        throw new IllegalStateException("Unable to deploy app with URI " +
            strImageUri);
      }
    } catch (Exception e) {
      LOGGER.error("Error building Java Image {}", strImageUri, e);
      imageName = null;
    }
    return Optional.ofNullable(imageName);
  }

  // TODO #8 Differentiate SB vs Quarkus using plugins
  private Optional<File> downloadArtifact(boolean isQuarkus,
      JavaRepoInfo repo) throws Exception {
    var groupId = "com.github." + repo.owner;
    if (isQuarkus) {
      return mavenArtifactService.resolveArtifact(groupId, repo.name, "jar",
          repo.ref, "runner");
    }
    return mavenArtifactService.resolveArtifact(groupId, repo.name, "jar",
        repo.ref);
  }

  private boolean isNotMaster(String ref) {
    return !"master-SNAPSHOT".equals(ref);
  }

}
