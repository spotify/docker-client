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
public class TaskStatus {

  public static final String TASK_STATE_NEW = "new";
  public static final String TASK_STATE_ALLOCATED = "allocated";
  public static final String TASK_STATE_PENDING = "pending";
  public static final String TASK_STATE_ASSIGNED = "assigned";
  public static final String TASK_STATE_ACCEPTED = "accepted";
  public static final String TASK_STATE_PREPARING = "preparing";
  public static final String TASK_STATE_READY = "ready";
  public static final String TASK_STATE_STARTING = "starting";
  public static final String TASK_STATE_RUNNING = "running";
  public static final String TASK_STATE_COMPLETE = "complete";
  public static final String TASK_STATE_SHUTDOWN = "shutdown";
  public static final String TASK_STATE_FAILED = "failed";
  public static final String TASK_STATE_REJECTED = "rejected";

  @JsonProperty("Timestamp")
  private String timestamp;

  @JsonProperty("State")
  private String state;

  @JsonProperty("Message")
  private String message;

  @JsonProperty("Err")
  private String err;

  @JsonProperty("ContainerStatus")
  private ContainerStatus containerStatus;

  public String timestamp() {
    return timestamp;
  }

  public String state() {
    return state;
  }

  public String message() {
    return message;
  }

  public String err() {
    return err;
  }

  public ContainerStatus containerStatus() {
    return containerStatus;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final TaskStatus that = (TaskStatus) o;

    return Objects.equals(this.timestamp, that.timestamp)
           && Objects.equals(this.state, that.state)
           && Objects.equals(this.message, that.message) && Objects.equals(this.err, that.err)
           && Objects.equals(this.containerStatus, that.containerStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, state, message, err, containerStatus);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("timestamp", timestamp).add("state", state)
        .add("message", message).add("err", err).add("containerStatus", containerStatus)
        .toString();
  }
}
