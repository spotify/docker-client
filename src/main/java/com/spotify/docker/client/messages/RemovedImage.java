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

package com.spotify.docker.client.messages;

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class RemovedImage {

  private final String imageId;
  private final Type type;

  public RemovedImage(@JsonProperty("Untagged") final String untagged,
                      @JsonProperty("Deleted") final String deleted) {
    if (untagged != null) {
      this.type = Type.UNTAGGED;
      this.imageId = untagged;
    } else if (deleted != null) {
      this.type = Type.DELETED;
      this.imageId = deleted;
    } else {
      this.type = Type.UNKNOWN;
      this.imageId = null;
    }
  }

  public RemovedImage(Type type, String imageId) {
    this.type = type;
    this.imageId = imageId;
  }

  public String imageId() {
    return imageId;
  }

  public Type type() {
    return type;
  }

  public static enum Type {
    UNTAGGED,
    DELETED,
    UNKNOWN
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final RemovedImage that = (RemovedImage) o;

    return Objects.equals(this.type, that.type) &&
           Objects.equals(this.imageId, that.imageId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(imageId, type);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("type", type)
        .add("imageId", imageId)
        .toString();
  }
}

