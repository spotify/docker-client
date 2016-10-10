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

import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class TaskSpec {

  @JsonProperty("ContainerSpec")
  private ContainerSpec containerSpec;

  @JsonProperty("Resources")
  private ResourceRequirements resources;

  @JsonProperty("RestartPolicy")
  private RestartPolicy restartPolicy;

  @JsonProperty("Placement")
  private Placement placement;

  @JsonProperty("Networks")
  private ImmutableList<NetworkAttachmentConfig> networks;

  @JsonProperty("LogDriver")
  private Driver logDriver;

  public ContainerSpec containerSpec() {
    return containerSpec;
  }

  public ResourceRequirements resources() {
    return resources;
  }

  public RestartPolicy restartPolicy() {
    return restartPolicy;
  }

  public Placement placement() {
    return placement;
  }

  public List<NetworkAttachmentConfig> networks() {
    return networks;
  }

  public Driver logDriver() {
    return logDriver;
  }

  public static class Builder {

    private TaskSpec spec = new TaskSpec();

    public Builder withContainerSpec(ContainerSpec containerSpec) {
      spec.containerSpec = containerSpec;
      return this;
    }

    public Builder withResources(ResourceRequirements resources) {
      spec.resources = resources;
      return this;
    }

    public Builder withRestartPolicy(RestartPolicy restartPolicy) {
      spec.restartPolicy = restartPolicy;
      return this;
    }

    public Builder withPlacement(Placement placement) {
      spec.placement = placement;
      return this;
    }

    public Builder withNetworks(NetworkAttachmentConfig... networks) {
      if (networks != null && networks.length > 0) {
        spec.networks = ImmutableList.copyOf(networks);
      }
      return this;
    }

    public Builder withNetworks(List<NetworkAttachmentConfig> networks) {
      if (networks != null && !networks.isEmpty()) {
        spec.networks = ImmutableList.copyOf(networks);
      }
      return this;
    }

    public Builder withLogDriver(Driver logDriver) {
      spec.logDriver = logDriver;
      return this;
    }

    public TaskSpec build() {
      return spec;
    }
  }

  public static TaskSpec.Builder builder() {
    return new TaskSpec.Builder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final TaskSpec that = (TaskSpec) o;

    return Objects.equals(this.containerSpec, that.containerSpec)
           && Objects.equals(this.resources, that.resources)
           && Objects.equals(this.restartPolicy, that.restartPolicy)
           && Objects.equals(this.placement, that.placement)
           && Objects.equals(this.networks, that.networks)
           && Objects.equals(this.logDriver, that.logDriver);
  }

  @Override
  public int hashCode() {
    return Objects.hash(containerSpec, resources, restartPolicy, placement, networks,
                        logDriver);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("containerSpec", containerSpec)
        .add("resources", resources).add("restartPolicy", restartPolicy)
        .add("placement", placement).add("networks", networks).add("logDriver", logDriver)
        .toString();
  }
}
