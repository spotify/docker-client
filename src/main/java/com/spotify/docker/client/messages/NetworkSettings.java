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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class NetworkSettings {

  @JsonProperty("IPAddress")
  private String ipAddress;
  @JsonProperty("IPPrefixLen")
  private Integer ipPrefixLen;
  @JsonProperty("Gateway")
  private String gateway;
  @JsonProperty("Bridge")
  private String bridge;
  @JsonProperty("PortMapping")
  private ImmutableMap<String, Map<String, String>> portMapping;
  @JsonProperty("Ports")
  private Map<String, List<PortBinding>> ports;
  @JsonProperty("MacAddress")
  private String macAddress;
  @JsonProperty("Networks")
  private ImmutableMap<String, AttachedNetwork> networks;

  private NetworkSettings(final Builder builder) {
    this.ipAddress = builder.ipAddress;
    this.ipPrefixLen = builder.ipPrefixLen;
    this.gateway = builder.gateway;
    this.bridge = builder.bridge;
    this.portMapping = builder.portMapping;
    this.ports = builder.ports;
    this.macAddress = builder.macAddress;
    this.networks = builder.networks;
  }

  @SuppressWarnings("unused")
  public NetworkSettings() {
  }

  public String ipAddress() {
    return ipAddress;
  }

  public Integer ipPrefixLen() {
    return ipPrefixLen;
  }

  public String gateway() {
    return gateway;
  }

  public String bridge() {
    return bridge;
  }

  public Map<String, Map<String, String>> portMapping() {
    return portMapping;
  }

  public Map<String, List<PortBinding>> ports() {
    return (ports == null) ? null : Collections.unmodifiableMap(ports);
  }

  public String macAddress() {
    return macAddress;
  }

  public Map<String, AttachedNetwork> networks() {
    return (networks == null) ? null : Collections.unmodifiableMap(networks);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final NetworkSettings that = (NetworkSettings) o;

    return Objects.equals(this.ipAddress, that.ipAddress) &&
           Objects.equals(this.ipPrefixLen, that.ipPrefixLen) &&
           Objects.equals(this.gateway, that.gateway) &&
           Objects.equals(this.bridge, that.bridge) &&
           Objects.equals(this.portMapping, that.portMapping) &&
           Objects.equals(this.ports, that.ports) &&
           Objects.equals(this.macAddress, that.macAddress) &&
           Objects.equals(this.networks, that.networks);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ipAddress, ipPrefixLen, gateway, bridge, portMapping, ports, macAddress,
                        networks);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("ipAddress", ipAddress)
        .add("ipPrefixLen", ipPrefixLen)
        .add("gateway", gateway)
        .add("bridge", bridge)
        .add("portMapping", portMapping)
        .add("ports", ports)
        .add("macAddress", macAddress)
        .add("networks", networks)
        .toString();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String ipAddress;
    private Integer ipPrefixLen;
    private String gateway;
    private String bridge;
    private ImmutableMap<String, Map<String, String>> portMapping;
    private Map<String, List<PortBinding>> ports;
    private String macAddress;
    private ImmutableMap<String, AttachedNetwork> networks;

    private Builder() {
    }

    private Builder(final NetworkSettings networkSettings) {
      this.ipAddress = networkSettings.ipAddress;
      this.ipPrefixLen = networkSettings.ipPrefixLen;
      this.gateway = networkSettings.gateway;
      this.bridge = networkSettings.bridge;
      this.portMapping = networkSettings.portMapping;
      this.ports = networkSettings.ports;
      this.macAddress = networkSettings.macAddress;
      this.networks = networkSettings.networks;
    }

    public Builder ipAddress(final String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    public Builder ipPrefixLen(final Integer ipPrefixLen) {
      this.ipPrefixLen = ipPrefixLen;
      return this;
    }

    public Builder gateway(final String gateway) {
      this.gateway = gateway;
      return this;
    }

    public Builder bridge(final String bridge) {
      this.bridge = bridge;
      return this;
    }

    public Builder portMapping(final Map<String, Map<String, String>> portMapping) {
      final ImmutableMap.Builder<String, Map<String, String>> builder = ImmutableMap.builder();
      for (final Map.Entry<String, Map<String, String>> entry : portMapping.entrySet()) {
        builder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
      }
      this.portMapping = builder.build();
      return this;
    }

    public Builder ports(final Map<String, List<PortBinding>> ports) {
      this.ports = (ports == null) ? null : Maps.newHashMap(ports);
      return this;
    }

    public Builder macAddress(final String macAddress) {
      this.macAddress = macAddress;
      return this;
    }

    public Builder networks(final Map<String, AttachedNetwork> networks) {
      if (networks != null) {
        this.networks = ImmutableMap.copyOf(networks);
      }
      return this;
    }

    public NetworkSettings build() {
      return new NetworkSettings(this);
    }
  }
}
