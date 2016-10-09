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
package com.spotify.docker.client.messages.swarm;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ContainerStatus {

  @JsonProperty("ContainerID")
  private String containerId;

  @JsonProperty("PID")
  private Integer pid;

  @JsonProperty("ExitCode")
  private Integer exitCode;

  // Checkstyle complains if using containerId()
  public String containerID() {
    return containerId;
  }

  public Integer pid() {
    return pid;
  }

  public Integer exitCode() {
    return exitCode;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ContainerStatus that = (ContainerStatus) o;

    return Objects.equals(this.containerId, that.containerId)
           && Objects.equals(this.pid, that.pid)
           && Objects.equals(this.exitCode, that.exitCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(containerId, pid, exitCode);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("containerId", containerId).add("pid", pid)
        .add("exitCode", exitCode).toString();
  }
}
