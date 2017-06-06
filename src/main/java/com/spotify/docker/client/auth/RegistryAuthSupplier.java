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

package com.spotify.docker.client.auth;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

public interface RegistryAuthSupplier {

  /**
   * Returns a RegistryAuth object that works with a given registry's API [e.g. GCR].
   *
   * @return the RegistryAuth to use when working with the image, or else {@code null} if no
   *         authentication info applies for this image
   */
  RegistryAuth authFor(String imageName) throws DockerException;

  /**
   * Returns a RegistryAuth object that is valid for a Docker Swarm context [i.e. not tied
   * to specific image]. It's unnecessary if it's not planned to use this AuthSupplier to pull
   * images for Swarm.
   *
   * @return the RegistryAuth to use in Swarn, or else {@code null} for no authentication info
   */
  RegistryAuth authForSwarm() throws DockerException;

  /** Authentication info to pass in the X-Registry-Config header when building an image. */
  RegistryConfigs authForBuild() throws DockerException;
}
