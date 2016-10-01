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

        /** Filter by task id */
        String taskId;

        /** Filter by task name */
        String taskName;

        /** Filter by service name */
        String serviceName;

        /** Filter by node id */
        String nodeId;

        /** Filter by label */
        String label;

        /** Filter by desired state */
        String desiredState;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDesiredState() {
            return desiredState;
        }

        public void setDesiredState(String desiredState) {
            this.desiredState = desiredState;
        }
    }

    public static class CriteriaBuilder {

        /** Criteria being built */
        private Criteria criteria = new Criteria();

        public CriteriaBuilder withTaskId(String taskId) {
            criteria.setTaskId(taskId);
            return this;
        }

        public CriteriaBuilder withTaskName(String taskName) {
            criteria.setTaskName(taskName);
            return this;
        }

        public CriteriaBuilder withServiceName(String serviceName) {
            criteria.setServiceName(serviceName);
            return this;
        }

        public CriteriaBuilder withNodeId(String nodeId) {
            criteria.setNodeId(nodeId);
            return this;
        }

        public CriteriaBuilder withLabel(String label) {
            criteria.setLabel(label);
            return this;
        }

        public CriteriaBuilder withDesiredState(String desiredState) {
            criteria.setDesiredState(desiredState);
            return this;
        }

        public Criteria build() {
            return criteria;
        }
    }

    public static CriteriaBuilder find() {
        return new Task.CriteriaBuilder();
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
