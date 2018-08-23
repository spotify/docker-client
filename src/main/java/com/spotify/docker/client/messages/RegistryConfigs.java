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
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;

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

  public static RegistryConfigs empty() {
    return builder().build();
  }

  public abstract ImmutableMap<String, RegistryAuth> configs();

  @JsonCreator
  public static RegistryConfigs create(final Map<String, RegistryAuth> configs) {
    if (configs == null) {
      return empty();
    }

    // need to add serverAddress to each RegistryAuth instance; it is not available when
    // Jackson is deserializing the RegistryAuth field
    final Map<String, RegistryAuth> transformedMap = Maps.transformEntries(configs,
        new Maps.EntryTransformer<String, RegistryAuth, RegistryAuth>() {
          @Override
          public RegistryAuth transformEntry(final String key, final RegistryAuth value) {
            if (value == null) {
              return null;
            }
            if (value.serverAddress() == null) {
              return value.toBuilder()
                  .serverAddress(key)
                  .build();
            }
            return value;
          }
        });

    return builder().configs(transformedMap).build();
  }

  public static Builder builder() {
    return new AutoValue_RegistryConfigs.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder configs(Map<String, RegistryAuth> configs);

    abstract ImmutableMap.Builder<String, RegistryAuth> configsBuilder();

    public Builder addConfig(final String server, final RegistryAuth registryAuth) {
      configsBuilder().put(server, registryAuth);
      return this;
    }

    public abstract RegistryConfigs build();
  }
}
