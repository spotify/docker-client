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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableCollection;
import com.spotify.docker.client.messages.DockerCredentialHelperAuth;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
   * @deprecated In favor of {@link #authForRegistry(Path, String)}
   */
  @Deprecated
  public RegistryAuth fromConfig(final Path configPath, final String serverAddress)
      throws IOException {
    return authForRegistry(configPath, serverAddress);
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

    final Map<String, String> credHelpers = config.credHelpers();
    final boolean hasCredHelpers = credHelpers != null && !credHelpers.isEmpty();
    final Map<String, RegistryAuth> auths = config.auths();
    final boolean hasAuths = auths != null && !auths.isEmpty();
    final String credsStore = config.credsStore();
    final boolean hasCredsStore = credsStore != null;

    // First use the credHelpers, if there are any
    if (hasCredHelpers) {
      for (final Map.Entry<String, String> credHelpersEntry : credHelpers.entrySet()) {
        final String registry = credHelpersEntry.getKey();
        final String aCredsStore = credHelpersEntry.getValue();
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

      for (final Map.Entry<String, RegistryAuth> authEntry : auths.entrySet()) {
        final String registry = authEntry.getKey();
        final RegistryAuth registryAuth = authEntry.getValue();
        if (registryAuth == null || registryAuth.equals(empty)) {
          // We have an empty object. Can we use credsStore?
          if (hasCredsStore) {
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

    // If there are no credHelpers or auths or credsStore, then the
    // config may be in a very old format. There aren't any keys for different
    // sections. The file is just a map of registries to auths.
    // In other words, it looks like a RegistryConfigs.
    // If we can map it to one, we'll return it.
    if (!(hasAuths || hasCredHelpers || hasCredsStore)) {
      try {
        return MAPPER.readValue(configPath.toFile(), RegistryConfigs.class);
      } catch (IOException ignored) {
        // Looks like that failed to parse.
        // Eat the exception, fall through, and return empty object.
      }
    }

    return registryConfigsBuilder.build();
  }

  /**
   * Generate {@link RegistryAuth} for the registry.
   *
   * @param configPath Path to the docker config file
   * @param registry Docker registry for which to generate auth
   * @return The generated authentication object
   */
  public RegistryAuth authForRegistry(final Path configPath, final String registry)
      throws IOException {
    checkNotNull(configPath);
    checkNotNull(registry);

    final DockerConfig config = MAPPER.readValue(configPath.toFile(), DockerConfig.class);
    if (config == null) {
      return RegistryAuth.builder().build();
    }

    final RegistryAuth registryAuth = authForRegistry(config, registry);
    if (registryAuth != null) {
      return registryAuth;
    }
    // If the given server address didn't have a protocol try adding a protocol to the address.
    // This handles cases where older versions of Docker included the protocol when writing
    // auth tokens to config.json.
    try {
      final URI serverAddressUri = new URI(registry);
      if (serverAddressUri.getScheme() == null) {
        for (String proto : Arrays.asList("https://", "http://")) {
          final RegistryAuth protoRegistryAuth = authForRegistry(config, proto + registry);
          if (protoRegistryAuth != null) {
            return protoRegistryAuth;
          }
        }
      }
    } catch (URISyntaxException e) {
      // Nothing to do, just let this fall through below
    }

    throw new IllegalArgumentException(
        "registry \"" + registry + "\" does not appear in config file at " + configPath);
  }

  private RegistryAuth authForRegistry(final DockerConfig config, final String registry)
      throws IOException {

    // If the registry shows up in "auths", return it
    final Map<String, RegistryAuth> auths = config.auths();
    if (auths != null && auths.get(registry) != null) {
      return auths.get(registry).toBuilder().serverAddress(registry).build();
    }

    // Else, we use a credential helper.
    final String credsStore = getCredentialStore(config, registry);
    if (credsStore != null) {
      return authWithCredentialHelper(credsStore, registry);
    }

    return null;
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
    final DockerCredentialHelperAuth dockerCredentialHelperAuth =
        DockerCredentialHelper.get(credsStore, registry);
    return dockerCredentialHelperAuth == null ? null : dockerCredentialHelperAuth.toRegistryAuth();
  }

  private String getCredentialStore(final DockerConfig config, final String registry) {
    checkNotNull(config, "Docker config cannot be null");
    checkNotNull(registry, "registry cannot be null");

    // Check for the registry in the credHelpers map first.
    // If it isn't there, default to credsStore.
    final Map<String, String> credHelpers = config.credHelpers();
    return (credHelpers != null && credHelpers.containsKey(registry))
        ? credHelpers.get(registry)
        : config.credsStore();
  }
}
