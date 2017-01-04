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

package com.spotify.docker.client.messages;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.MoreObjects;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A formatted string passed in X-Registry-Config request header.
 *
 * <pre>
 * {
 *   "docker.example.com": {
 *     "serveraddress": "docker.example.com",
 *     "username": "janedoe",
 *     "password": "hunter2",
 *     "email": "janedoe@example.com",
 *     "auth": ""
 *   },
 *   "https://index.docker.io/v1/": {
 *     "serveraddress": "docker.example.com",
 *     "username": "mobydock",
 *     "password": "conta1n3rize14",
 *     "email": "mobydock@example.com",
 *     "auth": ""
 *   }
 * }
 * </pre>
 */
@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class RegistryConfigs {

  private static final RegistryConfigs EMPTY =
      RegistryConfigs.create(Collections.<String, RegistryConfig>emptyMap());

  public static RegistryConfigs empty() {
    return EMPTY;
  }

  public abstract ImmutableMap<String, RegistryConfig> configs();

  @AutoValue
  public abstract static class RegistryConfig {

    // The address of the repository
    @JsonProperty("serveraddress")
    public abstract String serverAddress();

    @Nullable
    @JsonProperty("username")
    public abstract String username();

    @Nullable
    @JsonProperty("password")
    public abstract String password();

    @Nullable
    @JsonProperty("email")
    public abstract String email();

    // Not used but must be supplied
    @JsonProperty("auth")
    public abstract String auth();

    public static RegistryConfig create(
        final String serveraddress,
        final String username,
        final String password,
        final String email) {
      return create(serveraddress, username, password, email, "");
    }

    @JsonCreator
    static RegistryConfig create(
        @JsonProperty("serveraddress") final String serveraddress,
        @JsonProperty("username") final String username,
        @JsonProperty("password") final String password,
        @JsonProperty("email") final String email,
        @JsonProperty("auth") final String auth) {
      return new AutoValue_RegistryConfigs_RegistryConfig(serveraddress, username, password, email,
          auth);
    }

    // Override @AutoValue to not leak password
    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("serverAddress", serverAddress())
          .add("username", username())
          .add("email", email())
          .add("auth", auth())
          .toString();
    }
  }

  @JsonCreator
  public static RegistryConfigs create(final Map<String, RegistryConfig> configs) {
    final ImmutableMap<String, RegistryConfig> configsT =
        configs == null ? ImmutableMap.<String, RegistryConfig>of() : ImmutableMap.copyOf(configs);
    return new AutoValue_RegistryConfigs(configsT);
  }
}
