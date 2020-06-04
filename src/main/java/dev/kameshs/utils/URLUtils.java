package dev.kameshs.utils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import com.google.gson.Gson;

public class URLUtils {

  public static String swizzleURL(String url) {
    url = url.replaceFirst("^https://github.com/(.*)/blob/(.*)$",
        "https://raw.githubusercontent.com/$1/$2");

    url = url.replaceFirst("^https://gitlab.com/(.*)/-/blob/(.*)$",
        "https://gitlab.com/$1/-/raw/$2");

    url = url.replaceFirst("^https://bitbucket.org/(.*)/src/(.*)$",
        "https://bitbucket.org/$1/raw/$2");

    url = url.replaceFirst("^https://twitter.com/(.*)/status/(.*)$",
        "https://mobile.twitter.com/$1/status/$2");

    if (url.startsWith("https://gist.github.com/")) {
      url = extractFileFromGist(url);
    }

    return url;
  }

  private static String extractFileFromGist(String url) {
    // TODO: for gist we need to be smarter when it comes to downloading as it gives
    // an invalid flag when jbang compiles

    try {
      String gistapi = url.replaceFirst(
          "^https://gist.github.com/(([a-zA-Z0-9]*)/)?(?<gistid>[a-zA-Z0-9]*)$",
          "https://api.github.com/gists/${gistid}");
      // Util.info("looking at " + gistapi);
      String strdata = null;
      try {
        strdata = readStringFromURL(gistapi);
      } catch (IOException e) {
        // Util.info("error " + e);
        return url;
      }

      Gson parser = new Gson();

      Gist gist = parser.fromJson(strdata, Gist.class);

      // Util.info("found " + gist.files);
      final Optional<Map.Entry<String, Map<String, String>>> first =
          gist.files.entrySet()
              .stream()
              .filter(e -> e.getKey()
                  .endsWith(
                      ".java"))
              .findFirst();

      if (first.isPresent()) {
        // Util.info("looking at " + first);
        return (String) first.get().getValue().getOrDefault("raw_url", url);
      } else {
        // Util.info("nothing worked!");
        return url;
      }
    } catch (RuntimeException re) {
      return url;
    }
  }

  static class Gist {
    Map<String, Map<String, String>> files;
  }

  static String readStringFromURL(String requestURL) throws IOException {
    try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
        StandardCharsets.UTF_8.toString())) {
      scanner.useDelimiter("\\A");
      return scanner.hasNext() ? scanner.next() : "";
    }
  }


}
