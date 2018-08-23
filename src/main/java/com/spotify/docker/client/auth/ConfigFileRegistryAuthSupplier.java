/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 - 2017 Spotify AB
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

package com.spotify.docker.client.auth;

import com.spotify.docker.client.DockerConfigReader;
import com.spotify.docker.client.ImageRef;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RegistryAuthSupplier that returns data from the docker-cli config file. The config file is
 * re-read on each method call, to handle cases where a process is updating the file out-of-band
 * during the lifecycle of a DockerClient instance.
 */
public class ConfigFileRegistryAuthSupplier implements RegistryAuthSupplier {

  private static final Logger log = LoggerFactory.getLogger(ConfigFileRegistryAuthSupplier.class);

  private final DockerConfigReader reader;
  private final Path path;

  public ConfigFileRegistryAuthSupplier() {
    this(new DockerConfigReader());
  }

  public ConfigFileRegistryAuthSupplier(final DockerConfigReader reader) {
    this(reader, reader.defaultConfigPath());
  }

  public ConfigFileRegistryAuthSupplier(final DockerConfigReader reader, final Path path) {
    this.reader = reader;
    this.path = path;
  }

  private boolean configFileExists() {
    final File f = this.path.toFile();
    return f.isFile() && f.canRead();
  }

  @Override
  public RegistryAuth authFor(final String imageName) throws DockerException {
    if (!configFileExists()) {
      return null;
    }

    final ImageRef ref = new ImageRef(imageName);
    try {
      // Some registries like Docker Hub and GCR include "https://" in the server address.
      // Others like quay.io don't.
      final RegistryAuth registryAuth = reader.authForRegistry(path, ref.getRegistryUrl());
      if (registryAuth != null) {
        return registryAuth;
      }
      return reader.authForRegistry(path, ref.getRegistryName());
    } catch (IllegalArgumentException e) {
      log.debug("Failed first attempt to find auth for {}", ref.getRegistryUrl(), e);
      try {
        return reader.authForRegistry(path, ref.getRegistryName());
      } catch (IllegalArgumentException e2) {
        log.debug("Failed second attempt to find auth for {}", ref.getRegistryName(), e2);
        return null;
      } catch (IOException e2) {
        throw new DockerException(e2);
      }
    } catch (IOException e) {
      throw new DockerException(e);
    }
  }

  @Override
  public RegistryAuth authForSwarm() throws DockerException {
    return null;
  }

  @Override
  public RegistryConfigs authForBuild() throws DockerException {
    if (!configFileExists()) {
      return null;
    }

    try {
      return reader.authForAllRegistries(path);
    } catch (IOException e) {
      throw new DockerException(e);
    }
  }
}
