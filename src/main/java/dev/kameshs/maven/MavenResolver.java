
package dev.kameshs.maven;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MavenResolver {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(MavenResolver.class);

  @ConfigProperty(name = "dev.kameshs.jo-maven-repo",
      defaultValue = "target/local-repo")
  String joMavenRepo;

  private LocalRepository localRepository;

  @PostConstruct
  void init() {
    localRepository = new LocalRepository(joMavenRepo);
  }

  @Named("jo-repository-system")
  @Produces
  RepositorySystem newRepositorySystem() {
    // Service Locators
    DefaultServiceLocator serviceLocator =
        MavenRepositorySystemUtils.newServiceLocator();
    serviceLocator.addService(RepositoryConnectorFactory.class,
        BasicRepositoryConnectorFactory.class);

    // Add Transporters
    serviceLocator.addService(TransporterFactory.class,
        HttpTransporterFactory.class);

    serviceLocator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {

      @Override
      public void serviceCreationFailed(Class<?> type, Class<?> impl,
          Throwable exception) {
        LOGGER.error("Service creation failed for {} implementation {}: {}",
            type, impl, exception.getMessage(), exception);
      }

    });

    return serviceLocator.getService(RepositorySystem.class);
  }

  @Named("jo-repository-system-session")
  @Produces
  RepositorySystemSession newSession(
      @Named("jo-repository-system") RepositorySystem repositorySystem) {
    DefaultRepositorySystemSession session =
        MavenRepositorySystemUtils.newSession();
    session.setLocalRepositoryManager(
        repositorySystem.newLocalRepositoryManager(session, localRepository));
    return session;
  }

}
