package dev.kameshs.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import dev.kameshs.data.GitHubContent;

@Path("/repos")
@RegisterRestClient
public interface GitHubContentService {

  @GET
  @Path("{owner}/{repo}/contents/{path}")
  @Produces("application/vnd.github.v3+json")
  GitHubContent githubFile(@PathParam("owner") String owner,
      @PathParam("repo") String repo,
      @PathParam("path") String contentPath);
}
