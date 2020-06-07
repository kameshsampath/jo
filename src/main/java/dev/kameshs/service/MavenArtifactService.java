package dev.kameshs.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.quarkus.bootstrap.resolver.AppModelResolverException;
import io.quarkus.bootstrap.resolver.maven.BootstrapMavenContext;

@ApplicationScoped
public class MavenArtifactService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MavenArtifactService.class);

  @ConfigProperty(name = "dev.kameshs.jo-maven-repo",
      defaultValue = "target/local-repo")
  String joMavenRepo;

  RepositorySystem repositorySystem;

  RepositorySystemSession session;

  LocalRepository localRepository;

  List<RemoteRepository> remoteRepos = new ArrayList<>();

  @PostConstruct
  void init() {
    BootstrapMavenContext mavenContext;
    try {
      mavenContext = new BootstrapMavenContext();
      this.localRepository = new LocalRepository(joMavenRepo);
      this.repositorySystem = mavenContext.getRepositorySystem();
      this.session = newSession();
      RemoteRepository jitPackRepo = new RemoteRepository.Builder("jitpack.io",
          "default", "https://jitpack.io").build();
      remoteRepos.add(jitPackRepo);
      this.repositorySystem.newResolutionRepositories(session,
          remoteRepos);
    } catch (AppModelResolverException e) {
      LOGGER.error("Error intializing maven resolution", e);
    }
  }


  RepositorySystemSession newSession() {
    DefaultRepositorySystemSession session =
        MavenRepositorySystemUtils.newSession();
    session.setLocalRepositoryManager(
        repositorySystem.newLocalRepositoryManager(session, localRepository));
    return session;
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


  @Retry(maxRetries = 5, delay = 30000)
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
