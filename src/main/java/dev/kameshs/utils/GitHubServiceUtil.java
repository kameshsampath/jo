package dev.kameshs.utils;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.jkube.kit.common.util.Base64Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubServiceUtil {

  static final Logger LOGGER = LoggerFactory.getLogger(GitHubServiceUtil.class);

  static final GitHubClient gitHubClient = new GitHubClient();

  static String CONTENT_REQUEST_URI = "/repos/%s/%s/contents/%s";

  public static Optional<String> githubFileContent(String owner, String repo,
      String contentPath) {
    String fileContent = null;
    GitHubRequest gitHubRequest = new GitHubRequest();
    gitHubRequest.setType(RepositoryContents.class);
    gitHubRequest
        .setUri(String.format(CONTENT_REQUEST_URI, owner, repo, contentPath));
    try {
      Object body = gitHubClient.get(gitHubRequest).getBody();
      if (body != null) {
        RepositoryContents rc =
            (RepositoryContents) body;
        String encoded = rc.getContent();
        fileContent = new String(Base64Util.decode(encoded));
      }
    } catch (Exception e) {
      LOGGER.error("Error downloading content from GitHub", e);
    }
    return Optional.ofNullable(fileContent);
  }
}
