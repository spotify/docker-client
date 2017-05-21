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

import com.spotify.docker.client.exceptions.DockerException;
import java.io.IOException;
import java.nio.file.Path;

public class GoogleContainerRegistryAuthSupplier implements RegistryAuthSupplier {

  private final DockerConfigReader dockerCfgReader;
  private final GoogleContainerRegistryCredRefresher googleContainerRegistryCredRefresher;
  private final Path configPath;

  public GoogleContainerRegistryAuthSupplier() {
    this(new DockerConfigReader(),
        new GoogleContainerRegistryCredRefresher(new GCloudProcess()),
        new DockerConfigReader().defaultConfigPath());
  }

  public GoogleContainerRegistryAuthSupplier(DockerConfigReader dockerCfgReader,
                                             GoogleContainerRegistryCredRefresher
                                                 googleContainerRegistryCredRefresher,
                                             Path configPath) {
    this.dockerCfgReader = dockerCfgReader;
    this.googleContainerRegistryCredRefresher = googleContainerRegistryCredRefresher;
    this.configPath = configPath;
  }

  @Override
  public RegistryAuth authFor(String imageName) throws DockerException {
    try {
      String registryName = "https://" + imageName.split("/")[0];
      googleContainerRegistryCredRefresher.refresh();
      return dockerCfgReader.fromComfig(configPath, registryName);
    } catch (IOException ex) {
      throw new DockerException(ex);
    }
  }
}
