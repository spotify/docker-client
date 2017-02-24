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

package com.spotify.docker.client.messages.mount;

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
public abstract class VolumeOptions {

  @Nullable
  @JsonProperty("NoCopy")
  public abstract Boolean noCopy();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @Nullable
  @JsonProperty("DriverConfig")
  public abstract Driver driverConfig();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder noCopy(Boolean noCopy);

    public abstract Builder labels(Map<String, String> labels);

    abstract ImmutableMap.Builder<String, String> labelsBuilder();

    public Builder addLabel(final String label, final String value) {
      labelsBuilder().put(label, value);
      return this;
    }

    public abstract Builder driverConfig(Driver driverConfig);

    public abstract VolumeOptions build();
  }

  public static VolumeOptions.Builder builder() {
    return new AutoValue_VolumeOptions.Builder();
  }

  @JsonCreator
  static VolumeOptions create(
      @JsonProperty("NoCopy") final Boolean noCopy,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("DriverConfig") final Driver driverConfig) {
    return builder()
        .noCopy(noCopy)
        .labels(labels)
        .driverConfig(driverConfig)
        .build();
  }
}
