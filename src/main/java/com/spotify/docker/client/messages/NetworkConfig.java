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

package com.spotify.docker.client.messages;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

public class NetworkConfig {
  @JsonProperty("Name") private String name;
  @JsonProperty("Driver") private String driver;
  @JsonProperty("Options") private ImmutableMap<String, String> options;
  @JsonProperty("CheckDuplicate") private Boolean checkDuplicate;

  public NetworkConfig() {
  }

  public NetworkConfig(final Builder builder) {
    this.name = builder.name;
    this.checkDuplicate = builder.checkDuplicate;
    this.driver = builder.driver;
    this.options = builder.options;
  }

  public String name() {
    return name;
  }

  public Boolean checkDuplicate() {
    return checkDuplicate;
  }

  public String driver() {
    return driver;
  }

  public ImmutableMap<String, String> options() {
    return options == null ? ImmutableMap.<String, String>of() : options;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final NetworkConfig that = (NetworkConfig) o;

    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (checkDuplicate != null ? !checkDuplicate.equals(that.checkDuplicate)
        : that.checkDuplicate != null) {
      return false;
    }
    if (driver != null ? !driver.equals(that.driver) : that.driver != null) {
      return false;
    }
    if (options != null ? !options.equals(that.options) : that.options != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((driver == null) ? 0 : driver.hashCode());
    result = prime * result + ((checkDuplicate == null) ? 0 : checkDuplicate.hashCode());
    result = prime * result + ((options == null) ? 0 : options.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("driver", driver)
        .add("scope", checkDuplicate).add("options", options).toString();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String name;
    private Boolean checkDuplicate;
    private String driver;
    private ImmutableMap<String, String> options;

    private Builder() {
    }

    private Builder(final NetworkConfig networkSettings) {
      this.name = networkSettings.name;
      this.checkDuplicate = networkSettings.checkDuplicate;
      this.driver = networkSettings.driver;
      this.options = networkSettings.options;
    }

    public Builder name(final String name) {
      this.name = name;
      return this;
    }

    public Builder checkDuplicate(final Boolean checkDuplicate) {
      this.checkDuplicate = checkDuplicate;
      return this;
    }

    public Builder driver(final String driver) {
      this.driver = driver;
      return this;
    }

    public Builder options(final Map<String, String> options) {
      this.options = (options == null) ? null : ImmutableMap.copyOf(options);
      return this;
    }

    public NetworkConfig build() {
      return new NetworkConfig(this);
    }
  }

}
