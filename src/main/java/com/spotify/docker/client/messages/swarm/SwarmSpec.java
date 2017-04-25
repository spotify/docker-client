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

  @Nullable
  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @Nullable
  @JsonProperty("Orchestration")
  public abstract OrchestrationConfig orchestration();

  @Nullable
  @JsonProperty("Raft")
  public abstract RaftConfig raft();

  @Nullable
  @JsonProperty("Dispatcher")
  public abstract DispatcherConfig dispatcher();

  @Nullable
  @JsonProperty("CAConfig")
  public abstract CaConfig caConfig();

  @Nullable
  @JsonProperty("EncryptionConfig")
  public abstract EncryptionConfig encryptionConfig();

  @Nullable
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
      @JsonProperty("EncryptionConfig") final EncryptionConfig encryptionConfig,
      @JsonProperty("TaskDefaults") final TaskDefaults taskDefaults) {
    return builder()
        .name(name)
        .labels(labels)
        .orchestration(orchestration)
        .raft(raft)
        .dispatcher(dispatcher)
        .caConfig(caConfig)
        .encryptionConfig(encryptionConfig)
        .taskDefaults(taskDefaults)
        .build();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder name(String name);

    public abstract Builder labels(Map<String, String> labels);

    public abstract Builder orchestration(OrchestrationConfig orchestration);

    public abstract Builder raft(RaftConfig raft);

    public abstract Builder dispatcher(DispatcherConfig dispatcher);

    public abstract Builder caConfig(CaConfig caConfig);

    public abstract Builder encryptionConfig(EncryptionConfig encryptionConfig);

    public abstract Builder taskDefaults(TaskDefaults taskDefaults);

    public abstract SwarmSpec build();
  }

  public static SwarmSpec.Builder builder() {
    return new AutoValue_SwarmSpec.Builder();
  }
}
