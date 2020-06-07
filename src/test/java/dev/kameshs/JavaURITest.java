package dev.kameshs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import dev.kameshs.data.JavaRepoInfo;
import dev.kameshs.utils.URLUtils;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class JavaURITest {

  @Inject
  URLUtils urlUtils;

  @Test
  public void testWithoutRef() {
    Optional<JavaRepoInfo> rOptional = urlUtils.repoInfoFromURI(
        "https://github.com/kameshsampath/jo-sb-helloworld");
    assertTrue(rOptional.isPresent());
    var repoInfo = rOptional.get();
    assertEquals("kameshsampath", repoInfo.owner);
    assertEquals("jo-sb-helloworld", repoInfo.name);
    assertEquals("master-SNAPSHOT", repoInfo.ref);
  }

  @Test
  public void testWithRef() {
    Optional<JavaRepoInfo> rOptional = urlUtils.repoInfoFromURI(
        "https://github.com/kameshsampath/jo-sb-helloworld:v1.0.0");
    assertTrue(rOptional.isPresent());
    var repoInfo = rOptional.get();
    assertEquals("kameshsampath", repoInfo.owner);
    assertEquals("jo-sb-helloworld", repoInfo.name);
    assertEquals("v1.0.0", repoInfo.ref);
  }
}
