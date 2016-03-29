/*
 * Copyright (c) 2015 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.docker.client.messages;

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * An object that represents the JSON returned by the Docker API for low-level information about
 * exec commands.
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ExecState {

  @JsonProperty("ID") private String id;
  @JsonProperty("Running") private Boolean running;
  @JsonProperty("ExitCode") private Integer exitCode;
  @JsonProperty("ProcessConfig") private ProcessConfig processConfig;
  @JsonProperty("OpenStdin") private Boolean openStdin;
  @JsonProperty("OpenStderr") private Boolean openStderr;
  @JsonProperty("OpenStdout") private Boolean openStdout;
  @JsonProperty("Container") private ContainerInfo container;
  @JsonProperty("ContainerID") private String containerID;

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

  public String containerID() {
    return containerID;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ExecState that = (ExecState) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (running != null ? !running.equals(that.running) : that.running != null) {
      return false;
    }
    if (exitCode != null ? !exitCode.equals(that.exitCode) : that.exitCode != null) {
      return false;
    }
    if (processConfig != null ? !processConfig.equals(that.processConfig)
                              : that.processConfig != null) {
      return false;
    }
    if (openStdin != null ? !openStdin.equals(that.openStdin) : that.openStdin != null) {
      return false;
    }
    if (openStderr != null ? !openStderr.equals(that.openStderr) : that.openStderr != null) {
      return false;
    }
    if (openStdout != null ? !openStdout.equals(that.openStdout) : that.openStdout != null) {
      return false;
    }
    if (container != null ? !container.equals(that.container) : that.container != null) {
      return false;
    }
    return containerID != null ? containerID.equals(that.containerID) : that.containerID == null;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (running != null ? running.hashCode() : 0);
    result = 31 * result + (exitCode != null ? exitCode.hashCode() : 0);
    result = 31 * result + (processConfig != null ? processConfig.hashCode() : 0);
    result = 31 * result + (openStdin != null ? openStdin.hashCode() : 0);
    result = 31 * result + (openStderr != null ? openStderr.hashCode() : 0);
    result = 31 * result + (openStdout != null ? openStdout.hashCode() : 0);
    result = 31 * result + (container != null ? container.hashCode() : 0);
    result = 31 * result + (containerID != null ? containerID.hashCode() : 0);
    return result;
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
        .add("containerID", containerID)
        .toString();
  }
}
