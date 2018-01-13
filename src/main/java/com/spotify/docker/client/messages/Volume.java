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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Volume {

  @Nullable
  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Driver")
  public abstract String driver();

  @Nullable
  @JsonProperty("DriverOpts")
  public abstract ImmutableMap<String, String> driverOpts();

  @Nullable
  @JsonProperty("Options")
  public abstract ImmutableMap<String, String> options();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @Nullable
  @JsonProperty("Mountpoint")
  public abstract String mountpoint();

  @Nullable
  @JsonProperty("Scope")
  public abstract String scope();

  @Nullable
  @JsonProperty("Status")
  public abstract ImmutableMap<String, String> status();

  @JsonCreator
  static Volume create(
      @JsonProperty("Name") final String name,
      @JsonProperty("Driver") final String driver,
      @JsonProperty("DriverOpts") final Map<String, String> driverOpts,
      @JsonProperty("Options") final Map<String, String> options,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("Mountpoint") final String mountpoint,
      @JsonProperty("Scope") final String scope,
      @JsonProperty("Status") final Map<String, String> status) {
    return builder()
        .name(name)
        .driver(driver)
        .driverOpts(driverOpts)
        .options(options)
        .labels(labels)
        .mountpoint(mountpoint)
        .scope(scope)
        .status(status)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_Volume.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder name(String name);

    public abstract Builder driver(String driver);

    public abstract Builder driverOpts(Map<String, String> driverOpts);

    public abstract Builder options(Map<String, String> options);

    public abstract Builder labels(Map<String, String> labels);

    public abstract Builder mountpoint(String mountpoint);

    public abstract Builder scope(String scope);

    public abstract Builder status(Map<String, String> status);

    public abstract Volume build();
  }
}
