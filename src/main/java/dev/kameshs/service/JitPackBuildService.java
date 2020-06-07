package dev.kameshs.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.checkerframework.common.reflection.qual.GetConstructor;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import dev.kameshs.data.JitPackBuild;

@Path("/builds")
@RegisterRestClient
public interface JitPackBuildService {

  @GET
  @Path("{groupId}/{artifactId}/{tag}")
  @Produces(MediaType.APPLICATION_JSON)
  @Retry(maxRetries = 5, delay = 30000)
  JitPackBuild buildByTag(@PathParam("groupId") String groupId,
      @PathParam("artifactId") String artifactId,
      @PathParam("tag") String tag);

  @GetConstructor
  @Path("{groupId}/{artifactId}/latest")
  @Produces(MediaType.APPLICATION_JSON)
  @Retry(maxRetries = 5, delay = 30000)
  JitPackBuild buildLatest(@PathParam("groupId") String groupId,
      @PathParam("artifactId") String artifactId);
}
