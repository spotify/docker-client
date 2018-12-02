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
import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Task {

  @JsonProperty("ID")
  public abstract String id();

  @JsonProperty("Version")
  public abstract Version version();

  @JsonProperty("CreatedAt")
  public abstract Date createdAt();

  @JsonProperty("UpdatedAt")
  public abstract Date updatedAt();

  @Nullable
  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @JsonProperty("Spec")
  public abstract TaskSpec spec();

  @JsonProperty("ServiceID")
  public abstract String serviceId();

  @Nullable
  @JsonProperty("Slot")
  public abstract Integer slot();

  @Nullable
  @JsonProperty("NodeID")
  public abstract String nodeId();

  @JsonProperty("Status")
  public abstract TaskStatus status();

  @JsonProperty("DesiredState")
  public abstract String desiredState();

  @Nullable
  @JsonProperty("NetworksAttachments")
  public abstract ImmutableList<NetworkAttachment> networkAttachments();

  @JsonCreator
  static Task create(
      @JsonProperty("ID") final String id,
      @JsonProperty("Version") final Version version,
      @JsonProperty("CreatedAt") final Date createdAt,
      @JsonProperty("UpdatedAt") final Date updatedAt,
      @JsonProperty("Name") final String name,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("Spec") final TaskSpec spec,
      @JsonProperty("ServiceID") final String serviceId,
      @JsonProperty("Slot") final Integer slot,
      @JsonProperty("NodeID") final String nodeId,
      @JsonProperty("Status") final TaskStatus status,
      @JsonProperty("DesiredState") final String desiredState,
      @JsonProperty("NetworksAttachments") final List<NetworkAttachment> networkAttachments) {
    final ImmutableMap<String, String> labelsT = labels == null
                                                 ? null : ImmutableMap.copyOf(labels);
    final ImmutableList<NetworkAttachment> networkAttachmentsT =
        networkAttachments == null ? null : ImmutableList.copyOf(networkAttachments);
    return new AutoValue_Task(id, version, createdAt, updatedAt, name, labelsT,
        spec, serviceId, slot, nodeId, status, desiredState, networkAttachmentsT);
  }

  @AutoValue
  public abstract static class Criteria {

    /**
     * Filter by task id.
     */
    @Nullable
    public abstract List<String> taskIds();

    /**
     * Filter by task name.
     */
    @Nullable
    public abstract List<String> taskNames();

    /**
     * Filter by service name.
     */
    @Nullable
    public abstract List<String> serviceNames();

    /**
     * Filter by node id.
     */
    @Nullable
    public abstract List<String> nodeIds();

    /**
     * Filter by label.
     */
    @Nullable
    public abstract List<String> labels();

    /**
     * Filter by desired state.
     */
    @Nullable
    public abstract List<String> desiredStates();

    public static Builder builder() {
      return new AutoValue_Task_Criteria.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder taskIds(final List<String> taskIds);

      /**
       * @deprecated  As of release 7.0.0, replaced by {@link #taskId(String)}.
       */
      @Deprecated
      public Builder withTaskId(final String taskId) {
        taskIds(Collections.singletonList(taskId));
        return this;
      }

      public abstract Builder taskNames(final List<String> taskNames);

      /**
       * @deprecated  As of release 7.0.0, replaced by {@link #taskName(String)}.
       */
      @Deprecated
      public Builder withTaskName(final String taskName) {
        taskNames(Collections.singletonList(taskName));
        return this;
      }

      public abstract Builder serviceNames(final List<String> serviceNames);

      /**
       * @deprecated  As of release 7.0.0, replaced by {@link #serviceName(String)}.
       */
      @Deprecated
      public Builder withServiceName(final String serviceName) {
        serviceNames(Collections.singletonList(serviceName));
        return this;
      }

      public abstract Builder nodeIds(final List<String> nodeIds);

      /**
       * @deprecated  As of release 7.0.0, replaced by {@link #nodeId(String)}.
       */
      @Deprecated
      public Builder withNodeId(final String nodeId) {
        nodeIds(Collections.singletonList(nodeId));
        return this;
      }

      public abstract Builder labels(final List<String> labels);

      /**
       * @deprecated  As of release 7.0.0, replaced by {@link #label(String)}.
       */
      @Deprecated
      public Builder withLabel(final String label) {
        labels(Collections.singletonList(label));
        return this;
      }

      public abstract Builder desiredStates(final List<String> desiredStates);

      /**
       * @deprecated  As of release 7.0.0, replaced by {@link #desiredState(String)}.
       */
      @Deprecated
      public Builder withDesiredState(final String desiredState) {
        desiredStates(Collections.singletonList(desiredState));
        return this;
      }

      public abstract Criteria build();
    }
  }

  public static Criteria.Builder find() {
    return AutoValue_Task_Criteria.builder();
  }
}
