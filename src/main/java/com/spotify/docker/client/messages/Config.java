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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Config {

  @JsonProperty("Subnet") private String subnet;
  @JsonProperty("IPRange") private String ipRange;
  @JsonProperty("Gateway") private String gateway;

  public String subnet() {
    return subnet;
  }

  public String ipRange() {
    return ipRange;
  }

  public String gateway() {
    return gateway;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Config that = (Config) o;

    return Objects.equal(this.subnet, that.subnet) &&
        Objects.equal(this.ipRange, that.ipRange) &&
        Objects.equal(this.gateway, that.gateway);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(subnet, ipRange, gateway);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("subnet", subnet)
        .add("ipRange", ipRange)
        .add("gateway", gateway)
        .toString();
  }

}
