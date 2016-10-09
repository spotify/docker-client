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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Driver {

  @JsonProperty("Name")
  private String name;

  @JsonProperty("Options")
  private Map<String, String> options;

  public String name() {
    return name;
  }

  public Map<String, String> options() {
    return options;
  }

  public static class Builder {

    private Driver driver = new Driver();

    public Builder withName(String name) {
      driver.name = name;
      return this;
    }

    public Builder withOption(String name, String value) {
      if (driver.options == null) {
        driver.options = new HashMap<String, String>();
      }
      driver.options.put(name, value);
      return this;
    }

    public Driver build() {
      return driver;
    }
  }

  public static Driver.Builder builder() {
    return new Driver.Builder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Driver that = (Driver) o;

    return Objects.equals(this.name, that.name) && Objects.equals(this.options, that.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, options);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("options", options)
        .toString();
  }
}
