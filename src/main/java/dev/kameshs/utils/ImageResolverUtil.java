package dev.kameshs.utils;

import java.net.URI;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;


@ApplicationScoped
public class ImageResolverUtil {

  @ConfigProperty(name = "dev.kameshs.jo-base-image")
  String joBaseImage;

  public Optional<String> resolveFromImage(URI uri) {
    String scheme = uri.getScheme();
    if ("jbang".equals(scheme)) {
      //TODO #4 move to offical Image PR to jbang
      return Optional.of(joBaseImage);
    }
    return Optional.ofNullable(null);
  }
}
