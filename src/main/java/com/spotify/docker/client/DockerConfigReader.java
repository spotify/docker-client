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

package com.spotify.docker.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.messages.RegistryAuth;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import org.glassfish.jersey.internal.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerConfigReader {
  private static final Logger LOG = LoggerFactory.getLogger(DockerConfigReader.class);

  private static final ObjectMapper MAPPER = ObjectMapperProvider.objectMapper();


  public RegistryAuth fromComfig(Path configPath, String serverAddress) throws IOException {
    return parseDockerConfig(configPath, serverAddress).build();
  }

  public RegistryAuth.Builder parseDockerConfig(final Path configPath, String serverAddress)
      throws IOException {
    checkNotNull(configPath);
    final RegistryAuth.Builder authBuilder = RegistryAuth.builder();
    final JsonNode authJson = this.extractAuthJson(configPath);

    if (isNullOrEmpty(serverAddress)) {
      final Iterator<String> servers = authJson.fieldNames();
      if (servers.hasNext()) {
        serverAddress = servers.next();
      }
    } else {
      if (!authJson.has(serverAddress)) {
        LOG.error("Could not find auth config for {}. Returning empty builder", serverAddress);
        return RegistryAuth.builder().serverAddress(serverAddress);
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
      } else if (serverAuth.has("identityToken")) {
        authBuilder.identityToken(serverAuth.get("identityToken").asText());
        return authBuilder;
      } else {
        LOG.warn("Failed to parse auth string for {}", serverAddress);
        return authBuilder;
      }
    } else {
      LOG.warn("Could not find auth field for {}", serverAddress);
      return authBuilder;
    }

    if (serverAuth.has("email")) {
      authBuilder.email(serverAuth.get("email").asText());
    }

    return authBuilder;
  }

  public Path defaultConfigPath() {
    final String home = System.getProperty("user.home");
    final Path dockerConfig = Paths.get(home, ".docker", "config.json");
    final Path dockerCfg = Paths.get(home, ".dockercfg");

    if (Files.exists(dockerConfig)) {
      LOG.debug("Using configfile: {}", dockerConfig);
      return dockerConfig;
    } else {
      LOG.debug("Using configfile: {} ", dockerCfg);
      return dockerCfg;
    }
  }

  public JsonNode extractAuthJson(final Path configPath) throws IOException {
    final JsonNode config = MAPPER.readTree(configPath.toFile());

    if (config.has("auths")) {
      return config.get("auths");
    }

    return config;
  }
}
