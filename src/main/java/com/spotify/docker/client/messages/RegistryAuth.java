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
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.spotify.docker.client.DockerConfigReader;
import com.spotify.docker.client.ObjectMapperProvider;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class RegistryAuth {

  private static final Logger LOG = LoggerFactory.getLogger(RegistryAuth.class);

  private static final String DEFAULT_REGISTRY = "https://index.docker.io/v1/";
  private static final String DUMMY_EMAIL = "1234@5678.com";

  @SuppressWarnings("FieldCanBeLocal")
  private static final ObjectMapper MAPPER =
      new ObjectMapperProvider().getContext(RegistryAuth.class);

  @Nullable
  @JsonProperty("Username")
  public abstract String username();

  @Nullable
  @JsonProperty("Password")
  public abstract String password();

  /**
   * Unused but must be a well-formed email address (e.g. 1234@5678.com).
   */
  @Nullable
  @JsonProperty("Email")
  public abstract String email();

  @JsonProperty("ServerAddress")
  public abstract String serverAddress();

  @Nullable
  @JsonProperty("IdentityToken")
  public abstract String identityToken();

  public final String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username())
        // don't log the password or email
        .add("serverAddress", serverAddress())
        .add("identityToken", identityToken())
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
  @SuppressWarnings("unused")
  public static Builder fromDockerConfig() throws IOException {
    DockerConfigReader dockerCfgReader = new DockerConfigReader();
    return dockerCfgReader.fromComfig(dockerCfgReader.defaultConfigPath(), null).toBuilder();
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
        .fromComfig(dockerCfgReader.defaultConfigPath(), serverAddress).toBuilder();
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
    return dockerCfgReader.fromComfig(configPath, null).toBuilder();
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
    return dockerConfigReader.fromComfig(configPath, serverAddress).toBuilder();
  }


  public static Builder builder() {
    return new AutoValue_RegistryAuth.Builder()
        // Default to the public Docker registry.
        .serverAddress(DEFAULT_REGISTRY)
        .email(DUMMY_EMAIL);
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
