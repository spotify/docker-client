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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Network {

  @JsonProperty("Name")
  private String name;
  @JsonProperty("Id")
  private String id;
  @JsonProperty("Scope")
  private String scope;
  @JsonProperty("Driver")
  private String driver;
  @JsonProperty("IPAM")
  private Ipam ipam;
  @JsonProperty("Containers")
  Map<String, Container> containers;
  @JsonProperty("Options")
  private Map<String, String> options;

  public String name() {
    return name;
  }

  public String id() {
    return id;
  }

  public String scope() {
    return scope;
  }

  public String driver() {
    return driver;
  }

  public Map<String, String> options() {
    return options;
  }

  public Map<String, Container> containers() {
    return containers;
  }

  public Ipam ipam() {
    return ipam;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Network that = (Network) o;

    return Objects.equals(this.name, that.name) &&
           Objects.equals(this.id, that.id) &&
           Objects.equals(this.scope, that.scope) &&
           Objects.equals(this.driver, that.driver) &&
           Objects.equals(this.ipam, that.ipam) &&
           Objects.equals(this.containers, that.containers) &&
           Objects.equals(this.options, that.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, id, scope, driver, ipam, containers, options);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("id", id)
        .add("scope", scope)
        .add("driver", driver)
        .add("ipam", ipam)
        .add("containers", containers)
        .add("options", options)
        .toString();
  }

  @JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
  public static class Container {

    @JsonProperty("EndpointID")
    private String endpointId;
    @JsonProperty("MacAddress")
    private String macAddress;
    @JsonProperty("IPv4Address")
    private String ipv4address;
    @JsonProperty("IPv6Address")
    private String ipv6address;

    public String endpointId() {
      return endpointId;
    }

    public String macAddress() {
      return macAddress;
    }

    public String ipv4address() {
      return ipv4address;
    }

    public String ipv6address() {
      return ipv6address;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final Container that = (Container) o;

      return Objects.equals(this.endpointId, that.endpointId) &&
             Objects.equals(this.macAddress, that.macAddress) &&
             Objects.equals(this.ipv4address, that.ipv4address) &&
             Objects.equals(this.ipv6address, that.ipv6address);
    }

    @Override
    public int hashCode() {
      return Objects.hash(endpointId, macAddress, ipv4address, ipv6address);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("endpointId", endpointId)
          .add("macAddress", macAddress)
          .add("ipv4address", ipv4address)
          .add("ipv6address", ipv6address)
          .toString();
    }
  }
}
