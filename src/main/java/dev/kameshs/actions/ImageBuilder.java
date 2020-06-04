package dev.kameshs.actions;

import org.apache.commons.io.FileUtils;
import org.eclipse.jkube.kit.build.service.docker.ImageConfiguration;
import org.eclipse.jkube.kit.common.Assembly;
import org.eclipse.jkube.kit.common.AssemblyFile;
import org.eclipse.jkube.kit.config.image.build.Arguments;
import org.eclipse.jkube.kit.config.image.build.AssemblyConfiguration;
import org.eclipse.jkube.kit.config.image.build.BuildConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ImageBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(
      ImageBuilder.class.getName());
  public static final String JO_CONTAINER_REPO = "dev.local";

  static final String BASE_IMAGE = "quay.io/jbangdev/jbang-action";

  public void build(String strImageUri) throws Exception {
    URI uri = new URI(strImageUri);
    final String path = uri.getPath();
    LOGGER.debug("Image Host {} and Path {}",
        new Object[] {uri.getHost(), path});

    String[] segments = path.split("/");
    String jbangScriptSegment = segments[segments.length - 1];

    String imageName = jbangScriptSegment;
    String downloadURL = "https://" + uri.getHost() + path;

    String tempDestinationFile =
        "/tmp/" + path.replaceAll("/", "-").substring(1);

    String jbangExecScript = "/jbang/" + jbangScriptSegment;

    if (jbangScriptSegment.endsWith(".java")) {
      imageName =
          jbangScriptSegment.substring(0, jbangScriptSegment.lastIndexOf("."));
    } else {
      downloadURL = downloadURL + ".java";
      tempDestinationFile = tempDestinationFile + ".java";
      jbangExecScript = jbangExecScript + ".java";
    }

    LOGGER.debug("Dowload URL: {} ", downloadURL);
    LOGGER.debug("Script Destination File: {} ", tempDestinationFile);

    Optional<File> downloadedSource =
        downloadScriptFile(downloadURL, tempDestinationFile);

    if (downloadedSource.isPresent()) {

      LOGGER.debug("Script Name: {} ", jbangExecScript);

      //Copy the script file to destination folder
      AssemblyFile scriptFile =
          AssemblyFile.builder().source(downloadedSource.get())
              .outputDirectory(new File("/jbang"))
              .build();
      List<AssemblyFile> scriptFiles = Arrays.asList(scriptFile);

      AssemblyConfiguration scriptAssembly = AssemblyConfiguration
          .builder()
          .inline(Assembly.builder().files(scriptFiles).build())
          .build();

      //Image build Configuration
      BuildConfiguration bc = BuildConfiguration
          .builder()
          .from(BASE_IMAGE)
          .cmd(Arguments.builder()
              .shell(jbangExecScript)
              .build())
          .assembly(
              scriptAssembly)
          .build();

      ImageConfiguration
          .builder()
          .name(imageName)
          .build(bc);
    }
  }

  private Optional<File> downloadScriptFile(String downloadURL,
      String destinationFile) {
    File file = null;
    try {
      file = new File(destinationFile);
      FileUtils.copyURLToFile(new URL(downloadURL), file);
    } catch (Exception e) {
      LOGGER.error("Error downloading file {}", downloadURL, e);
      file = null;
    }
    return Optional.ofNullable(file);
  }
}
