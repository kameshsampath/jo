package dev.kameshs.service;

import java.io.File;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;

@ApplicationScoped
public class MavenArtifactService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MavenArtifactService.class);

  @Named("jo-maven-artifact-resolver")
  @Inject
  MavenArtifactResolver mavenArtifactResolver;

  public Optional<File> resolveArtifact(String groupId, String artifactId,
      String packaging, String version) {
    try {
      Artifact artifact =
          new DefaultArtifact(groupId, artifactId, packaging, version);
      return resolveArtifact(artifact);
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
      Artifact artifact =
          new DefaultArtifact(groupId, artifactId,
              classifier, packaging, version);
      return resolveArtifact(artifact);
    } catch (Exception e) {
      LOGGER.error("Error resolving artifact {}",
          String.join(":", groupId, artifactId,
              classifier, packaging,
              version),
          e);
    }
    return Optional.ofNullable(null);
  }

  protected Optional<File> resolveArtifact(Artifact artifact)
      throws Exception {
    File artifactFile = null;
    ArtifactResult artifactResult = mavenArtifactResolver
        .resolve(artifact);
    if (artifactResult.isResolved()) {
      artifactFile = artifactResult.getArtifact().getFile();
    } else if (artifactResult.isMissing()) {
      LOGGER.error("Error resolving artifact {}, reason missing", artifact);
    }
    return Optional.ofNullable(artifactFile);
  }
}
