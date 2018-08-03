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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableCollection;
import com.spotify.docker.client.messages.DockerCredentialHelper;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerConfigReader {
  private static final Logger LOG = LoggerFactory.getLogger(DockerConfigReader.class);

  private static final ObjectMapper MAPPER = ObjectMapperProvider.objectMapper();

  /**
   * Parse the contents of the config file and generate all possible
   * {@link RegistryAuth}s, which are bundled into a {@link RegistryConfigs} instance.
   * @param configPath Path to config file.
   * @return All registry configs that can be generated from the config file
   * @throws IOException If the file cannot be read, or its JSON cannot be parsed
   * @deprecated Use {@link #authForAllRegistries(Path)} instead.
   */
  @Deprecated
  public RegistryConfigs fromConfig(final Path configPath) throws IOException {
    return authForAllRegistries(configPath);
  }

  /**
   * Returns the RegistryAuth for the config file for the given registry server name.
   *
   * @throws IllegalArgumentException if the config file does not contain registry auth info for the
   *                                  registry
   */
  public RegistryAuth fromConfig(final Path configPath, final String serverAddress)
      throws IOException {
    return parseDockerConfig(configPath, serverAddress);
  }

  /**
   * Return a single RegistryAuth from the default config file.
   * If there is only one, it'll be that one.
   *
   * @return Some registry auth value.
   */
  public RegistryAuth anyRegistryAuth() throws IOException {
    return anyRegistryAuth(defaultConfigPath());
  }

  /**
   * Return a single RegistryAuth from the config file.
   * If there are multiple RegistryAuth entries, which entry is returned from this method
   * depends on hashing and should not be considered reliable.
   * If there is only one entry, however, that will be the one returned. This is the
   * primary use of this method, as a useful way to extract a RegistryAuth during testing.
   * In that environment, the contents of the config file are known and controlled. In a
   * production environment, the contents of the config file are less predictable.
   *
   * @param configPath Path to the docker config file.
   * @return Some registry auth value.
   */
  @VisibleForTesting
  RegistryAuth anyRegistryAuth(final Path configPath) throws IOException {
    final ImmutableCollection<RegistryAuth> registryAuths =
        authForAllRegistries(configPath).configs().values();
    return registryAuths.isEmpty()
        ? RegistryAuth.builder().build()
        : registryAuths.iterator().next();
  }

  /**
   * Parse the contents of the config file and generate all possible
   * {@link RegistryAuth}s, which are bundled into a {@link RegistryConfigs} instance.
   * @param configPath Path to config file.
   * @return All registry auths that can be generated from the config file
   * @throws IOException If the file cannot be read, or its JSON cannot be parsed
   */
  public RegistryConfigs authForAllRegistries(final Path configPath) throws IOException {
    checkNotNull(configPath);

    final DockerConfig config = MAPPER.readValue(configPath.toFile(), DockerConfig.class);
    if (config == null) {
      return RegistryConfigs.empty();
    }

    final RegistryConfigs.Builder registryConfigsBuilder = RegistryConfigs.builder();

    final Map<String, String> credsHelpers = config.credsHelpers();
    final boolean hasCredsHelpers = credsHelpers != null && !credsHelpers.isEmpty();
    final Map<String, RegistryAuth> auths = config.auths();
    final boolean hasAuths = auths != null && !auths.isEmpty();
    final String credsStore = config.credsStore();
    final boolean hasCredsStore = credsStore != null;

    // First use the credsHelpers, if there are any
    if (hasCredsHelpers) {
      for (final String registry : credsHelpers.keySet()) {
        final String aCredsStore = credsHelpers.get(registry);
        registryConfigsBuilder.addConfig(registry,
            authWithCredentialHelper(aCredsStore, registry));
      }
    }

    // If there are any objects in "auths", they could take two forms.
    // Older auths will map registry keys to objects with "auth" values, sometimes emails.
    // Newer auths will map registry keys to empty objects. They expect you
    // to use the credsStore to authenticate.
    if (hasAuths) {
      // We will use this empty RegistryAuth to check for empty auth values
      final RegistryAuth empty = RegistryAuth.builder().build();

      for (final String registry : auths.keySet()) {
        final RegistryAuth registryAuth = auths.get(registry);
        if (registryAuth == null || registryAuth.equals(empty)) {
          // We have an empty object. Can we use credsStore?
          if (credsStore != null) {
            registryConfigsBuilder.addConfig(registry,
                authWithCredentialHelper(credsStore, registry));
          } // no else clause. If we can't fall back to credsStore, we can't auth.
        } else {
          // The auth object isn't empty.
          // We need to add the registry to its properties, then
          // add it to the RegistryConfigs
          registryConfigsBuilder.addConfig(registry,
              registryAuth.toBuilder().serverAddress(registry).build());
        }
      }
    }

    // If there are no credsHelpers or auths or credsStore, then the
    // config may be in a very old format. There aren't any keys for different
    // sections. The file is just a map of registries to auths.
    // In other words, it looks like a RegistryConfigs.
    // If we can map it to one, we'll return it.
    if (!(hasAuths || hasCredsHelpers || hasCredsStore)) {
      try {
        return MAPPER.readValue(configPath.toFile(), RegistryConfigs.class);
      } catch (IOException ignored) {
        // Looks like that failed to parse.
        // Eat the exception, fall through, and return empty object.
      }
    }

    return registryConfigsBuilder.build();
  }

  private RegistryAuth parseDockerConfig(final Path configPath, final String serverAddress)
      throws IOException {
    checkNotNull(configPath);

    final Map<String, RegistryAuth> configs = authForAllRegistries(configPath).configs();

    if (isNullOrEmpty(serverAddress)) {
      if (configs.isEmpty()) {
        return RegistryAuth.builder().build();
      }
      LOG.warn("Returning first entry from docker config file - use fromConfig(Path) instead, "
               + "this behavior is deprecated and will soon be removed");
      return configs.values().iterator().next();
    }

    if (configs.containsKey(serverAddress)) {
      return configs.get(serverAddress);
    }

    // If the given server address didn't have a protocol try adding a protocol to the address.
    // This handles cases where older versions of Docker included the protocol when writing
    // auth tokens to config.json.
    try {
      final URI serverAddressUri = new URI(serverAddress);
      if (serverAddressUri.getScheme() == null) {
        for (String proto : Arrays.asList("https://", "http://")) {
          final String addrWithProto = proto + serverAddress;
          if (configs.containsKey(addrWithProto)) {
            return configs.get(addrWithProto);
          }
        }
      }
    } catch (URISyntaxException e) {
      // Nothing to do, just let this fall through below
    }

    throw new IllegalArgumentException(
        "serverAddress=" + serverAddress + " does not appear in config file at " + configPath);
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

  /**
   * Obtain auth using a credential helper.
   * @param credsStore The name of the credential helper
   * @param registry The registry for which we need to obtain auth
   * @return A RegistryAuth object with a username, password, and server.
   * @throws IOException This method attempts to execute
   *                     "docker-credential-" + credsStore + " get". If you don't have the
   *                     proper credential helper installed and on your path, this
   *                     will fail.
   */
  private RegistryAuth authWithCredentialHelper(final String credsStore,
                                                final String registry) throws IOException {
    LOG.debug("Executing \"docker-credential-{} get\" for registry \"{}\"", credsStore, registry);
    final Process process = Runtime.getRuntime().exec("docker-credential-" + credsStore + " get");

    try (final Writer outStreamWriter =
          new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
      try (final BufferedWriter writer = new BufferedWriter(outStreamWriter)) {
        writer.write(registry + "\n");
        writer.flush();
      }
    }

    try (final InputStreamReader reader =
          new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)) {
      try (BufferedReader input = new BufferedReader(reader)) {
        String serverAuthDetails = input.readLine();
        // ErrCredentialsNotFound standardizes the not found error, so every helper returns
        // the same message and docker can handle it properly.
        // https://github.com/docker/docker-credential-helpers/blob/19b711cc92fbaa47533646fa8adb457d199c99e1/credentials/error.go#L4-L6
        if ("credentials not found in native keychain".equals(serverAuthDetails)) {
          return null;
        }
        final DockerCredentialHelper dockerCredentialHelper =
            MAPPER.readValue(serverAuthDetails, DockerCredentialHelper.class);
        return dockerCredentialHelper == null ? null : dockerCredentialHelper.toRegistryAuth();
      }
    }
  }
}
