package dev.kameshs.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MavenArtifactService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MavenArtifactService.class);

  @Inject
  @Named("jo-repository-system")
  RepositorySystem repositorySystem;

  @Inject
  @Named("jo-repository-system-session")
  RepositorySystemSession session;

  List<RemoteRepository> remoteRepos = new ArrayList<>();

  @PostConstruct
  void init() {
    RemoteRepository jitPackRepo = new RemoteRepository.Builder("jitpack.io",
        "default", "https://jitpack.io").build();
    remoteRepos.add(jitPackRepo);
    repositorySystem.newResolutionRepositories(session, remoteRepos);
  }

  public Optional<File> resolveArtifact(String groupId, String artifactId,
      String packaging, String version) {
    try {
      Artifact artifact =
          new DefaultArtifact(groupId, artifactId, packaging, version);
      ArtifactRequest artifactRequest = new ArtifactRequest();
      artifactRequest.setArtifact(artifact);
      artifactRequest.setRepositories(remoteRepos);
      ArtifactResult artifactResult =
          repositorySystem.resolveArtifact(session, artifactRequest);
      artifact = artifactResult.getArtifact();
      return Optional.ofNullable(artifact.getFile());
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
              classifier, packaging,
              version);
      ArtifactRequest artifactRequest = new ArtifactRequest();
      artifactRequest.setArtifact(artifact);
      artifactRequest.setRepositories(remoteRepos);
      ArtifactResult artifactResult =
          repositorySystem.resolveArtifact(session, artifactRequest);
      artifact = artifactResult.getArtifact();
      return Optional.ofNullable(artifact.getFile());
    } catch (Exception e) {
      LOGGER.error("Error resolving artifact {}",
          String.join(":", groupId, artifactId,
              classifier, packaging,
              version),
          e);
    }
    return Optional.ofNullable(null);
  }
}
