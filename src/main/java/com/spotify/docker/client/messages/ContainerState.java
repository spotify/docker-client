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

  @JsonProperty("Status")
  private String status;
  @JsonProperty("Running")
  private Boolean running;
  @JsonProperty("Paused")
  private Boolean paused;
  @JsonProperty("Restarting")
  private Boolean restarting;
  @JsonProperty("Pid")
  private Integer pid;
  @JsonProperty("ExitCode")
  private Integer exitCode;
  @JsonProperty("StartedAt")
  private Date startedAt;
  @JsonProperty("FinishedAt")
  private Date finishedAt;
  @JsonProperty("Error")
  private String error;
  @JsonProperty("OOMKilled")
  private Boolean oomKilled;

  public String status() {
    return status;
  }

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

    return Objects.equals(this.status, that.status) &&
           Objects.equals(this.running, that.running) &&
           Objects.equals(this.paused, that.paused) &&
           Objects.equals(this.restarting, that.restarting) &&
           Objects.equals(this.pid, that.pid) &&
           Objects.equals(this.exitCode, that.exitCode) &&
           Objects.equals(this.startedAt, that.startedAt) &&
           Objects.equals(this.finishedAt, that.finishedAt) &&
           Objects.equals(this.error, that.error) &&
           Objects.equals(this.oomKilled, that.oomKilled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, running, pid, paused, restarting, exitCode, startedAt, finishedAt,
                        error, oomKilled);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("status", status)
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
