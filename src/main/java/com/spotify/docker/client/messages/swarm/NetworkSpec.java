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

import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class NetworkSpec {

  @JsonProperty("Name")
  private String name;

  @JsonProperty("Labels")
  private Map<String, String> labels;

  @JsonProperty("DriverConfiguration")
  private Driver driverConfiguration;

  @JsonProperty("IPv6Enabled")
  private Boolean ipv6Enabled;

  @JsonProperty("Internal")
  private Boolean internal;

  @JsonProperty("Attachable")
  private Boolean attachable;

  @JsonProperty("IPAMOptions")
  private IpamOptions ipamOptions;

  public String name() {
    return name;
  }

  public Map<String, String> labels() {
    return labels;
  }

  public Driver driverConfiguration() {
    return driverConfiguration;
  }

  public Boolean ipv6Enabled() {
    return ipv6Enabled;
  }

  public Boolean internal() {
    return internal;
  }

  public Boolean attachable() {
    return attachable;
  }

  public IpamOptions ipamOptions() {
    return ipamOptions;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final NetworkSpec that = (NetworkSpec) o;

    return Objects.equals(this.name, that.name) && Objects.equals(this.labels, that.labels)
           && Objects.equals(this.driverConfiguration, that.driverConfiguration)
           && Objects.equals(this.ipv6Enabled, that.ipv6Enabled)
           && Objects.equals(this.internal, that.internal)
           && Objects.equals(this.attachable, that.attachable)
           && Objects.equals(this.ipamOptions, that.ipamOptions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, labels, driverConfiguration, ipv6Enabled, internal, attachable,
                        ipamOptions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("labels", labels)
        .add("driverConfiguration", driverConfiguration).add("ipv6Enabled", ipv6Enabled)
        .add("internal", internal).add("attachable", attachable)
        .add("ipamOptions", ipamOptions).toString();
  }
}
