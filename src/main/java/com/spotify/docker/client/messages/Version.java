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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Version {

  @NotNull
  @JsonProperty("ApiVersion")
  public abstract String apiVersion();

  @NotNull
  @JsonProperty("Arch")
  public abstract String arch();

  @Nullable
  @JsonProperty("BuildTime")
  public abstract String buildTime();

  @NotNull
  @JsonProperty("GitCommit")
  public abstract String gitCommit();

  @NotNull
  @JsonProperty("GoVersion")
  public abstract String goVersion();

  @NotNull
  @JsonProperty("KernelVersion")
  public abstract String kernelVersion();

  @NotNull
  @JsonProperty("Os")
  public abstract String os();

  @NotNull
  @JsonProperty("Version")
  public abstract String version();

  @JsonCreator
  static Version create(
      @JsonProperty("ApiVersion") final String apiVersion,
      @JsonProperty("Arch") final String arch,
      @JsonProperty("BuildTime") final String buildTime,
      @JsonProperty("GitCommit") final String gitCommit,
      @JsonProperty("GoVersion") final String goVersion,
      @JsonProperty("KernelVersion") final String kernelVersion,
      @JsonProperty("Os") final String os,
      @JsonProperty("Version") final String version) {
    return new AutoValue_Version(apiVersion, arch, buildTime, gitCommit, goVersion, kernelVersion,
        os, version);
  }
}
