/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2017 Spotify AB
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

package com.spotify.docker.client.gcr;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.spotify.docker.client.DockerConfigReader;
import com.spotify.docker.client.messages.RegistryAuth;
import java.nio.file.Path;
import org.junit.Test;
import org.mockito.InOrder;

public class GoogleContainerRegistryAuthSupplierTest {

  @Test
  public void authForRefreshesCredsBeforeReadingConfigFile() throws Exception {
    DockerConfigReader dockerCfgReader = mock(DockerConfigReader.class);
    GoogleContainerRegistryCredRefresher googleContainerRegistryCredRefresher =
        mock(GoogleContainerRegistryCredRefresher.class);
    Path path = mock(Path.class);
    InOrder inOrder = inOrder(googleContainerRegistryCredRefresher, dockerCfgReader);

    GoogleContainerRegistryAuthSupplier googleContainerRegistryAuthSupplier =
        new GoogleContainerRegistryAuthSupplier(
            dockerCfgReader, googleContainerRegistryCredRefresher, path);

    googleContainerRegistryAuthSupplier.authFor("us.gcr.io/awesome-project/example-image");
    inOrder.verify(googleContainerRegistryCredRefresher).refresh();
    inOrder.verify(dockerCfgReader).fromConfig(any(Path.class), anyString());
  }

  @Test
  public void authForReturnsRegistryAuthThatMatchesRegistryName() throws Exception {
    DockerConfigReader dockerCfgReader = mock(DockerConfigReader.class);
    GoogleContainerRegistryCredRefresher googleContainerRegistryCredRefresher =
        mock(GoogleContainerRegistryCredRefresher.class);
    Path path = mock(Path.class);

    RegistryAuth expected =
        RegistryAuth.builder().email("no@no.com").identityToken("authorific").build();

    when(dockerCfgReader.fromConfig(any(Path.class), eq("https://us.gcr.io"))).thenReturn(expected);

    GoogleContainerRegistryAuthSupplier googleContainerRegistryAuthSupplier =
        new GoogleContainerRegistryAuthSupplier(
            dockerCfgReader, googleContainerRegistryCredRefresher, path);

    RegistryAuth registryAuth = googleContainerRegistryAuthSupplier
        .authFor("us.gcr.io/awesome-project/example-image");

    assertEquals(expected.email(), registryAuth.email());
    assertEquals(expected.identityToken(), registryAuth.identityToken());

  }

}
