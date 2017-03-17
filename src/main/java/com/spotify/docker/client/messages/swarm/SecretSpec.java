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
public abstract class SecretSpec {

  @JsonProperty("Name")
  public abstract String name();
  
  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @Nullable
  @JsonProperty("Data")
  public abstract String data();
  
  public static Builder builder() {
    return new AutoValue_SecretSpec.Builder();
  }
  
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder name(String name);
    
    public abstract Builder labels(Map<String, String> labels);
    
    public abstract Builder data(String data);
    
    public abstract SecretSpec build();
  }

  @JsonCreator
  static SecretSpec create(
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
