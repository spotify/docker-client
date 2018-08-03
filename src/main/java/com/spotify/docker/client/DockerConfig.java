/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 - 2018 Spotify AB
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

package com.spotify.docker.client;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.RegistryAuth;

import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class DockerConfig {

  @Nullable
  @JsonProperty("credsHelpers")
  public abstract ImmutableMap<String, String> credsHelpers();

  @Nullable
  @JsonProperty("auths")
  public abstract ImmutableMap<String, RegistryAuth> auths();

  @Nullable
  @JsonProperty("HttpHeaders")
  public abstract ImmutableMap<String, String> httpHeaders();

  @Nullable
  @JsonProperty("credsStore")
  public abstract String credsStore();

  @Nullable
  @JsonProperty("detachKeys")
  public abstract String detachKeys();

  @Nullable
  @JsonProperty("stackOrchestrator")
  public abstract String stackOrchestrator();

  @JsonCreator
  public static DockerConfig create(
          @JsonProperty("credsHelpers") final Map<String, String> credsHelpers,
          @JsonProperty("auths") final Map<String, RegistryAuth> auths,
          @JsonProperty("HttpHeaders") final Map<String, String> httpHeaders,
          @JsonProperty("credsStore") final String credsStore,
          @JsonProperty("detachKeys") final String detachKeys,
          @JsonProperty("stackOrchestrator") final String stackOrchestrator) {
    return new AutoValue_DockerConfig(
        credsHelpers == null
            ? ImmutableMap.<String, String>of()
            : ImmutableMap.copyOf(credsHelpers),
        auths == null
            ? ImmutableMap.<String, RegistryAuth>of()
            : ImmutableMap.copyOf(auths),
        httpHeaders == null
            ? ImmutableMap.<String, String>of()
            : ImmutableMap.copyOf(httpHeaders),
        credsStore,
        detachKeys,
        stackOrchestrator);
  }
}
