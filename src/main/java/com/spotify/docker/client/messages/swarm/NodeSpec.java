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
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class NodeSpec {

  @Nullable
  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @JsonProperty("Role")
  public abstract String role();

  @JsonProperty("Availability")
  public abstract String availability();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder name(String name);

    abstract ImmutableMap.Builder<String, String> labelsBuilder();

    public Builder addLabel(final String label, final String value) {
      labelsBuilder().put(label, value);
      return this;
    }

    public abstract Builder labels(Map<String, String> labels);

    public abstract Builder role(String role);

    public abstract Builder availability(String availability);

    public abstract NodeSpec build();
  }

  public static NodeSpec.Builder builder() {
    return new AutoValue_NodeSpec.Builder();
  }

  public static NodeSpec.Builder builder(final NodeSpec source) {
    return new AutoValue_NodeSpec.Builder(source);
  }

  @JsonCreator
  static NodeSpec create(@JsonProperty("Name") final String name,
                         @JsonProperty("Labels") final Map<String, String> labels,
                         @JsonProperty("Role") final String role,
                         @JsonProperty("Availability") final String availability) {
    final Builder builder = builder()
        .name(name)
        .labels(labels)
        .role(role)
        .availability(availability);

    return builder.build();
  }
}
