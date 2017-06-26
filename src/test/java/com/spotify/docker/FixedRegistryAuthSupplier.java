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

package com.spotify.docker;

import static java.util.Collections.singletonMap;

import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

/**
 * Returns a fixed {@link RegistryAuth} and {@link RegistryConfigs} based on a single set of
 * username, password, and registry name.
 */
public class FixedRegistryAuthSupplier implements RegistryAuthSupplier {

  private final RegistryAuth registryAuth;
  private final RegistryConfigs registryConfigs;

  public FixedRegistryAuthSupplier(final String username,
                                   final String password,
                                   final String registryName) {
    this.registryAuth = RegistryAuth.builder()
        .username(username)
        .password(password)
        .build();
    this.registryConfigs = RegistryConfigs.create(singletonMap(registryName, registryAuth));
  }

  @Override
  public RegistryAuth authFor(final String imageName) throws DockerException {
    return registryAuth;
  }

  @Override
  public RegistryAuth authForSwarm() throws DockerException {
    return registryAuth;
  }

  @Override
  public RegistryConfigs authForBuild() throws DockerException {
    return registryConfigs;
  }
}

