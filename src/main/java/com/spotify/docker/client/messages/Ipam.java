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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Ipam {

  @JsonProperty("Driver")
  private String driver;
  @JsonProperty("Config")
  private List<IpamConfig> config;

  private Ipam(final Builder builder) {
    this.driver = builder.driver;
    this.config = builder.configs;
  }

  @SuppressWarnings("unused")
  public Ipam() {
  }

  public String driver() {
    return driver;
  }

  public List<IpamConfig> config() {
    return config;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Ipam that = (Ipam) o;

    return Objects.equals(this.driver, that.driver) &&
           Objects.equals(this.config, that.config);
  }

  @Override
  public int hashCode() {
    return Objects.hash(driver, config);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("driver", driver)
        .add("config", config)
        .toString();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String driver;
    private List<IpamConfig> configs = new ArrayList<IpamConfig>();

    public Builder driver(final String driver) {
      this.driver = driver;
      return this;
    }

    public Builder config(final String subnet, final String ipRange, final String gateway) {
      final IpamConfig config = new IpamConfig();
      config.subnet(subnet);
      config.ipRange(ipRange);
      config.gateway(gateway);
      configs.add(config);
      return this;
    }

    public Ipam build() {
      return new Ipam(this);
    }
  }
}
