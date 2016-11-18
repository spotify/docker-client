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
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Task {

  @NotNull
  @JsonProperty("ID")
  public abstract String id();

  @NotNull
  @JsonProperty("Version")
  public abstract Version version();

  @NotNull
  @JsonProperty("CreatedAt")
  public abstract Date createdAt();

  @NotNull
  @JsonProperty("UpdatedAt")
  public abstract Date updatedAt();

  @Nullable
  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @NotNull
  @JsonProperty("Spec")
  public abstract TaskSpec spec();

  @NotNull
  @JsonProperty("ServiceID")
  public abstract String serviceId();

  @NotNull
  @JsonProperty("Slot")
  public abstract Integer slot();

  @NotNull
  @JsonProperty("NodeID")
  public abstract String nodeId();

  @NotNull
  @JsonProperty("Status")
  public abstract TaskStatus status();

  @NotNull
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
    @NotNull
    public abstract String taskId();

    /**
     * Filter by task name.
     */
    @Nullable
    public abstract String taskName();

    /**
     * Filter by service name.
     */
    @Nullable
    public abstract String serviceName();

    /**
     * Filter by node id.
     */
    @Nullable
    public abstract String nodeId();

    /**
     * Filter by label.
     */
    @Nullable
    public abstract String label();

    /**
     * Filter by desired state.
     */
    @Nullable
    public abstract String desiredState();

    public static Builder builder() {
      return new AutoValue_Task_Criteria.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder taskId(final String taskId);

      public abstract Builder taskName(final String taskName);

      public abstract Builder serviceName(final String serviceName);

      public abstract Builder nodeId(final String nodeId);

      public abstract Builder label(final String label);

      public abstract Builder desiredState(final String desiredState);

      public abstract Criteria build();
    }
  }

  public static Criteria.Builder find() {
    return AutoValue_Task_Criteria.builder();
  }
}
