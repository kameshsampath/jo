package dev.kameshs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.util.Optional;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import dev.kameshs.data.JavaRepoInfo;
import dev.kameshs.data.JitPackBuild;
import dev.kameshs.service.JitPackBuildService;
import dev.kameshs.service.MavenArtifactService;
import dev.kameshs.utils.URLUtils;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MavenArtifactServiceTest {

  @Inject
  MavenArtifactService service;

  final String sbRepoURI = "https://github.com/kameshsampath/jo-sb-helloworld";
  final String quarkusRepoURI =
      "https://github.com/kameshsampath/hello-quarkus";

  @Inject
  @RestClient
  JitPackBuildService jitPackBuildService;

  @Inject
  URLUtils urlUtils;


  @Test
  public void testResolve() {
    Optional<JavaRepoInfo> rOptional = urlUtils.repoInfoFromURI(quarkusRepoURI);
    assertTrue(rOptional.isPresent());
    var repo = rOptional.get();
    var groupId = "com.github." + repo.owner;
    JitPackBuild build =
        jitPackBuildService.buildByTag(groupId, repo.name, repo.ref);
    assertNotNull(build);
    assertEquals("ok", build.status);
    Optional<File> oArtifact =
        service.resolveArtifact(groupId, repo.name, "jar", repo.ref);
    assertTrue(oArtifact.isPresent());
    assertTrue(oArtifact.get().exists());
  }

  @Test
  public void testResolveWithClassifer() {
    Optional<JavaRepoInfo> rOptional = urlUtils.repoInfoFromURI(quarkusRepoURI);
    assertTrue(rOptional.isPresent());
    var repo = rOptional.get();
    var groupId = "com.github." + repo.owner;
    JitPackBuild build =
        jitPackBuildService.buildByTag(groupId, repo.name, repo.ref);
    assertNotNull(build);
    assertEquals("ok", build.status);
    Optional<File> oArtifact =
        service.resolveArtifact(groupId, repo.name, "jar", repo.ref, "runner");
    assertTrue(oArtifact.isPresent());
    assertTrue(oArtifact.get().exists());
  }
}
