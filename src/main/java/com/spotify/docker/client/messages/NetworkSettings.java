/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class NetworkSettings {

  @JsonProperty("IPAddress") private String ipAddress;
  @JsonProperty("IPPrefixLen") private Integer ipPrefixLen;
  @JsonProperty("Gateway") private String gateway;
  @JsonProperty("Bridge") private String bridge;
  @JsonProperty("PortMapping") private ImmutableMap<String, Map<String, String>> portMapping;
  @JsonProperty("Ports") private ImmutableMap<String, List<PortBinding>> ports;

  private NetworkSettings() {
  }

  private NetworkSettings(final Builder builder) {
    this.ipAddress = builder.ipAddress;
    this.ipPrefixLen = builder.ipPrefixLen;
    this.gateway = builder.gateway;
    this.bridge = builder.bridge;
    this.portMapping = builder.portMapping;
    this.ports = builder.ports;
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
    return ports;
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

    if (bridge != null ? !bridge.equals(that.bridge) : that.bridge != null) {
      return false;
    }
    if (gateway != null ? !gateway.equals(that.gateway) : that.gateway != null) {
      return false;
    }
    if (ipAddress != null ? !ipAddress.equals(that.ipAddress) : that.ipAddress != null) {
      return false;
    }
    if (ipPrefixLen != null ? !ipPrefixLen.equals(that.ipPrefixLen) : that.ipPrefixLen != null) {
      return false;
    }
    if (portMapping != null ? !portMapping.equals(that.portMapping) : that.portMapping != null) {
      return false;
    }
    if (ports != null ? !ports.equals(that.ports) : that.ports != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = ipAddress != null ? ipAddress.hashCode() : 0;
    result = 31 * result + (ipPrefixLen != null ? ipPrefixLen.hashCode() : 0);
    result = 31 * result + (gateway != null ? gateway.hashCode() : 0);
    result = 31 * result + (bridge != null ? bridge.hashCode() : 0);
    result = 31 * result + (portMapping != null ? portMapping.hashCode() : 0);
    result = 31 * result + (ports != null ? ports.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("ipAddress", ipAddress)
        .add("ipPrefixLen", ipPrefixLen)
        .add("gateway", gateway)
        .add("bridge", bridge)
        .add("portMapping", portMapping)
        .add("ports", ports)
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
    private ImmutableMap<String, List<PortBinding>> ports;

    private Builder() {
    }

    private Builder(final NetworkSettings networkSettings) {
      this.ipAddress = networkSettings.ipAddress;
      this.ipPrefixLen = networkSettings.ipPrefixLen;
      this.gateway = networkSettings.gateway;
      this.bridge = networkSettings.bridge;
      this.portMapping = networkSettings.portMapping;
      this.ports = networkSettings.ports;
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
      for (Map.Entry<String, Map<String, String>> entry : portMapping.entrySet()) {
        builder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
      }
      this.portMapping = builder.build();
      return this;
    }

    public Builder ports(final Map<String, List<PortBinding>> ports) {
      final ImmutableMap.Builder<String, List<PortBinding>> builder = ImmutableMap.builder();
      for (Map.Entry<String, List<PortBinding>> entry : ports.entrySet()) {
        builder.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
      }
      this.ports = builder.build();
      return this;
    }

    public NetworkSettings build() {
      return new NetworkSettings(this);
    }
  }
}
