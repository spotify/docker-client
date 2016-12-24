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

import org.jetbrains.annotations.NotNull;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ContainerChange {

  @NotNull
  @JsonProperty("Path")
  public abstract String path();

  @NotNull
  @JsonProperty("Kind")
  public abstract Integer kind();

  @NotNull
  public static Builder builder() {
    return new AutoValue_ContainerChange.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    @JsonProperty("Path")
    public abstract Builder path(String path);

    @JsonProperty("Kind")
    public abstract Builder kind(Integer kind);

    public abstract ContainerChange build();
  }

  @JsonCreator
  static ContainerChange create(
      @JsonProperty("Path") final String path,
      @JsonProperty("Kind") final Integer kind) {
    return builder()
        .path(path)
        .kind(kind)
        .build();
  }
}
