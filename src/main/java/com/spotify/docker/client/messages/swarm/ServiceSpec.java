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

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ServiceSpec {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Labels")
    private Map<String, String> labels;

    @JsonProperty("TaskTemplate")
    private TaskSpec taskTemplate;

    @JsonProperty("Mode")
    private ServiceMode mode;

    @JsonProperty("UpdateConfig")
    private UpdateConfig updateConfig;

    @JsonProperty("Networks")
    private ImmutableList<NetworkAttachmentConfig> networks;

    @JsonProperty("EndpointSpec")
    private EndpointSpec endpointSpec;

    public String name() {
        return name;
    }

    public Map<String, String> labels() {
        return labels;
    }

    public TaskSpec taskTemplate() {
        return taskTemplate;
    }

    public ServiceMode mode() {
        return mode;
    }

    public UpdateConfig updateConfig() {
        return updateConfig;
    }

    public List<NetworkAttachmentConfig> networks() {
        return networks;
    }

    public EndpointSpec endpointSpec() {
        return endpointSpec;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ServiceSpec that = (ServiceSpec) o;

        return Objects.equals(this.name, that.name) && Objects.equals(this.labels, that.labels)
                && Objects.equals(this.taskTemplate, that.taskTemplate)
                && Objects.equals(this.mode, that.mode)
                && Objects.equals(this.updateConfig, that.updateConfig)
                && Objects.equals(this.networks, that.networks)
                && Objects.equals(this.endpointSpec, that.endpointSpec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, labels, taskTemplate, mode, updateConfig, networks, endpointSpec);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("labels", labels)
                .add("taskTemplate", taskTemplate).add("mode", mode)
                .add("updateConfig", updateConfig).add("networks", networks)
                .add("endpointSpec", endpointSpec).toString();
    }
}
