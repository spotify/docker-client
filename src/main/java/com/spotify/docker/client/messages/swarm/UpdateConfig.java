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
public class UpdateConfig {

  @JsonProperty("Parallelism")
  private Long parallelism;

  @JsonProperty("Delay")
  private Long delay;

  @JsonProperty("FailureAction")
  private String failureAction;

  public Long parallelism() {
    return parallelism;
  }

  public Long delay() {
    return delay;
  }

  public String failureAction() {
    return failureAction;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final UpdateConfig that = (UpdateConfig) o;

    return Objects.equals(this.parallelism, that.parallelism)
           && Objects.equals(this.delay, that.delay)
           && Objects.equals(this.failureAction, that.failureAction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parallelism, delay, failureAction);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("parallelism", parallelism).add("delay", delay)
        .add("failureAction", failureAction).toString();
  }
}
