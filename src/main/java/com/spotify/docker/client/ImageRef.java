/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.docker.client;

public class ImageRef {

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
  }

  public String getImage() {
    return image;
  }

  public String getTag() {
    return tag;
  }

  @Override
  public String toString() {
    return tag == null ? image : image + ':' + tag;
  }
}
