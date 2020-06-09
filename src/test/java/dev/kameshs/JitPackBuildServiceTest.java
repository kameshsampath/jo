package dev.kameshs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import javax.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import dev.kameshs.data.JavaRepoInfo;
import dev.kameshs.data.JitPackBuild;
import dev.kameshs.service.JitPackBuildService;
import dev.kameshs.utils.URLUtils;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JitPackBuildServiceTest {

  final String repoURI = "quarkus://github.com/kameshsampath/hello-quarkus";

  @Inject
  @RestClient
  JitPackBuildService jitPackBuildService;

  @Inject
  URLUtils urlUtils;

  @Test
  public void testDefaultRef() {
    try {
      Optional<JavaRepoInfo> rOptional = urlUtils.repoInfoFromURI(repoURI);
      assertTrue(rOptional.isPresent());
      var repo = rOptional.get();
      var groupId = "com.github." + repo.owner;
      JitPackBuild build =
          jitPackBuildService.buildByTag(groupId, repo.name, repo.ref);
      assertNotNull(build);
      assertEquals("ok", build.status);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
