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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import org.jetbrains.annotations.NotNull;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Mount {

  @NotNull
  @JsonProperty("Type")
  public abstract String type();

  @NotNull
  @JsonProperty("Source")
  public abstract String source();

  @NotNull
  @JsonProperty("Target")
  public abstract String target();

  @NotNull
  @JsonProperty("ReadOnly")
  public abstract Boolean readOnly();

  @NotNull
  @JsonProperty("BindOptions")
  public abstract BindOptions bindOptions();

  @NotNull
  @JsonProperty("VolumeOptions")
  public abstract VolumeOptions volumeOptions();

  @AutoValue.Builder
  public abstract static class Builder {

    @JsonProperty("Type")
    public abstract Builder type(String type);

    @JsonProperty("Source")
    public abstract Builder source(String source);

    @JsonProperty("Target")
    public abstract Builder target(String target);

    public abstract Builder readOnly(Boolean readOnly);

    public abstract Builder bindOptions(BindOptions bindOptions);

    public abstract Builder volumeOptions(VolumeOptions volumeOptions);

    public abstract Mount build();
  }

  @NotNull
  public static Mount.Builder builder() {
    return new AutoValue_Mount.Builder();
  }
}
