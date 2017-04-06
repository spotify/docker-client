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
public abstract class EngineConfig {

  @JsonProperty("EngineVersion")
  public abstract String engineVersion();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @Nullable
  @JsonProperty("Plugins")
  public abstract ImmutableList<EnginePlugin> plugins();

  @JsonCreator
  static EngineConfig create(@JsonProperty("EngineVersion") final String engineVersion,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("Plugins") final List<EnginePlugin> plugins) {
    final ImmutableMap<String, String> labelsT = labels == null ? null
        : ImmutableMap.copyOf(labels);
    
    final ImmutableList<EnginePlugin> pluginsT = plugins == null ? null
        : ImmutableList.copyOf(plugins);
    return new AutoValue_EngineConfig(engineVersion, labelsT, pluginsT);
  }

}
