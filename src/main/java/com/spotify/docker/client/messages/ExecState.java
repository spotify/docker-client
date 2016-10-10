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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * An object that represents the JSON returned by the Docker API for low-level information about
 * exec commands.
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ExecState {

  @JsonProperty("ID")
  private String id;
  @JsonProperty("Running")
  private Boolean running;
  @JsonProperty("ExitCode")
  private Integer exitCode;
  @JsonProperty("ProcessConfig")
  private ProcessConfig processConfig;
  @JsonProperty("OpenStdin")
  private Boolean openStdin;
  @JsonProperty("OpenStderr")
  private Boolean openStderr;
  @JsonProperty("OpenStdout")
  private Boolean openStdout;
  @JsonProperty("Container")
  private ContainerInfo container;
  @JsonProperty("ContainerID")
  private String containerId;

  public String id() {
    return id;
  }

  public Boolean running() {
    return running;
  }

  public Integer exitCode() {
    return exitCode;
  }

  public ProcessConfig processConfig() {
    return processConfig;
  }

  public Boolean openStdin() {
    return openStdin;
  }

  public Boolean openStderr() {
    return openStderr;
  }

  public Boolean openStdout() {
    return openStdout;
  }

  public ContainerInfo container() {
    return container;
  }

  public String containerId() {
    return containerId;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final ExecState that = (ExecState) obj;

    return Objects.equals(this.id, that.id)
           && Objects.equals(this.running, that.running)
           && Objects.equals(this.exitCode, that.exitCode)
           && Objects.equals(this.processConfig, that.processConfig)
           && Objects.equals(this.openStdin, that.openStdin)
           && Objects.equals(this.openStderr, that.openStderr)
           && Objects.equals(this.openStdout, that.openStdout)
           && Objects.equals(this.container, that.container)
           && Objects.equals(this.containerId, that.containerId);

  }

  @Override
  public int hashCode() {
    return Objects.hash(id, running, exitCode, processConfig, openStdin, openStderr, openStdout,
                        container, containerId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("running", running)
        .add("exitCode", exitCode)
        .add("processConfig", processConfig)
        .add("openStdin", openStdin)
        .add("openStderr", openStderr)
        .add("openStdout", openStdout)
        .add("container", container)
        .add("containerId", containerId)
        .toString();
  }
}
