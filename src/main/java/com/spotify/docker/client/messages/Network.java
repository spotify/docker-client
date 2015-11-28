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
import com.google.common.collect.ImmutableMap;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Network {

  @JsonProperty("Name") private String name;
  @JsonProperty("Id") private String id;
  @JsonProperty("Scope") private String scope;
  @JsonProperty("Driver") private String driver;
  @JsonProperty("IPAM") private Ipam ipam;
  @JsonProperty("Options") private ImmutableMap<String, String> options;

  private Network() {
  }

  private Network(final Builder builder) {
    this.name = builder.name;
    this.scope = builder.scope;
    this.driver = builder.driver;
  }

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
  public ImmutableMap<String, String> options() {
    return options;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Network that = (Network) o;

    return Objects.equal(this.name, that.name) &&
        Objects.equal(this.id, that.id) &&
        Objects.equal(this.id, that.id) &&
        Objects.equal(this.scope, that.scope) &&
        Objects.equal(this.driver, that.driver) &&
        Objects.equal(this.options, that.options);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, id, scope, driver, options);
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("id", id)
        .add("scope", scope)
        .add("driver", driver)
        .add("options", options)
        .toString();
  }

  public static class Builder {

    private String name;
    private String scope;
    private String driver;

    private Builder() {
    }

    private Builder(Network network) {
      this.name = network.name;
      this.scope = network.scope;
      this.driver = network.driver;
    }

    public Builder name(final String name) {
      if (name != null && !name.isEmpty()) {
        this.name = name;
      }
      return this;
    }

    public Builder scope(final String scope) {
      if (scope != null && !scope.isEmpty()) {
        this.scope = scope;
      }
      return this;
    }

    public Builder driver(final String driver) {
      if (driver != null && !driver.isEmpty()) {
        this.driver = driver;
      }
      return this;
    }

    public Network build() {
      return new Network(this);
    }

  }

}
