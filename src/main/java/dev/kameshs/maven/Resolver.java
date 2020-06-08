package dev.kameshs.maven;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Resolver {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(Resolver.class);

  @ConfigProperty(name = "dev.kameshs.jo-maven-repo",
      defaultValue = "target/local-repo")
  String joMavenRepo;

  @Named("jo-jitpack-maven-resolver-system")
  @Produces
  ConfigurableMavenResolverSystem mavenResolverSystem() {
    return Maven
        .configureResolver()
        .withRemoteRepo("jitpack.io",
            "https://jitpack.io", "default");
  }
}
