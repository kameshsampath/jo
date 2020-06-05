package dev.kameshs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import dev.kameshs.data.GitHubContent;
import dev.kameshs.service.GitHubContentService;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class GitHubContentServiceTest {

  @Inject
  @RestClient
  GitHubContentService ghContentService;

  @Test
  public void scriptContentTest() throws Exception {
    GitHubContent ghContent =
        ghContentService.githubFile("kameshsampath", "jo",
            "examples/http/server.java");
    assertNotNull(ghContent.downloadUrl);
    assertEquals(
        "https://raw.githubusercontent.com/kameshsampath/jo/master/examples/http/server.java",
        ghContent.downloadUrl);
    assertNotNull(ghContent.name);
    assertNotNull("server.java ", ghContent.name);
    assertNotNull(ghContent.path);
    assertNotNull("examples/http/server.java", ghContent.path);
    assertNotNull(ghContent.content);
    String content =
        readStreamAsString(this.getClass().getResourceAsStream("/server.java"));
    assertNotNull(content, ghContent.content);
  }

  private String readStreamAsString(InputStream in) {
    String content;
    try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
      content = scanner.useDelimiter("\\z").next();
    }
    return content;
  }
}
