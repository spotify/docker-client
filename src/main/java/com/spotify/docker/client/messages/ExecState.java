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

/**
 * An object that represents the JSON returned by the Docker API for low-level information about
 * exec commands.
 */
@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ExecState {

  @JsonProperty("ID")
  public abstract String id();

  @JsonProperty("Running")
  public abstract Boolean running();

  @Nullable
  @JsonProperty("ExitCode")
  public abstract Long exitCode();

  @JsonProperty("ProcessConfig")
  public abstract ProcessConfig processConfig();

  @JsonProperty("OpenStdin")
  public abstract Boolean openStdin();

  @JsonProperty("OpenStdout")
  public abstract Boolean openStdout();

  @JsonProperty("OpenStderr")
  public abstract Boolean openStderr();

  @Nullable
  @JsonProperty("Container")
  public abstract ContainerInfo container();

  @Nullable
  @JsonProperty("ContainerID")
  public abstract String containerId();

  @JsonCreator
  static ExecState create(
      @JsonProperty("ID") final String id,
      @JsonProperty("Running") final Boolean running,
      @JsonProperty("ExitCode") final Long exitCode,
      @JsonProperty("ProcessConfig") final ProcessConfig processConfig,
      @JsonProperty("OpenStdin") final Boolean openStdin,
      @JsonProperty("OpenStdout") final Boolean openStdout,
      @JsonProperty("OpenStderr") final Boolean openStderr,
      @JsonProperty("Container") final ContainerInfo containerInfo,
      @JsonProperty("ContainerID") final String containerId) {
    return new AutoValue_ExecState(id, running, exitCode, processConfig, openStdin, openStdout,
                                   openStderr, containerInfo, containerId);
  }
}
