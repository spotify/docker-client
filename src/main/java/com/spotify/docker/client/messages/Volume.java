/*
 * Copyright (c) 2016 Spotify AB.
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
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Volume {

  @JsonProperty("Name")
  private String name;
  @JsonProperty("Driver")
  private String driver;
  @JsonProperty("DriverOpts")
  private ImmutableMap<String, String> driverOpts;
  @JsonProperty("Labels")
  private ImmutableMap<String, String> labels;
  @JsonProperty("Mountpoint")
  private String mountpoint;
  @JsonProperty("Scope")
  private String scope;
  @JsonProperty("Status")
  private ImmutableMap<String, String> status;

  private Volume() {
  }

  private Volume(final Builder builder) {
    this.name = builder.name;
    this.driver = builder.driver;
    this.driverOpts = builder.driverOpts;
    this.labels = builder.labels;
    this.mountpoint = builder.mountpoint;
    this.scope = builder.scope;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public String name() {
    return name;
  }

  public String driver() {
    return driver;
  }

  public Map<String, String> driverOpts() {
    return driverOpts;
  }

  public Map<String, String> labels() {
    return labels;
  }

  public String mountpoint() {
    return mountpoint;
  }

  public String scope() {
    return scope;
  }

  public Map<String, String> status() {
    return status;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Volume that = (Volume) o;

    return Objects.equals(this.name, that.name) &&
           Objects.equals(this.driver, that.driver) &&
           Objects.equals(this.driverOpts, that.driverOpts) &&
           Objects.equals(this.labels, that.labels) &&
           Objects.equals(this.mountpoint, that.mountpoint) &&
           Objects.equals(this.scope, that.scope) &&
           Objects.equals(this.status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, driver, driverOpts, labels, mountpoint, scope, status);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("driver", driver)
        .add("driverOpts", driverOpts)
        .add("labels", labels)
        .add("mountpoint", mountpoint)
        .add("scope", scope)
        .add("status", status)
        .toString();
  }

  public static class Builder {

    private String name;
    private String driver;
    private ImmutableMap<String, String> driverOpts;
    private ImmutableMap<String, String> labels;
    private String mountpoint;
    private String scope;

    private Builder() {
    }

    private Builder(final Volume volume) {
      this.name = volume.name;
      this.driver = volume.driver;
      this.driverOpts = volume.driverOpts;
      this.labels = volume.labels;
      this.mountpoint = volume.mountpoint;
      this.scope = volume.scope;
    }

    public Volume build() {
      return new Volume(this);
    }

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder driver(final String driver) {
      this.driver = driver;
      return this;
    }

    public Builder driverOpts(final Map<String, String> driverOpts) {
      this.driverOpts = ImmutableMap.copyOf(driverOpts);
      return this;
    }

    public Builder labels(final Map<String, String> labels) {
      this.labels = ImmutableMap.copyOf(labels);
      return this;
    }

    public Builder scope(final String scope) {
      this.scope = scope;
      return this;
    }
  }
}
