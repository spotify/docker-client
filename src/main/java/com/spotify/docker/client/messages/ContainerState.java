/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import com.google.common.base.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ContainerState {

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
  private String startedAt;
  @JsonProperty("FinishedAt")
  private String finishedAt;


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

  public String startedAt() {
    return startedAt;
  }

  public String finishedAt() {
    return finishedAt;
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
    if (startedAt != null ? !startedAt.equals(that.startedAt) : that.startedAt != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = running != null ? running.hashCode() : 0;
    result = 31 * result + (pid != null ? pid.hashCode() : 0);
    result = 31 * result + (exitCode != null ? exitCode.hashCode() : 0);
    result = 31 * result + (startedAt != null ? startedAt.hashCode() : 0);
    result = 31 * result + (finishedAt != null ? finishedAt.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("running", running)
        .add("pid", pid)
        .add("exitCode", exitCode)
        .add("startedAt", startedAt)
        .add("finishedAt", finishedAt)
        .toString();
  }
}
