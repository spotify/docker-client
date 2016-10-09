/*
 * Copyright (c) 2015 Spotify AB.
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
package com.spotify.docker.client.messages.swarm;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class PortConfig {

  public static final String PROTOCOL_TCP = "tcp";
  public static final String PROTOCOL_UDP = "udp";

  @JsonProperty("Name")
  private String name;

  @JsonProperty("Protocol")
  private String protocol;

  @JsonProperty("TargetPort")
  private Integer targetPort;

  @JsonProperty("PublishedPort")
  private Integer publishedPort;

  public String name() {
    return name;
  }

  public String protocol() {
    return protocol;
  }

  public Integer targetPort() {
    return targetPort;
  }

  public Integer publishedPort() {
    return publishedPort;
  }

  public static class Builder {

    private PortConfig config = new PortConfig();

    public Builder withName(String name) {
      config.name = name;
      return this;
    }

    public Builder withProtocol(String protocol) {
      config.protocol = protocol;
      return this;
    }

    public Builder withTargetPort(int targetPort) {
      config.targetPort = targetPort;
      return this;
    }

    public Builder withPublishedPort(int publishedPort) {
      config.publishedPort = publishedPort;
      return this;
    }

    public PortConfig build() {
      return config;
    }
  }

  public static PortConfig.Builder builder() {
    return new PortConfig.Builder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final PortConfig that = (PortConfig) o;

    return Objects.equals(this.name, that.name) && Objects.equals(this.protocol, that.protocol)
           && Objects.equals(this.targetPort, that.targetPort)
           && Objects.equals(this.publishedPort, that.publishedPort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, protocol, targetPort, publishedPort);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("protocol", protocol)
        .add("targetPort", targetPort).add("publishedPort", publishedPort).toString();
  }
}
