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
public abstract class PortConfig {

  public static final String PROTOCOL_TCP = "tcp";
  public static final String PROTOCOL_UDP = "udp";

  @NotNull
  @JsonProperty("Name")
  public abstract String name();

  @NotNull
  @JsonProperty("Protocol")
  public abstract String protocol();

  @NotNull
  @JsonProperty("TargetPort")
  public abstract Integer targetPort();

  @NotNull
  @JsonProperty("PublishedPort")
  public abstract Integer publishedPort();

  @AutoValue.Builder
  public abstract static class Builder {

    @JsonProperty("Name")
    public abstract Builder name(String name);

    @JsonProperty("Protocol")
    public abstract Builder protocol(String protocol);

    @JsonProperty("TargetPort")
    public abstract Builder targetPort(Integer targetPort);

    @JsonProperty("PublishedPort")
    public abstract Builder publishedPort(Integer publishedPort);

    public abstract PortConfig build();
  }

  @NotNull
  public static PortConfig.Builder builder() {
    return new AutoValue_PortConfig.Builder();
  }

  @JsonCreator
  static PortConfig create(
      @JsonProperty("Name") final String name,
      @JsonProperty("Protocol") final String protocol,
      @JsonProperty("TargetPort") final Integer targetPort,
      @JsonProperty("PublishedPort") final Integer publishedPort) {
    return builder()
        .name(name)
        .protocol(protocol)
        .targetPort(targetPort)
        .publishedPort(publishedPort)
        .build();
  }
}
