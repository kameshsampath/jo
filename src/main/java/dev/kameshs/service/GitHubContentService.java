package dev.kameshs.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.kameshs.data.GitHubContent;

@Path("/repos")
@RegisterRestClient
public interface GitHubContentService {

  static final Logger LOGGER = LoggerFactory.getLogger(
      GitHubContentService.class);

  @GET
  @Path("{owner}/{repo}/contents/{path}")
  @Produces("application/vnd.github.v3+json")
  public GitHubContent githubFile(@PathParam("owner") String owner,
      @PathParam("repo") String repo,
      @PathParam("path") String contentPath);
}
