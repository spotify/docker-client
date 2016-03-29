/*
 * Copyright (c) 2014 Spotify AB.
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

import java.util.Date;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ContainerState {

  @JsonProperty("Running") private Boolean running;
  @JsonProperty("Paused") private Boolean paused;
  @JsonProperty("Restarting") private Boolean restarting;
  @JsonProperty("Pid") private Integer pid;
  @JsonProperty("ExitCode") private Integer exitCode;
  @JsonProperty("StartedAt") private Date startedAt;
  @JsonProperty("FinishedAt") private Date finishedAt;
  @JsonProperty("Error") private String error;
  @JsonProperty("OOMKilled") private Boolean oomKilled;

  public Boolean running() {
    return running;
  }

  public Boolean paused() {
    return paused;
  }

  public Boolean restarting() {
    return restarting;
  }

  public Integer pid() {
    return pid;
  }

  public Integer exitCode() {
    return exitCode;
  }

  public Date startedAt() {
    return startedAt == null ? null : new Date(startedAt.getTime());
  }

  public Date finishedAt() {
    return finishedAt == null ? null : new Date(finishedAt.getTime());
  }

  public String error() {
    return error;
  }

  public Boolean oomKilled() {
    return oomKilled;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ContainerState that = (ContainerState) o;

    if (exitCode != null ? !exitCode.equals(that.exitCode) : that.exitCode != null) {
      return false;
    }
    if (finishedAt != null ? !finishedAt.equals(that.finishedAt) : that.finishedAt != null) {
      return false;
    }
    if (pid != null ? !pid.equals(that.pid) : that.pid != null) {
      return false;
    }
    if (running != null ? !running.equals(that.running) : that.running != null) {
      return false;
    }
    if (paused != null ? !paused.equals(that.paused) : that.paused != null) {
      return false;
    }
    if (restarting != null ? !restarting.equals(that.restarting) : that.restarting != null) {
      return false;
    }
    if (startedAt != null ? !startedAt.equals(that.startedAt) : that.startedAt != null) {
      return false;
    }
    if (error != null ? !error.equals(that.error) : that.error != null) {
      return false;
    }
    if (oomKilled != null ? !oomKilled.equals(that.oomKilled) : that.oomKilled != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(running, pid, paused, restarting, exitCode, startedAt, finishedAt,
                        error, oomKilled);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("running", running)
        .add("pid", pid)
        .add("paused", paused)
        .add("restarting", restarting)
        .add("exitCode", exitCode)
        .add("startedAt", startedAt)
        .add("finishedAt", finishedAt)
        .add("error", error)
        .add("oomKilled", oomKilled)
        .toString();
  }
}
