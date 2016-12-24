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

package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import org.jetbrains.annotations.NotNull;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ReplicatedService {

  @NotNull
  @JsonProperty("Replicas")
  public abstract Long replicas();

  @AutoValue.Builder
  public abstract static class Builder {

    @JsonProperty("Replicas")
    public abstract Builder replicas(Long replicas);

    public abstract ReplicatedService build();
  }

  @NotNull
  public static ReplicatedService.Builder builder() {
    return new AutoValue_ReplicatedService.Builder();
  }

  @JsonCreator
  static ReplicatedService create(@JsonProperty("Replicas") final Long replicas) {
    return builder()
        .replicas(replicas)
        .build();
  }
}
