package dev.kameshs.utils;

import java.net.URI;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import dev.kameshs.actions.ImageBuilderBase;
import dev.kameshs.actions.JBangImageBuilder;
import dev.kameshs.actions.JavaImageBuilder;


@ApplicationScoped
public class ResolverUtil {

  @ConfigProperty(name = "dev.kameshs.jo-base-image")
  String joBaseImage;

  @ConfigProperty(name = "dev.kameshs.java-base-image")
  String javaBaseImage;

  @Inject
  JBangImageBuilder jbangImageBuilder;

  @Inject
  JavaImageBuilder javaImageBuilder;

  public Optional<String> resolveFromImage(URI uri) {
    String scheme = uri.getScheme();
    if ("jbang".equals(scheme)) {
      // TODO #4 move to offical Image PR to jbang
      return Optional.of(joBaseImage);
    } else if ("java".equals(scheme)) {
      // TODO #4 move to offical Image PR to jbang
      return Optional.of(javaBaseImage);
    }
    return Optional.ofNullable(null);
  }

  public Optional<ImageBuilderBase> resolveBuilderFromURI(String imageUri) {
    var uri = URI.create(imageUri);
    String scheme = uri.getScheme();
    if ("jbang".equals(scheme)) {
      // TODO #4 move to offical Image PR to jbang
      return Optional.of(jbangImageBuilder);
    } else if ("java".equals(scheme)) {
      return Optional.of(javaImageBuilder);
    }
    return Optional.ofNullable(null);
  }
}
