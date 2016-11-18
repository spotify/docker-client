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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ServiceSpec {

  @NotNull
  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @NotNull
  @JsonProperty("TaskTemplate")
  public abstract TaskSpec taskTemplate();

  @NotNull
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

    @JsonProperty("Name")
    public abstract Builder name(String name);

    abstract ImmutableMap.Builder<String, String> labelsBuilder();

    public Builder addLabel(final String label, final String value) {
      labelsBuilder().put(label, value);
      return this;
    }

    @JsonProperty("Labels")
    public abstract Builder labels(Map<String, String> labels);

    @JsonProperty("TaskTemplate")
    public abstract Builder taskTemplate(TaskSpec taskTemplate);

    @JsonProperty("Mode")
    public abstract Builder mode(ServiceMode mode);

    @JsonProperty("UpdateConfig")
    public abstract Builder updateConfig(UpdateConfig updateConfig);

    @JsonProperty("Networks")
    public abstract Builder networks(NetworkAttachmentConfig... networks);

    @JsonProperty("Networks")
    public abstract Builder networks(List<NetworkAttachmentConfig> networks);

    @JsonProperty("EndpointSpec")
    public abstract Builder endpointSpec(EndpointSpec endpointSpec);

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
    final Builder builder = builder()
        .name(name)
        .labels(labels)
        .taskTemplate(taskTemplate)
        .mode(mode)
        .updateConfig(updateConfig)
        .endpointSpec(endpointSpec);

    if (labels != null) {
      builder.labels(labels);
    }
    if (networks != null) {
      builder.networks(networks);
    }

    return builder.build();
  }
}
