/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2018 Spotify AB
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
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.DEFAULT;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = DEFAULT)
public abstract class Distribution {

  @JsonProperty("Descriptor")
  public abstract Descriptor descriptor();

  @Nullable
  @JsonProperty("Platforms")
  public abstract ImmutableList<Platform> platforms();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder descriptor(Descriptor descriptor);

    public abstract Builder platforms(ImmutableList<Platform> platforms);

    public abstract Distribution build();
  }

  public static Builder builder() {
    return new AutoValue_Distribution.Builder();
  }

  @JsonCreator
  static Distribution create(
          @JsonProperty("Descriptor") Descriptor descriptor,
          @JsonProperty("Platforms") ImmutableList<Platform> platforms) {
    return builder()
            .descriptor(descriptor)
            .platforms(platforms)
            .build();
  }
}
