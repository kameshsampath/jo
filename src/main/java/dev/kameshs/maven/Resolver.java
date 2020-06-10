package dev.kameshs.maven;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import io.quarkus.bootstrap.resolver.AppModelResolverException;
import io.quarkus.bootstrap.resolver.maven.MavenArtifactResolver;

@ApplicationScoped
public class Resolver {

  private final List<RemoteRepository> remoteRepos = new ArrayList<>();

  @ConfigProperty(name = "dev.kameshs.jo-local-repo")
  Optional<String> localRepo;

  @PostConstruct
  void init() {
    RemoteRepository jitPackRepo = new RemoteRepository.Builder(
        "jitpack.io",
        "default",
        "https://jitpack.io")
            .setReleasePolicy(new RepositoryPolicy(true,
                RepositoryPolicy.UPDATE_POLICY_NEVER,
                RepositoryPolicy.CHECKSUM_POLICY_FAIL))
            .setSnapshotPolicy(
                new RepositoryPolicy(true,
                    RepositoryPolicy.UPDATE_POLICY_ALWAYS,
                    RepositoryPolicy.CHECKSUM_POLICY_FAIL))
            .build();
    remoteRepos.add(jitPackRepo);
  }

  @Named("jo-maven-artifact-resolver")
  @Produces
  MavenArtifactResolver mavenArtifactResolver()
      throws AppModelResolverException {
    return MavenArtifactResolver
        .builder()
        .setRemoteRepositories(remoteRepos)
        .setLocalRepository(localRepo.orElse(String.join("/",
            System.getProperty("user.home"), ".m2", "repository")))
        .build();
  }
}
