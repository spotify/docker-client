/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class NetworkSpec {

  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @JsonProperty("DriverConfiguration")
  public abstract Driver driverConfiguration();

  @Nullable
  @JsonProperty("IPv6Enabled")
  public abstract Boolean ipv6Enabled();

  @Nullable
  @JsonProperty("Internal")
  public abstract Boolean internal();

  @Nullable
  @JsonProperty("Attachable")
  public abstract Boolean attachable();

  @Nullable
  @JsonProperty("IPAMOptions")
  public abstract IpamOptions ipamOptions();

  @JsonCreator
  static NetworkSpec create(
      @JsonProperty("Name") final String name,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("DriverConfiguration") final Driver driver,
      @JsonProperty("IPv6Enabled") final Boolean ipv6Enabled,
      @JsonProperty("Internal") final Boolean internal,
      @JsonProperty("Attachable") final Boolean attachable,
      @JsonProperty("IPAMOptions") final IpamOptions ipamOptions) {
    final ImmutableMap<String, String> labelsT = labels == null
                                                 ? null : ImmutableMap.copyOf(labels);
    return new AutoValue_NetworkSpec(name, labelsT, driver, ipv6Enabled, internal, attachable,
        ipamOptions);
  }
}
