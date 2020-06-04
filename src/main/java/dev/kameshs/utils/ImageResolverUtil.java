package dev.kameshs.utils;

import java.net.URI;
import java.util.Optional;

public class ImageResolverUtil {

  public static Optional<String> resolveFromImage(URI uri) {
    String scheme = uri.getScheme();
    String image = null;
    if ("jbang".equals(scheme)) {
      //TODO #4 move to offical Image PR to jbang
      image = "quay.io/kameshsampath/jbang-action";
    }
    return Optional.ofNullable(image);
  }
}
