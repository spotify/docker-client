/*
 * Copyright (c) 2014 CyDesign Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.docker.client.messages;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.glassfish.jersey.internal.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class AuthConfig {

  private static final Logger log = LoggerFactory.getLogger(AuthConfig.class);

  @SuppressWarnings("FieldCanBeLocal")
  // ObjectMapper is thread-safe and this saves us from having instantiate it every time
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @JsonProperty("Username")
  private String username;

  @JsonProperty("Password")
  private String password;

  @JsonProperty("Email")
  private String email;

  @JsonProperty("ServerAddress")
  private String serverAddress;

  @SuppressWarnings("unused")
  private AuthConfig() {
  }

  private AuthConfig(final Builder builder) {
    this.username = builder.username;
    this.password = builder.password;
    this.email = builder.email;
    this.serverAddress = builder.serverAddress;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }

  public String email() {
    return email;
  }

  public String serverAddress() {
    return serverAddress;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final AuthConfig that = (AuthConfig) o;

    return Objects.equals(this.username, that.username) &&
           Objects.equals(this.password, that.password) &&
           Objects.equals(this.email, that.email) &&
           Objects.equals(this.serverAddress, that.serverAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, password, email, serverAddress);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .add("password", password)
        .add("email", email)
        .add("serverAddress", serverAddress)
        .toString();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  /**
   * This function looks for and parses credentials for logging into Docker registries. We first
   * look in ~/.docker/config.json and fallback to ~/.dockercfg. We use the first credential in the
   * config file. These files are created from running `docker login`.
   *
   * @return a {@link Builder}
   * @throws IOException when we can't parse the docker config file
   */
  @SuppressWarnings("unused")
  public static Builder fromDockerConfig() throws IOException {
    return parseDockerConfig(defaultConfigPath(), null);
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
    return parseDockerConfig(defaultConfigPath(), serverAddress);
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
    return parseDockerConfig(configPath, null);
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
    return parseDockerConfig(configPath, serverAddress);
  }

  private static Path defaultConfigPath() {
    final String home = System.getProperty("user.home");
    final Path dockerConfig = Paths.get(home, ".docker", "config.json");
    final Path dockerCfg = Paths.get(home, ".dockercfg");

    if (Files.exists(dockerConfig)) {
      log.debug("Using configfile: {}", dockerConfig);
      return dockerConfig;
    } else if (Files.exists(dockerCfg)) {
      log.debug("Using configfile: {} ", dockerCfg);
      return dockerCfg;
    } else {
      throw new RuntimeException(
          "Could not find a docker config. Please run 'docker login' to create one");
    }
  }

  private static AuthConfig.Builder parseDockerConfig(final Path configPath, String serverAddress)
      throws IOException {
    checkNotNull(configPath);
    final AuthConfig.Builder authBuilder = AuthConfig.builder();
    final JsonNode authJson = extractAuthJson(configPath);

    if (isNullOrEmpty(serverAddress)) {
      final Iterator<String> servers = authJson.fieldNames();
      if (servers.hasNext()) {
        serverAddress = servers.next();
      }
    } else {
      if (!authJson.has(serverAddress)) {
        log.error("Could not find auth config for {}. Returning empty builder", serverAddress);
        return AuthConfig.builder().serverAddress(serverAddress);
      }
    }

    final JsonNode serverAuth = authJson.get(serverAddress);
    if (serverAuth != null && serverAuth.has("auth")) {
      authBuilder.serverAddress(serverAddress);
      final String authString = serverAuth.get("auth").asText();
      final String[] authParams = Base64.decodeAsString(authString).split(":");

      if (authParams.length == 2) {
        authBuilder.username(authParams[0].trim());
        authBuilder.password(authParams[1].trim());
      } else {
        log.warn("Failed to parse auth string for {}", serverAddress);
        return authBuilder;
      }
    } else {
      log.warn("Could not find auth field for {}", serverAddress);
      return authBuilder;
    }

    if (serverAuth.has("email")) {
      authBuilder.email(serverAuth.get("email").asText());
    }

    return authBuilder;
  }

  private static JsonNode extractAuthJson(final Path configPath) throws IOException {
    final JsonNode config = MAPPER.readTree(configPath.toFile());

    if (config.has("auths")) {
      return config.get("auths");
    }

    return config;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String username;
    private String password;
    private String email;
    // Default to the public Docker registry.
    private String serverAddress = "https://index.docker.io/v1/";

    private Builder() {
    }

    private Builder(final AuthConfig config) {
      this.username = config.username;
      this.password = config.password;
      this.email = config.email;
      this.serverAddress = config.serverAddress;
    }

    public Builder username(final String username) {
      this.username = username;
      return this;
    }

    public String username() {
      return username;
    }

    public Builder password(final String password) {
      this.password = password;
      return this;
    }

    public String password() {
      return password;
    }

    public Builder email(final String email) {
      this.email = email;
      return this;
    }

    public String email() {
      return email;
    }

    public Builder serverAddress(final String serverAddress) {
      this.serverAddress = serverAddress;
      return this;
    }

    public String serverAddress() {
      return serverAddress;
    }

    public AuthConfig build() {
      return new AuthConfig(this);
    }
  }
}
