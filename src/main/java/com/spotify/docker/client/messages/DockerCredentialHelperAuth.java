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

package com.spotify.docker.client.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

/**
 * Represents the auth response received from a docker credential helper
 * on a "get" operation, or sent to a credential helper on a "store".
 *
 * <p>See {@link com.spotify.docker.client.DockerCredentialHelper}.</p>
 */
@AutoValue
public abstract class DockerCredentialHelperAuth {
  @JsonProperty("Username")
  public abstract String username();

  @JsonProperty("Secret")
  public abstract String secret();

  @JsonProperty("ServerURL")
  public abstract String serverUrl();

  @JsonCreator
  public static DockerCredentialHelperAuth create(
        @JsonProperty("Username") final String username,
        @JsonProperty("Secret") final String secret,
        @JsonProperty("ServerURL") final String serverUrl) {
    return new AutoValue_DockerCredentialHelperAuth(username, secret, serverUrl);
  }

  @JsonIgnore
  public RegistryAuth toRegistryAuth() {
    return RegistryAuth.builder()
        .username(username())
        .password(secret())
        .serverAddress(serverUrl())
        .build();
  }
}
