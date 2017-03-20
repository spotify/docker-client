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

  @Nullable
  @JsonProperty("EndpointID")
  public abstract String endpointId();

  @Nullable
  @JsonProperty("SandboxID")
  public abstract String sandboxId();

  @Nullable
  @JsonProperty("SandboxKey")
  public abstract String sandboxKey();

  @Nullable
  @JsonProperty("HairpinMode")
  public abstract Boolean hairpinMode();

  @Nullable
  @JsonProperty("LinkLocalIPv6Address")
  public abstract String linkLocalIPv6Address();

  @Nullable
  @JsonProperty("LinkLocalIPv6PrefixLen")
  public abstract Integer linkLocalIPv6PrefixLen();

  @Nullable
  @JsonProperty("GlobalIPv6Address")
  public abstract String globalIPv6Address();

  @Nullable
  @JsonProperty("GlobalIPv6PrefixLen")
  public abstract Integer globalIPv6PrefixLen();

  @Nullable
  @JsonProperty("IPv6Gateway")
  public abstract String ipv6Gateway();

  @JsonCreator
  static NetworkSettings create(
      @JsonProperty("IPAddress") final String ipAddress,
      @JsonProperty("IPPrefixLen") final Integer ipPrefixLen,
      @JsonProperty("Gateway") final String gateway,
      @JsonProperty("Bridge") final String bridge,
      @JsonProperty("PortMapping") final Map<String, Map<String, String>> portMapping,
      @JsonProperty("Ports") final Map<String, List<PortBinding>> ports,
      @JsonProperty("MacAddress") final String macAddress,
      @JsonProperty("Networks") final Map<String, AttachedNetwork> networks,
      @JsonProperty("EndpointID") final String endpointId,
      @JsonProperty("SandboxID") final String sandboxId,
      @JsonProperty("SandboxKey") final String sandboxKey,
      @JsonProperty("HairpinMode") final Boolean hairpinMode,
      @JsonProperty("LinkLocalIPv6Address") final String linkLocalIPv6Address,
      @JsonProperty("LinkLocalIPv6PrefixLen") final Integer linkLocalIPv6PrefixLen,
      @JsonProperty("GlobalIPv6Address") final String globalIPv6Address,
      @JsonProperty("GlobalIPv6PrefixLen") final Integer globalIPv6PrefixLen,
      @JsonProperty("IPv6Gateway") final String ipv6Gateway) {

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
        .endpointId(endpointId)
        .sandboxId(sandboxId)
        .sandboxKey(sandboxKey)
        .hairpinMode(hairpinMode)
        .linkLocalIPv6Address(linkLocalIPv6Address)
        .linkLocalIPv6PrefixLen(linkLocalIPv6PrefixLen)
        .globalIPv6Address(globalIPv6Address)
        .globalIPv6PrefixLen(globalIPv6PrefixLen)
        .ipv6Gateway(ipv6Gateway)
        .build();
  }

  private static Builder builder() {
    return new AutoValue_NetworkSettings.Builder();
  }

  @AutoValue.Builder
  abstract static class Builder {

    abstract Builder ipAddress(String ipAddress);

    abstract Builder ipPrefixLen(Integer ipPrefixLen);

    abstract Builder gateway(String gateway);

    abstract Builder bridge(String bridge);

    abstract Builder portMapping(Map<String, Map<String, String>> portMapping);

    abstract Builder ports(Map<String, List<PortBinding>> ports);

    abstract Builder macAddress(String macAddress);

    abstract Builder networks(Map<String, AttachedNetwork> networks);

    abstract Builder endpointId(final String endpointId);

    abstract Builder sandboxId(final String sandboxId);

    abstract Builder sandboxKey(final String sandboxKey);

    abstract Builder hairpinMode(final Boolean hairpinMode);

    abstract Builder linkLocalIPv6Address(final String linkLocalIPv6Address);

    abstract Builder linkLocalIPv6PrefixLen(final Integer linkLocalIPv6PrefixLen);

    abstract Builder globalIPv6Address(final String globalIPv6Address);

    abstract Builder globalIPv6PrefixLen(final Integer globalIPv6PrefixLen);

    abstract Builder ipv6Gateway(final String ipv6Gateway);

    abstract NetworkSettings build();
  }
}
