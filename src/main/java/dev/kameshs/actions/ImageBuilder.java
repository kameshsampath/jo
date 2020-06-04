package dev.kameshs.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.jkube.kit.build.service.docker.ImageConfiguration;
import org.eclipse.jkube.kit.build.service.docker.ServiceHub;
import org.eclipse.jkube.kit.build.service.docker.ServiceHubFactory;
import org.eclipse.jkube.kit.common.Assembly;
import org.eclipse.jkube.kit.common.AssemblyFile;
import org.eclipse.jkube.kit.common.JavaProject;
import org.eclipse.jkube.kit.common.KitLogger;
import org.eclipse.jkube.kit.config.JKubeConfiguration;
import org.eclipse.jkube.kit.config.image.build.Arguments;
import org.eclipse.jkube.kit.config.image.build.AssemblyConfiguration;
import org.eclipse.jkube.kit.config.image.build.BuildConfiguration;
import org.eclipse.jkube.kit.config.resource.RuntimeMode;
import org.eclipse.jkube.kit.config.service.BuildServiceConfig;
import org.eclipse.jkube.kit.config.service.JKubeServiceHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.kameshs.utils.GitHubServiceUtil;
import dev.kameshs.utils.ImageResolverUtil;

public class ImageBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(
      ImageBuilder.class.getName());
  public static final String JO_CONTAINER_REPO = "dev.local";

  static final String BASE_IMAGE = "quay.io/jbangdev/jbang-action";


  final KitLogger kitLogger;
  final ServiceHub serviceHub;
  final JKubeConfiguration configuration;
  final BuildServiceConfig dockerBuildServiceConfig;

  public ImageBuilder() {
    kitLogger = new KitLogger.StdoutLogger();
    kitLogger.info(
        "Initiating default JKube configuration and required services...");
    kitLogger.info(" - Creating Docker Service Hub");
    serviceHub = new ServiceHubFactory().createServiceHub(
        kitLogger);
    kitLogger.info(" - Creating Docker Build Service Configuration");
    dockerBuildServiceConfig = BuildServiceConfig
        .builder()
        .build();
    kitLogger.info(" - Creating configuration for JKube");
    kitLogger.info(" - Current working directory is: %s",
        getProjectDir().toFile().toString());
    configuration = JKubeConfiguration.builder()
        .project(JavaProject.builder()
            .baseDirectory(getProjectDir().toFile())
            .build())
        .outputDirectory("scripts")
        .build();

  }

  public Optional<String> build(String strImageUri) throws Exception {
    String imageName = null;
    //TODO #2 #1 Resolve base image based on the URI
    URI uri = new URI(strImageUri);
    Optional<String> optFromImage = ImageResolverUtil.resolveFromImage(uri);
    if (!optFromImage.isPresent()) {
      throw new IllegalStateException(
          "Unable to find base image for URI " + uri);
    } else {
      String fromImage = optFromImage.get();
      final String path = uri.getPath();
      LOGGER.debug("Image Host {} and Path {}",
          new Object[] {uri.getHost(), path});

      String[] segments = path.split("/");
      String jbangScriptSegment = segments[segments.length - 1];

      imageName = jbangScriptSegment;
      String downloadURL = "https://" + uri.getHost() + path;

      String destinationFile = path.replaceAll("/", "-").substring(1);

      String jbangExecScript = "/scripts/" + jbangScriptSegment;

      if (jbangScriptSegment.endsWith(".java")) {
        imageName =
            jbangScriptSegment.substring(0,
                jbangScriptSegment.lastIndexOf("."));
      } else {
        downloadURL += ".java";
        destinationFile += ".java";
        jbangExecScript += ".java";
      }

      Optional<File> downloadedSource =
          downloadScriptFile(segments, downloadURL, destinationFile);

      if (downloadedSource.isPresent()) {

        LOGGER.debug("Script Name: {} ", jbangExecScript);

        //Copy the script file to destination folder
        AssemblyFile scriptFile =
            AssemblyFile.builder()
                .source(downloadedSource.get())
                .build();

        AssemblyConfiguration scriptAssembly = AssemblyConfiguration
            .builder()
            .targetDir("/scripts")
            .inline(Assembly.builder().files(Arrays.asList(
                scriptFile))
                .build())
            .build();

        //Image build Configuration
        BuildConfiguration bc = BuildConfiguration
            .builder()
            .from(fromImage)
            .cmd(Arguments.builder()
                .shell(jbangExecScript)
                .build())
            .assembly(scriptAssembly)
            .port("8080")
            .build();

        final ImageConfiguration imageConfiguration = ImageConfiguration
            .builder()
            .name(imageName)
            .build(bc)
            .build();

        JKubeServiceHub jKubeServiceHub = JKubeServiceHub.builder()
            .log(kitLogger)
            .configuration(configuration)
            .platformMode(RuntimeMode.kubernetes)
            .dockerServiceHub(serviceHub)
            .buildServiceConfig(dockerBuildServiceConfig)
            .build();
        jKubeServiceHub.getBuildService().build(imageConfiguration);

        final String imageId = jKubeServiceHub.getDockerServiceHub()
            .getDockerAccess().getImageId(imageName);

        kitLogger.info("Docker image built successfully (%s)!", imageId);
      }
    }
    return Optional.ofNullable(imageName);
  }

  //TODO #3 hande other sources, for now only GitHub
  private Optional<File> downloadScriptFile(String[] segments,
      String downloadURL,
      String destinationFile) {
    File file = null;
    try {
      String repoOwner = segments[1];
      String repo = segments[2];
      String filePath =
          String.join("/", Arrays.copyOfRange(segments, 3, segments.length));

      if (!filePath.endsWith(".java")) {
        filePath += ".java";
      }

      LOGGER.debug("Repo {} Repo-Owner{} Filepath {} ", repoOwner, repo,
          filePath);

      Optional<String> optContent =
          GitHubServiceUtil.githubFileContent(repoOwner, repo, filePath);

      if (optContent.isPresent()) {
        LOGGER.debug("Downloaded content:{}", optContent.get());
        file = new File(configuration.getOutputDirectory(), destinationFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(optContent.get());
        writer.close();
      }

    } catch (Exception e) {
      LOGGER.error("Error downloading file {}", downloadURL, e);
      file = null;
    }
    return Optional.ofNullable(file);
  }

  private static Path getProjectDir() {
    final Path currentWorkDir = Paths.get("");
    if (currentWorkDir.toAbsolutePath().endsWith("docker-image")) {
      return currentWorkDir.toAbsolutePath();
    }
    return currentWorkDir.resolve("jo").resolve("kit")
        .resolve("docker-image");
  }
}
