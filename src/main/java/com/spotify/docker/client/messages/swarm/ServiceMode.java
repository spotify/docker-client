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

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ServiceMode {

  @Nullable
  @JsonProperty("Replicated")
  public abstract ReplicatedService replicated();

  @Nullable
  @JsonProperty("Global")
  public abstract GlobalService global();

  public static ServiceMode withReplicas(final long replicas) {
    return ServiceMode.builder()
        .replicated(ReplicatedService.builder().replicas(replicas).build())
        .build();
  }

  public static ServiceMode withGlobal() {
    return ServiceMode.builder().global(GlobalService.builder().build()).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder replicated(ReplicatedService replicated);

    public abstract Builder global(GlobalService global);

    public abstract ServiceMode build();
  }

  public static ServiceMode.Builder builder() {
    return new AutoValue_ServiceMode.Builder();
  }

  @JsonCreator
  static ServiceMode create(
      @JsonProperty("Replicated") final ReplicatedService replicated,
      @JsonProperty("Global") final GlobalService global) {
    return builder()
        .replicated(replicated)
        .global(global)
        .build();
  }
}
