package dev.kameshs.service;

import java.io.File;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MavenArtifactService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MavenArtifactService.class);

  @Named("jo-jitpack-maven-resolver-system")
  @Inject
  ConfigurableMavenResolverSystem mavenResolverSystem;

  public Optional<File> resolveArtifact(String groupId, String artifactId,
      String packaging, String version) {
    try {
      // G:A:P:V
      String mavenCoords =
          String.join(":", groupId, artifactId, packaging, version);
      return resolveArtifact(mavenCoords);
    } catch (Exception e) {
      LOGGER.error("Error resolving artifact {}",
          String.join(":", groupId, artifactId, packaging,
              version),
          e);
    }
    return Optional.ofNullable(null);
  }

  public Optional<File> resolveArtifact(String groupId, String artifactId,
      String packaging, String version, String classifier) {
    try {
      // G:A:P:C:V
      String mavenCoords =
          String.join(":", groupId, artifactId, packaging,
              classifier, version);
      return resolveArtifact(mavenCoords);
    } catch (Exception e) {
      LOGGER.error("Error resolving artifact {}",
          String.join(":", groupId, artifactId,
              classifier, packaging,
              version),
          e);
    }
    return Optional.ofNullable(null);
  }

  protected Optional<File> resolveArtifact(String mavenCoords)
      throws Exception {
    File artifactFile = Maven.resolver()
        .resolve(mavenCoords)
        .withoutTransitivity()
        .asSingleFile();
    return Optional.ofNullable(artifactFile);
  }
}
