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
   * look in ~/.docker/config.json and fallback to ~/.dockercfg. We use the first credential in the
   * config file. These files are created from running `docker login`.
   *
   * @return a {@link Builder}
   * @throws IOException when we can't parse the docker config file
   * @deprecated in favor of registryAuthSupplier
   */
  @Deprecated
  @SuppressWarnings({"deprecated", "unused"})
  public static Builder fromDockerConfig() throws IOException {
    DockerConfigReader dockerCfgReader = new DockerConfigReader();
    return dockerCfgReader.fromFirstConfig(dockerCfgReader.defaultConfigPath()).toBuilder();
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
        .fromConfig(dockerCfgReader.defaultConfigPath(), serverAddress).toBuilder();
  }

  /**
   * Returns the first credential from the specified path to the docker file. This method is
   * package-local so we can test it.
   *
   * @param configPath The path to the config file
   * @return a {@link Builder}
   * @throws IOException when we can't parse the docker config file
   */
  @VisibleForTesting
  static Builder fromDockerConfig(final Path configPath) throws IOException {
    DockerConfigReader dockerCfgReader = new DockerConfigReader();
    return dockerCfgReader.fromConfig(configPath, null).toBuilder();
  }

  /**
   * Returns the specified credential from the specified path to the docker file. This method is
   * package-local so we can test it.
   *
   * @param configPath    The path to the config file
   * @param serverAddress A string representing the server address
   * @return a {@link Builder}
   * @throws IOException If an IOException occurred
   */
  @VisibleForTesting
  static Builder fromDockerConfig(final Path configPath, final String serverAddress)
      throws IOException {
    DockerConfigReader dockerConfigReader = new DockerConfigReader();
    return dockerConfigReader.fromConfig(configPath, serverAddress).toBuilder();
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
