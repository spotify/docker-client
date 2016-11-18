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

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class UpdateConfig {

  @NotNull
  @JsonProperty("Parallelism")
  public abstract Long parallelism();

  @NotNull
  @JsonProperty("Delay")
  public abstract Long delay();

  @NotNull
  @JsonProperty("FailureAction")
  public abstract String failureAction();


  @JsonCreator
  static UpdateConfig create(
      @JsonProperty("Parallelism") final Long parallelism,
      @JsonProperty("Delay") final Long delay,
      @JsonProperty("FailureAction") final String failureAction) {
    return new AutoValue_UpdateConfig(parallelism, delay, failureAction);
  }
}
