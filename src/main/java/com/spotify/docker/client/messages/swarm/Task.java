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
import com.google.common.collect.ImmutableList;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Task {

  @JsonProperty("ID")
  private String id;

  @JsonProperty("Version")
  private Version version;

  @JsonProperty("CreatedAt")
  private Date createdAt;

  @JsonProperty("UpdatedAt")
  private Date updatedAt;

  @JsonProperty("Name")
  private String name;

  @JsonProperty("Labels")
  private Map<String, String> labels;

  @JsonProperty("Spec")
  private TaskSpec spec;

  @JsonProperty("ServiceID")
  private String serviceId;

  @JsonProperty("Slot")
  private Integer slot;

  @JsonProperty("NodeID")
  private String nodeId;

  @JsonProperty("Status")
  private TaskStatus status;

  @JsonProperty("DesiredState")
  private String desiredState;

  @JsonProperty("NetworksAttachments")
  private ImmutableList<NetworkAttachment> networkAttachments;

  public String id() {
    return id;
  }

  public Version version() {
    return version;
  }

  public Date createdAt() {
    return createdAt == null ? null : new Date(createdAt.getTime());
  }

  public Date updatedAt() {
    return updatedAt == null ? null : new Date(updatedAt.getTime());
  }

  public String name() {
    return name;
  }

  public Map<String, String> labels() {
    return labels;
  }

  public TaskSpec spec() {
    return spec;
  }

  public String serviceId() {
    return serviceId;
  }

  public Integer slot() {
    return slot;
  }

  public String nodeId() {
    return nodeId;
  }

  public TaskStatus status() {
    return status;
  }

  public String desiredState() {
    return desiredState;
  }

  public List<NetworkAttachment> networkAttachments() {
    return networkAttachments;
  }

  public static class Criteria {

    /**
     * Filter by task id
     */
    private String taskId;
    /**
     * Filter by task name
     */
    private String taskName;
    /**
     * Filter by service name
     */
    private String serviceName;
    /**
     * Filter by node id
     */
    private String nodeId;
    /**
     * Filter by label
     */
    private String label;
    /**
     * Filter by desired state
     */
    private String desiredState;

    private Criteria(final Builder builder) {
      this.taskId = builder.taskId;
      this.taskName = builder.taskName;
      this.serviceName = builder.serviceName;
      this.nodeId = builder.nodeId;
      this.label = builder.label;
      this.desiredState = builder.desiredState;
    }

    public String getTaskId() {
      return taskId;
    }

    public String getTaskName() {
      return taskName;
    }

    public String getServiceName() {
      return serviceName;
    }

    public String getNodeId() {
      return nodeId;
    }

    public String getLabel() {
      return label;
    }

    public String getDesiredState() {
      return desiredState;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {

      private String taskId;
      private String taskName;
      private String serviceName;
      private String nodeId;
      private String label;
      private String desiredState;

      public Builder withTaskId(final String taskId) {
        this.taskId = taskId;
        return this;
      }

      public Builder withTaskName(final String taskName) {
        this.taskName = taskName;
        return this;
      }

      public Builder withServiceName(final String serviceName) {
        this.serviceName = serviceName;
        return this;
      }

      public Builder withNodeId(final String nodeId) {
        this.nodeId = nodeId;
        return this;
      }

      public Builder withLabel(final String label) {
        this.label = label;
        return this;
      }

      public Builder withDesiredState(final String desiredState) {
        this.desiredState = desiredState;
        return this;
      }

      public Criteria build() {
        return new Criteria(this);
      }
    }
  }

  public static Criteria.Builder find() {
    return Task.Criteria.builder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Task that = (Task) o;

    return Objects.equals(this.id, that.id) && Objects.equals(this.version, that.version)
           && Objects.equals(this.createdAt, that.createdAt)
           && Objects.equals(this.updatedAt, that.updatedAt)
           && Objects.equals(this.name, that.name) && Objects.equals(this.labels, that.labels)
           && Objects.equals(this.spec, that.spec)
           && Objects.equals(this.serviceId, that.serviceId)
           && Objects.equals(this.slot, that.slot) && Objects.equals(this.nodeId, that.nodeId)
           && Objects.equals(this.status, that.status)
           && Objects.equals(this.desiredState, that.desiredState)
           && Objects.equals(this.networkAttachments, that.networkAttachments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, version, createdAt, updatedAt, name, labels, spec, serviceId, slot,
                        nodeId, status, desiredState, networkAttachments);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("version", version)
        .add("createdAt", createdAt).add("updatedAt", updatedAt).add("name", name)
        .add("labels", labels).add("spec", spec).add("serviceId", serviceId)
        .add("slot", slot).add("nodeId", nodeId).add("status", status)
        .add("desiredState", desiredState).add("networkAttachments", networkAttachments)
        .toString();
  }
}
