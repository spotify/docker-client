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

/**
 * Represents the contents of the docker config.json file.
 */
@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class DockerConfig {

  @Nullable
  @JsonProperty("credHelpers")
  public abstract ImmutableMap<String, String> credHelpers();

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

  @Nullable
  @JsonProperty("psFormat")
  public abstract String psFormat();

  @Nullable
  @JsonProperty("imagesFormat")
  public abstract String imagesFormat();

  @JsonCreator
  public static DockerConfig create(
          @JsonProperty("credHelpers") final Map<String, String> credHelpers,
          @JsonProperty("auths") final Map<String, RegistryAuth> auths,
          @JsonProperty("HttpHeaders") final Map<String, String> httpHeaders,
          @JsonProperty("credsStore") final String credsStore,
          @JsonProperty("detachKeys") final String detachKeys,
          @JsonProperty("stackOrchestrator") final String stackOrchestrator,
          @JsonProperty("psFormat") final String psFormat,
          @JsonProperty("imagesFormat") final String imagesFormat) {
    return new AutoValue_DockerConfig(
        credHelpers == null
            ? ImmutableMap.<String, String>of()
            : ImmutableMap.copyOf(credHelpers),
        auths == null
            ? ImmutableMap.<String, RegistryAuth>of()
            : ImmutableMap.copyOf(auths),
        httpHeaders == null
            ? ImmutableMap.<String, String>of()
            : ImmutableMap.copyOf(httpHeaders),
        credsStore,
        detachKeys,
        stackOrchestrator,
        psFormat,
        imagesFormat);
  }
}
