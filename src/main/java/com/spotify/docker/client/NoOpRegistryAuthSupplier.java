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

package com.spotify.docker.client;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryAuthSupplier;
import java.rmi.registry.Registry;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Wraps a RegistryAuth with the RegisrtyAuthSupplier interface.
 */
public class NoOpRegistryAuthSupplier
    implements RegistryAuthSupplier {

  private final RegistryAuth registryAuth;

  public NoOpRegistryAuthSupplier(RegistryAuth registryAuth) {
    this.registryAuth = registryAuth;
  }

  public NoOpRegistryAuthSupplier() {
    registryAuth = null;

  }

  @Override
  public RegistryAuth authFor(String imageName) throws DockerException {
    return registryAuth;
  }

  @Override
  public RegistryAuth authForSwarm() {
    return registryAuth;
  }

}
