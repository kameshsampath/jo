package dev.kameshs.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.eclipse.jkube.kit.build.service.docker.ImageConfiguration;
import org.eclipse.jkube.kit.common.Assembly;
import org.eclipse.jkube.kit.common.AssemblyFile;
import org.eclipse.jkube.kit.config.image.build.Arguments;
import org.eclipse.jkube.kit.config.image.build.AssemblyConfiguration;
import org.eclipse.jkube.kit.config.image.build.BuildConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.kameshs.utils.GitHubServiceUtil;
import dev.kameshs.utils.ImageResolverUtil;

public class ImageBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(
      ImageBuilder.class.getName());
  public static final String JO_CONTAINER_REPO = "dev.local";

  static final String BASE_IMAGE = "quay.io/jbangdev/jbang-action";

  public void build(String strImageUri) throws Exception {
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

      String imageName = jbangScriptSegment;
      String downloadURL = "https://" + uri.getHost() + path;

      String tempDestinationFile =
          "/tmp/" + path.replaceAll("/", "-").substring(1);


      String jbangExecScript = "/scripts/" + jbangScriptSegment;

      if (jbangScriptSegment.endsWith(".java")) {
        imageName =
            jbangScriptSegment.substring(0,
                jbangScriptSegment.lastIndexOf("."));
      } else {
        downloadURL += ".java";
        tempDestinationFile += ".java";
        jbangExecScript += ".java";
      }

      Optional<File> downloadedSource =
          downloadScriptFile(segments, downloadURL, tempDestinationFile);

      if (downloadedSource.isPresent()) {

        LOGGER.debug("Script Name: {} ", jbangExecScript);

        //Copy the script file to destination folder
        AssemblyFile scriptFile =
            AssemblyFile.builder()
                .source(downloadedSource.get())
                .outputDirectory(new File("/scripts"))
                .build();
        List<AssemblyFile> scriptFiles = Arrays.asList(scriptFile);

        AssemblyConfiguration scriptAssembly = AssemblyConfiguration
            .builder()
            .inline(Assembly.builder().files(scriptFiles).build())
            .build();

        //Image build Configuration
        BuildConfiguration bc = BuildConfiguration
            .builder()
            .from(fromImage)
            .cmd(Arguments.builder()
                .shell(jbangExecScript)
                .build())
            .assembly(
                scriptAssembly)
            .build();

        ImageConfiguration
            .builder()
            .name(imageName)
            .build(bc)
            .build();
      }
    }
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
        file = new File(destinationFile);
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
}
