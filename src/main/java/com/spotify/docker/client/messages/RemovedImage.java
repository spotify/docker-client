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

package com.spotify.docker.client.messages;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class RemovedImage {

  public abstract Type type();

  @Nullable
  public abstract String imageId();

  @JsonCreator
  public static RemovedImage create(
      @JsonProperty("Untagged") final String untagged,
      @JsonProperty("Deleted") final String deleted) {
    if (untagged != null) {
      return new AutoValue_RemovedImage(Type.UNTAGGED, untagged);
    } else if (deleted != null) {
      return new AutoValue_RemovedImage(Type.DELETED, deleted);
    } else {
      return new AutoValue_RemovedImage(Type.UNKNOWN, null);
    }
  }

  public static RemovedImage create(final Type type, final String imageId) {
    return new AutoValue_RemovedImage(type, imageId);
  }

  public enum Type {
    UNTAGGED,
    DELETED,
    UNKNOWN
  }
}

