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

package com.spotify.docker.client.messages;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.util.Collections.singletonList;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Ipam {

  @JsonProperty("Driver")
  public abstract String driver();

  @Nullable
  @JsonProperty("Config")
  public abstract ImmutableList<IpamConfig> config();

  @Nullable
  @JsonProperty("Options")
  public abstract ImmutableMap<String, String> options();

  public static Builder builder() {
    return new AutoValue_Ipam.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder driver(String driver);

    public abstract Builder options(Map<String, String> options);

    public abstract Builder config(List<IpamConfig> config);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #config(List)}.
     */
    @Deprecated
    public Builder config(final String subnet, final String ipRange, final String gateway) {
      return Ipam.builder().config(singletonList(IpamConfig.create(subnet, ipRange, gateway)));
    }

    public abstract Ipam build();
  }

  public static Ipam create(final String driver, final List<IpamConfig> config) {
    return builder()
        .driver(driver)
        .config(config)
        .options(null)
        .build();
  }

  @JsonCreator
  public static Ipam create(
      @JsonProperty("Driver") final String driver,
      @JsonProperty("Config") final List<IpamConfig> config,
      @JsonProperty("Options") final Map<String, String> options) {
    return builder()
        .driver(driver)
        .config(config)
        .options(options)
        .build();
  }
}
