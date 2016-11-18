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

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class SwarmSpec {

  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @JsonProperty("Orchestration")
  public abstract OrchestrationConfig orchestration();

  @JsonProperty("Raft")
  public abstract RaftConfig raft();

  @JsonProperty("Dispatcher")
  public abstract DispatcherConfig dispatcher();

  @JsonProperty("CAConfig")
  public abstract CaConfig caConfig();

  @JsonProperty("TaskDefaults")
  public abstract TaskDefaults taskDefaults();

  @JsonCreator
  static SwarmSpec create(
      @JsonProperty("Name") final String name,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("Orchestration") final OrchestrationConfig orchestration,
      @JsonProperty("Raft") final RaftConfig raft,
      @JsonProperty("Dispatcher") final DispatcherConfig dispatcher,
      @JsonProperty("CAConfig") final CaConfig caConfig,
      @JsonProperty("TaskDefaults") final TaskDefaults taskDefaults) {
    final ImmutableMap<String, String> labelsT = labels == null
                                                 ? null : ImmutableMap.copyOf(labels);
    return new AutoValue_SwarmSpec(name, labelsT, orchestration, raft, dispatcher, caConfig,
        taskDefaults);
  }
}
