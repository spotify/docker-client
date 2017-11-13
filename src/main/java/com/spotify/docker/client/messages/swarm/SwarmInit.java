/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 - 2017 Spotify AB
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
public abstract class SwarmInit {

  @JsonProperty("ListenAddr")
  public abstract String listenAddr();

  @JsonProperty("AdvertiseAddr")
  public abstract String advertiseAddr();

  @Nullable
  @JsonProperty("ForceNewCluster")
  public abstract Boolean forceNewCluster();

  @Nullable
  @JsonProperty("Spec")
  public abstract SwarmSpec swarmSpec();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder listenAddr(String listenAddr);

    public abstract Builder advertiseAddr(String advertiseAddr);

    public abstract Builder forceNewCluster(Boolean forceNewCluster);

    public abstract Builder swarmSpec(SwarmSpec swarmSpec);

    public abstract SwarmInit build();
  }

  public static SwarmInit.Builder builder() {
    return new AutoValue_SwarmInit.Builder();
  }

  @JsonCreator
  static SwarmInit create(
      @JsonProperty("ListenAddr") final String listenAddr,
      @JsonProperty("AdvertiseAddr") final String advertiseAddr,
      @JsonProperty("ForceNewCluster") final Boolean forceNewCluster,
      @JsonProperty("Spec") final SwarmSpec swarmSpec) {
    return builder()
        .listenAddr(listenAddr)
        .advertiseAddr(advertiseAddr)
        .forceNewCluster(forceNewCluster)
        .swarmSpec(swarmSpec)
        .build();
  }
}
