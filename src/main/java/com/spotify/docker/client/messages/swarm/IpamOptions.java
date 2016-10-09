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
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class IpamOptions {

  @JsonProperty("Driver")
  private Driver driver;

  @JsonProperty("Configs")
  private ImmutableList<IpamConfig> configs;

  public Driver driver() {
    return driver;
  }

  public List<IpamConfig> configs() {
    return configs;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final IpamOptions that = (IpamOptions) o;

    return Objects.equals(this.driver, that.driver)
           && Objects.equals(this.configs, that.configs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(driver, configs);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("driver", driver).add("configs", configs)
        .toString();
  }
}
