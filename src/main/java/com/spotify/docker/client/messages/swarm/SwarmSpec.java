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

import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class SwarmSpec {

  @JsonProperty("Name")
  private String name;

  @JsonProperty("Labels")
  private Map<String, String> labels;

  @JsonProperty("Orchestration")
  private OrchestrationConfig orchestration;

  @JsonProperty("Raft")
  private RaftConfig raft;

  @JsonProperty("Dispatcher")
  private DispatcherConfig dispatcher;

  @JsonProperty("CAConfig")
  private CaConfig caConfig;

  @JsonProperty("TaskDefaults")
  private TaskDefaults taskDefaults;

  public String name() {
    return name;
  }

  public Map<String, String> labels() {
    return labels;
  }

  public OrchestrationConfig orchestration() {
    return orchestration;
  }

  public RaftConfig raft() {
    return raft;
  }

  public DispatcherConfig dispatcher() {
    return dispatcher;
  }

  public CaConfig caConfig() {
    return caConfig;
  }

  public TaskDefaults taskDefaults() {
    return taskDefaults;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final SwarmSpec that = (SwarmSpec) o;

    return Objects.equals(this.name, that.name) && Objects.equals(this.labels, that.labels)
           && Objects.equals(this.orchestration, that.orchestration)
           && Objects.equals(this.raft, that.raft)
           && Objects.equals(this.dispatcher, that.dispatcher)
           && Objects.equals(this.caConfig, that.caConfig)
           && Objects.equals(this.taskDefaults, that.taskDefaults);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, labels, orchestration, raft, dispatcher, caConfig, taskDefaults);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("name", name).add("labels", labels)
        .add("orchestration", orchestration).add("raft", raft).add("dispatcher", dispatcher)
        .add("caConfig", caConfig).add("taskDefaults", taskDefaults).toString();
  }
}
