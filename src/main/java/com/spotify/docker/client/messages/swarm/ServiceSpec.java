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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

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

  public static class Builder {

    private ServiceSpec spec = new ServiceSpec();

    public Builder withName(String name) {
      spec.name = name;
      return this;
    }

    public Builder withLabel(String label, String value) {
      if (spec.labels == null) {
        spec.labels = new HashMap<String, String>();
      }
      spec.labels.put(label, value);
      return this;
    }

    public Builder withLabels(Map<String, String> labels) {
      if (spec.labels == null) {
        spec.labels = new HashMap<String, String>();
      }

      spec.labels.putAll(labels);
      return this;
    }

    public Builder withTaskTemplate(TaskSpec taskTemplate) {
      spec.taskTemplate = taskTemplate;
      return this;
    }

    public Builder withServiceMode(ServiceMode mode) {
      spec.mode = mode;
      return this;
    }

    public Builder withUpdateConfig(UpdateConfig updateConfig) {
      spec.updateConfig = updateConfig;
      return this;
    }

    public Builder withNetworks(NetworkAttachmentConfig... networks) {
      spec.networks = ImmutableList.copyOf(networks);
      return this;
    }

    public Builder withNetworks(List<NetworkAttachmentConfig> networks) {
      spec.networks = ImmutableList.copyOf(networks);
      return this;
    }

    public Builder withEndpointSpec(EndpointSpec endpointSpec) {
      spec.endpointSpec = endpointSpec;
      return this;
    }

    public ServiceSpec build() {
      return spec;
    }
  }

  public static ServiceSpec.Builder builder() {
    return new ServiceSpec.Builder();
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
