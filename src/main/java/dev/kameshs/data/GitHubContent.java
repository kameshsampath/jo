package dev.kameshs.data;

import javax.json.bind.annotation.JsonbProperty;

public class GitHubContent {

  @JsonbProperty("content")
  public String content;

  @JsonbProperty("download_url")
  public String downloadUrl;

  @JsonbProperty("name")
  public String name;

  @JsonbProperty("path")
  public String path;

  @JsonbProperty("type")
  public String type;

  @Override
  public String toString() {
    return String.format(
        "GitHub File:[\n \t Name: %s \n \t Path: %s \n \t Dowload URL: %s \n ]",
        name,
        path,
        downloadUrl);
  }
}
