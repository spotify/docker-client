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
public class RestartPolicy {

  public static final String RESTART_POLICY_NONE = "none";
  public static final String RESTART_POLICY_ON_FAILURE = "on-failure";
  public static final String RESTART_POLICY_ANY = "any";

  @JsonProperty("Condition")
  private String condition;

  @JsonProperty("Delay")
  private Long delay;

  @JsonProperty("MaxAttempts")
  private Integer maxAttempts;

  @JsonProperty("Window")
  private Long window;

  public String condition() {
    return condition;
  }

  public Long delay() {
    return delay;
  }

  public Integer maxAttempts() {
    return maxAttempts;
  }

  public Long window() {
    return window;
  }

  public static class Builder {

    private RestartPolicy restart = new RestartPolicy();

    public Builder withCondition(String condition) {
      restart.condition = condition;
      return this;
    }

    public Builder withDelay(long delay) {
      restart.delay = delay;
      return this;
    }

    public Builder withMaxAttempts(int maxAttempts) {
      restart.maxAttempts = maxAttempts;
      return this;
    }

    public Builder withWindow(long window) {
      restart.window = window;
      return this;
    }

    public RestartPolicy build() {
      return restart;
    }
  }

  public static RestartPolicy.Builder builder() {
    return new RestartPolicy.Builder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final RestartPolicy that = (RestartPolicy) o;

    return Objects.equals(this.condition, that.condition)
           && Objects.equals(this.delay, that.delay)
           && Objects.equals(this.maxAttempts, that.maxAttempts)
           && Objects.equals(this.window, that.window);
  }

  @Override
  public int hashCode() {
    return Objects.hash(condition, delay, maxAttempts, window);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("condition", condition).add("delay", delay)
        .add("maxAttempts", maxAttempts).add("window", window).toString();
  }
}
