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

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Device {

  @Nullable
  @JsonProperty("PathOnHost")
  public abstract String pathOnHost();

  @Nullable
  @JsonProperty("PathInContainer")
  public abstract String pathInContainer();

  @Nullable
  @JsonProperty("CgroupPermissions")
  public abstract String cgroupPermissions();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder pathOnHost(String pathOnHost);

    public abstract Builder pathInContainer(String pathInContainer);

    public abstract Builder cgroupPermissions(String cgroupPermissions);

    public abstract Device build();
  }

  public static Device.Builder builder() {
    return new AutoValue_Device.Builder();
  }

  @JsonCreator
  static Device create(
      @JsonProperty("PathOnHost") final String pathOnHost,
      @JsonProperty("PathInContainer") final String pathInContainer,
      @JsonProperty("CgroupPermissions") final String cgroupPermissions) {
    return builder()
        .pathOnHost(pathOnHost)
        .pathInContainer(pathInContainer)
        .cgroupPermissions(cgroupPermissions)
        .build();
  }
}
