/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 - 2017 Spotify AB
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
public abstract class ConfigSpec {

  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @Nullable
  @JsonProperty("Data")
  public abstract String data();

  public static ConfigSpec.Builder builder() {
    return new AutoValue_ConfigSpec.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract ConfigSpec.Builder name(String name);

    public abstract ConfigSpec.Builder labels(Map<String, String> labels);

    /**
     * Base64-url-safe-encoded secret data.
     *
     * @param data the config data.
     * @return the builder
     */
    public abstract ConfigSpec.Builder data(String data);

    public abstract ConfigSpec build();
  }

  @JsonCreator
  static ConfigSpec create(
      @JsonProperty("Name") String name,
      @JsonProperty("Labels") Map<String, String> labels,
      @JsonProperty("Data") String data) {
    return builder()
        .name(name)
        .labels(labels)
        .data(data)
        .build();
  }
}
