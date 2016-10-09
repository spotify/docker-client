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

import java.util.Date;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class UpdateStatus {

  @JsonProperty("State")
  private String state;

  @JsonProperty("StartedAt")
  private Date startedAt;

  @JsonProperty("CompletedAt")
  private Date completedAt;

  @JsonProperty("Message")
  private String message;

  public String state() {
    return state;
  }

  public Date startedAt() {
    return startedAt == null ? null : new Date(startedAt.getTime());
  }

  public Date completedAt() {
    return completedAt == null ? null : new Date(completedAt.getTime());
  }

  public String message() {
    return message;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final UpdateStatus that = (UpdateStatus) o;

    return Objects.equals(this.state, that.state)
           && Objects.equals(this.startedAt, that.startedAt)
           && Objects.equals(this.completedAt, that.completedAt)
           && Objects.equals(this.message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, startedAt, completedAt, message);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("state", state).add("startedAt", startedAt)
        .add("completedAt", completedAt).add("message", message).toString();
  }
}
