/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.docker.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;

public class ImageRef {

  private static final String DEFAULT_REGISTRY = "docker.io";
  private static final String DEFAULT_REGISTRY_URL = "https://index.docker.io/v1/";

  private final String registryUrl;
  private final String registry;
  private final String image;
  private final String tag;

  public ImageRef(final String image) {
    final int lastAt = image.lastIndexOf('@');
    final int lastColon = image.lastIndexOf(':');
    if (lastAt >= 0) {
      this.image = image;
      this.tag = null;
    } else if (lastColon < 0) {
      this.image = image;
      this.tag = null;
    } else {
      final String tag = image.substring(lastColon + 1);
      if (tag.indexOf('/') < 0) {
        this.image = image.substring(0, lastColon);
        this.tag = tag;
      } else {
        this.image = image;
        this.tag = null;
      }
    }
    final String[] parts = image.split("/", 2);
    if (parts.length > 1 && isRegistry(parts[0])) {
      this.registry = parts[0];
      this.registryUrl = parseRegistryUrl(parts[0]);
    } else {
      this.registry = DEFAULT_REGISTRY;
      this.registryUrl = DEFAULT_REGISTRY_URL;
    }
  }

  private static boolean isRegistry(String part) {
    return part.contains(".");
  }

  public String getImage() {
    return image;
  }

  /** The image tag, or null if not set. */
  public String getTag() {
    return tag;
  }

  /** Hostname/ip address and port of the registry. */
  public String getRegistryName() {
    return registry;
  }

  /** Registry URL. */
  public String getRegistryUrl() {
    return registryUrl;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("registry", registry)
        .add("image", image)
        .add("tag", tag)
        .toString();
  }

  /** Return registry server address given first part of image. */
  @VisibleForTesting
  static String parseRegistryUrl(final String url) {
    if (url.equals("docker.io") || url.equals("index.docker.io")) {
      return DEFAULT_REGISTRY_URL;
    } else if (url.matches("(^|(\\w+)\\.)gcr\\.io$") || url.equals("gcr.kubernetes.io")) {
      // GCR uses https
      return "https://" + url;
    } else if (url.equals("quay.io")) {
      // Quay doesn't use any protocol in credentials file
      return "quay.io";
    } else if (!url.contains("http://") && !url.contains("https://")) {
      // Assume https
      return "https://" + url;
    } else {
      return url;
    }
  }
}
