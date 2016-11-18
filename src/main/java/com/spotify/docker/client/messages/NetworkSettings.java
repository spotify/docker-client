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

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class NetworkSettings {

  @Nullable
  @JsonProperty("IPAddress")
  public abstract String ipAddress();

  @Nullable
  @JsonProperty("IPPrefixLen")
  public abstract Integer ipPrefixLen();

  @Nullable
  @JsonProperty("Gateway")
  public abstract String gateway();

  @Nullable
  @JsonProperty("Bridge")
  public abstract String bridge();

  @Nullable
  @JsonProperty("PortMapping")
  public abstract ImmutableMap<String, Map<String, String>> portMapping();

  @Nullable
  @JsonProperty("Ports")
  public abstract ImmutableMap<String, List<PortBinding>> ports();

  @Nullable
  @JsonProperty("MacAddress")
  public abstract String macAddress();

  @Nullable
  @JsonProperty("Networks")
  public abstract ImmutableMap<String, AttachedNetwork> networks();

  @JsonCreator
  static NetworkSettings create(
      @JsonProperty("IPAddress") final String ipAddress,
      @JsonProperty("IPPrefixLen") final Integer ipPrefixLen,
      @JsonProperty("Gateway") final String gateway,
      @JsonProperty("Bridge") final String bridge,
      @JsonProperty("PortMapping") final Map<String, Map<String, String>> portMapping,
      @JsonProperty("Ports") final Map<String, List<PortBinding>> ports,
      @JsonProperty("MacAddress") final String macAddress,
      @JsonProperty("Networks") final Map<String, AttachedNetwork> networks) {

    final ImmutableMap.Builder<String, List<PortBinding>> portsCopy = ImmutableMap.builder();
    if (ports != null) {
      for (final Map.Entry<String, List<PortBinding>> entry : ports.entrySet()) {
        portsCopy.put(entry.getKey(),
            entry.getValue() == null ? Collections.<PortBinding>emptyList() : entry.getValue());
      }
    }

    return builder()
        .ipAddress(ipAddress)
        .ipPrefixLen(ipPrefixLen)
        .gateway(gateway)
        .bridge(bridge)
        .portMapping(portMapping)
        .ports(portsCopy.build())
        .macAddress(macAddress)
        .networks(networks)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_NetworkSettings.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder ipAddress(String ipAddress);

    public abstract Builder ipPrefixLen(Integer ipPrefixLen);

    public abstract Builder gateway(String gateway);

    public abstract Builder bridge(String bridge);

    public abstract Builder portMapping(Map<String, Map<String, String>> portMapping);

    public abstract Builder ports(Map<String, List<PortBinding>> ports);

    public abstract Builder macAddress(String macAddress);

    public abstract Builder networks(Map<String, AttachedNetwork> networks);

    public abstract NetworkSettings build();
  }
}
