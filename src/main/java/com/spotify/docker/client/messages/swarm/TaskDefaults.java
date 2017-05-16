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

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class TaskDefaults {

  @Nullable
  @JsonProperty("LogDriver")
  public abstract Driver logDriver();

  @JsonCreator
  static TaskDefaults create(@JsonProperty("LogDriver") final Driver logDriver) {
    return builder()
        .logDriver(logDriver)
        .build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder logDriver(Driver logDriver);

    public abstract TaskDefaults build();
  }

  public static TaskDefaults.Builder builder() {
    return new AutoValue_TaskDefaults.Builder();
  }
}
