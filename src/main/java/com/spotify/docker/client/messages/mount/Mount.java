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

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Mount {

  @Nullable
  @JsonProperty("Type")
  public abstract String type();

  @Nullable
  @JsonProperty("Source")
  public abstract String source();

  @Nullable
  @JsonProperty("Target")
  public abstract String target();

  @Nullable
  @JsonProperty("ReadOnly")
  public abstract Boolean readOnly();

  @Nullable
  @JsonProperty("BindOptions")
  public abstract BindOptions bindOptions();

  @Nullable
  @JsonProperty("VolumeOptions")
  public abstract VolumeOptions volumeOptions();

  @Nullable
  @JsonProperty("TmpfsOptions")
  public abstract TmpfsOptions tmpfsOptions();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder type(String type);

    public abstract Builder source(String source);

    public abstract Builder target(String target);

    public abstract Builder readOnly(Boolean readOnly);

    public abstract Builder bindOptions(BindOptions bindOptions);

    public abstract Builder volumeOptions(VolumeOptions volumeOptions);

    public abstract Builder tmpfsOptions(TmpfsOptions tmpfsOptions);

    public abstract Mount build();
  }

  public static Mount.Builder builder() {
    return new AutoValue_Mount.Builder();
  }

  @JsonCreator
  static Mount create(
      @JsonProperty("Type") final String type,
      @JsonProperty("Source") final String source,
      @JsonProperty("Target") final String target,
      @JsonProperty("ReadOnly") final Boolean readOnly,
      @JsonProperty("BindOptions") final BindOptions bindOptions,
      @JsonProperty("VolumeOptions") final VolumeOptions volumeOptions,
      @JsonProperty("TmpfsOptions") final TmpfsOptions tmpfsOptions) {
    return builder()
        .type(type)
        .source(source)
        .target(target)
        .readOnly(readOnly)
        .bindOptions(bindOptions)
        .volumeOptions(volumeOptions)
        .tmpfsOptions(tmpfsOptions)
        .build();
  }
}
