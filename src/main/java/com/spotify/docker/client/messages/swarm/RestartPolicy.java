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

package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class RestartPolicy {

  public static final String RESTART_POLICY_NONE = "none";
  public static final String RESTART_POLICY_ON_FAILURE = "on-failure";
  public static final String RESTART_POLICY_ANY = "any";

  @NotNull
  @JsonProperty("Condition")
  public abstract String condition();

  @NotNull
  @JsonProperty("Delay")
  public abstract Long delay();

  @NotNull
  @JsonProperty("MaxAttempts")
  public abstract Integer maxAttempts();

  @Nullable
  @JsonProperty("Window")
  public abstract Long window();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder condition(String condition);

    public abstract Builder delay(Long delay);

    public abstract Builder maxAttempts(Integer maxAttempts);

    public abstract Builder window(Long window);

    public abstract RestartPolicy build();
  }

  @NotNull
  public static RestartPolicy.Builder builder() {
    return new AutoValue_RestartPolicy.Builder();
  }

  @JsonCreator
  static RestartPolicy create(
      @JsonProperty("Condition") final String condition,
      @JsonProperty("Delay") final Long delay,
      @JsonProperty("MaxAttempts") final Integer maxAttempts,
      @JsonProperty("Window") final Long window) {
    return builder()
        .condition(condition)
        .delay(delay)
        .maxAttempts(maxAttempts)
        .window(window)
        .build();
  }
}
