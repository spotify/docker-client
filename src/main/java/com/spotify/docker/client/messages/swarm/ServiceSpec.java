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
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ServiceSpec {

  @Nullable
  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @JsonProperty("TaskTemplate")
  public abstract TaskSpec taskTemplate();

  @Nullable
  @JsonProperty("Mode")
  public abstract ServiceMode mode();

  @Nullable
  @JsonProperty("UpdateConfig")
  public abstract UpdateConfig updateConfig();

  @Nullable
  @JsonProperty("Networks")
  public abstract ImmutableList<NetworkAttachmentConfig> networks();

  @Nullable
  @JsonProperty("EndpointSpec")
  public abstract EndpointSpec endpointSpec();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder name(String name);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #name(String)}.
     */
    @Deprecated
    public Builder withName(String name) {
      name(name);
      return this;
    }

    abstract ImmutableMap.Builder<String, String> labelsBuilder();

    public Builder addLabel(final String label, final String value) {
      labelsBuilder().put(label, value);
      return this;
    }

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #addLabel(String, String)}.
     */
    @Deprecated
    public Builder withLabel(final String label, final String value) {
      addLabel(label, value);
      return this;
    }

    public abstract Builder labels(Map<String, String> labels);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #labels(Map)}.
     */
    @Deprecated
    public Builder withLabels(final Map<String, String> labels) {
      labels(labels);
      return this;
    }

    public abstract Builder taskTemplate(TaskSpec taskTemplate);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #taskTemplate(TaskSpec)}.
     */
    @Deprecated
    public Builder withTaskTemplate(TaskSpec taskTemplate) {
      taskTemplate(taskTemplate);
      return this;
    }

    public abstract Builder mode(ServiceMode mode);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #mode(ServiceMode)}.
     */
    @Deprecated
    public Builder withServiceMode(final ServiceMode mode) {
      mode(mode);
      return this;
    }

    public abstract Builder updateConfig(UpdateConfig updateConfig);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #updateConfig(UpdateConfig)}.
     */
    @Deprecated
    public Builder withUpdateConfig(final UpdateConfig updateConfig) {
      updateConfig(updateConfig);
      return this;
    }

    public abstract Builder networks(NetworkAttachmentConfig... networks);

    public abstract Builder networks(List<NetworkAttachmentConfig> networks);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #networks(NetworkAttachmentConfig...)}.
     */
    @Deprecated
    public Builder withNetworks(NetworkAttachmentConfig... networks) {
      networks(networks);
      return this;
    }

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #networks(List)}.
     */
    @Deprecated
    public Builder withNetworks(List<NetworkAttachmentConfig> networks) {
      networks(networks);
      return this;
    }

    public abstract Builder endpointSpec(EndpointSpec endpointSpec);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #endpointSpec(EndpointSpec)}.
     */
    @Deprecated
    public Builder withEndpointSpec(final EndpointSpec endpointSpec) {
      endpointSpec(endpointSpec);
      return this;
    }

    public abstract ServiceSpec build();
  }

  public static ServiceSpec.Builder builder() {
    return new AutoValue_ServiceSpec.Builder();
  }

  @JsonCreator
  static ServiceSpec create(
      @JsonProperty("Name") final String name,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("TaskTemplate") final TaskSpec taskTemplate,
      @JsonProperty("Mode") final ServiceMode mode,
      @JsonProperty("UpdateConfig") final UpdateConfig updateConfig,
      @JsonProperty("Networks") final List<NetworkAttachmentConfig> networks,
      @JsonProperty("EndpointSpec") final EndpointSpec endpointSpec) {
    return builder()
        .name(name)
        .labels(labels)
        .taskTemplate(taskTemplate)
        .mode(mode)
        .updateConfig(updateConfig)
        .endpointSpec(endpointSpec)
        .networks(networks)
        .build();
  }
}
