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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.spotify.docker.client.DockerConfigReader;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.Nullable;
import org.glassfish.jersey.internal.util.Base64;

/**
 * Represents all the auth info for a particular registry.
 *
 * <p>These are sent to docker during authenticated registry operations
 * in the X-Registry-Config header (see {@link RegistryConfigs}).</p>
 *
 * <p>Typically these objects are built by requesting auth information from a
 * {@link com.spotify.docker.client.DockerCredentialHelper}. However, in older less-secure
 * docker versions, these can be written directly into the ~/.docker/config.json file,
 * with the username and password joined with a ":" and base-64 encoded.</p>
 */
@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class RegistryAuth {

  @Nullable
  @JsonProperty("username")
  public abstract String username();

  @Nullable
  @JsonProperty("password")
  public abstract String password();

  /**
   * Unused but must be a well-formed email address (e.g. 1234@5678.com).
   */
  @Nullable
  @JsonProperty("email")
  public abstract String email();

  @Nullable
  @JsonProperty("serveraddress")
  public abstract String serverAddress();

  @Nullable
  @JsonProperty("identitytoken")
  public abstract String identityToken();

  @Override
  public final String toString() {
    return MoreObjects.toStringHelper(RegistryAuth.class)
        .add("username", username())
        // don't log the password or email
        .add("serveraddress", serverAddress())
        .add("identitytoken", identityToken())
        .toString();
  }

  public abstract Builder toBuilder();

  /**
   * This function looks for and parses credentials for logging into Docker registries. We first
   * look in ~/.docker/config.json and fallback to ~/.dockercfg.
   * These files are created from running `docker login`.
   * If the file contains multiple credentials, which entry is returned from this method
   * depends on hashing and should not be considered reliable.
   *
   * @return a {@link Builder}
   * @throws IOException when we can't parse the docker config file
   * @deprecated in favor of {@link com.spotify.docker.client.auth.ConfigFileRegistryAuthSupplier}
   */
  @Deprecated
  public static Builder fromDockerConfig() throws IOException {
    return new DockerConfigReader().anyRegistryAuth().toBuilder();
  }

  /**
   * This function looks for and parses credentials for logging into the Docker registry specified
   * by serverAddress. We first look in ~/.docker/config.json and fallback to ~/.dockercfg. These
   * files are created from running `docker login`.
   *
   * @param serverAddress A string representing the server address
   * @return a {@link Builder}
   * @throws IOException when we can't parse the docker config file
   */
  @SuppressWarnings("unused")
  public static Builder fromDockerConfig(final String serverAddress) throws IOException {
    DockerConfigReader dockerCfgReader = new DockerConfigReader();
    return dockerCfgReader
        .authForRegistry(dockerCfgReader.defaultConfigPath(), serverAddress).toBuilder();
  }

  @JsonCreator
  public static RegistryAuth create(@JsonProperty("username") final String username,
                                    @JsonProperty("password") final String password,
                                    @JsonProperty("email") final String email,
                                    @JsonProperty("serveraddress") final String serveraddress,
                                    @JsonProperty("identitytoken") final String identitytoken,
                                    @JsonProperty("auth") final String auth) {

    final Builder builder;
    if (auth != null) {
      builder = forAuth(auth);
    } else {
      builder = builder()
          .username(username)
          .password(password);
    }
    return builder
        .email(email)
        .serverAddress(serveraddress)
        .identityToken(identitytoken)
        .build();
  }

  /** Construct a Builder based upon the "auth" field of the docker client config file. */
  public static Builder forAuth(final String auth) {
    // split with limit=2 to catch case where password contains a colon
    final String[] authParams = Base64.decodeAsString(auth).split(":", 2);

    if (authParams.length != 2) {
      return builder();
    }

    return builder()
        .username(authParams[0].trim())
        .password(authParams[1].trim());
  }

  public static Builder builder() {
    return new AutoValue_RegistryAuth.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder username(final String username);

    public abstract Builder password(final String password);

    public abstract Builder email(final String email);

    public abstract Builder serverAddress(final String serverAddress);

    public abstract Builder identityToken(final String token);

    public abstract RegistryAuth build();
  }
}
