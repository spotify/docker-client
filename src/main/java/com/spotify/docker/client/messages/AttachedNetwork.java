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
import com.google.common.base.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class AttachedNetwork {

  @JsonProperty("EndpointID") private String endpointId;
  @JsonProperty("Gateway") private String gateway;
  @JsonProperty("IPAddress") private String ipAddress;
  @JsonProperty("IPPrefixLen") private Integer ipPrefixLen;
  @JsonProperty("IPv6Gateway") private String ipv6Gateway;
  @JsonProperty("GlobalIPv6Address") private String globalIPv6Address;
  @JsonProperty("GlobalIPv6PrefixLen") private Integer globalIPv6PrefixLen;
  @JsonProperty("MacAddress") private String macAddress;

  public String endpointId() {
    return endpointId;
  }

  public String gateway() {
    return gateway;
  }

  public String ipAddress() {
    return ipAddress;
  }

  public Integer ipPrefixLen() {
    return ipPrefixLen;
  }

  public String ipv6Gateway() {
    return ipv6Gateway;
  }

  public String globalIPv6Address() {
    return globalIPv6Address;
  }

  public Integer globalIPv6PrefixLen() {
    return globalIPv6PrefixLen;
  }

  public String macAddress() {
    return macAddress;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AttachedNetwork that = (AttachedNetwork) o;

    return Objects.equal(this.endpointId, that.endpointId) &&
        Objects.equal(this.gateway, that.gateway) &&
        Objects.equal(this.ipAddress, that.ipAddress) &&
        Objects.equal(this.ipPrefixLen, that.ipPrefixLen) &&
        Objects.equal(this.ipv6Gateway, that.ipv6Gateway) &&
        Objects.equal(this.globalIPv6Address, that.globalIPv6Address) &&
        Objects.equal(this.globalIPv6PrefixLen, that.globalIPv6PrefixLen) &&
        Objects.equal(this.macAddress, that.macAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(endpointId, gateway, ipAddress, ipPrefixLen, ipv6Gateway,
        globalIPv6Address, globalIPv6PrefixLen, macAddress);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("endpointID", endpointId).add("gateway", gateway)
        .add("ipAddress", ipAddress).add("ipPrefixLen", ipPrefixLen).add("ipv6Gateway", ipv6Gateway)
        .add("globalIPv6Address", globalIPv6Address).add("globalIPv6PrefixLen", globalIPv6PrefixLen)
        .add("macAddress", macAddress).toString();
  }

}
