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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.google.common.io.Resources;
import com.spotify.docker.client.OsUtils;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class RegistryAuthTest {

  private static final RegistryAuth DOCKER_AUTH_CONFIG = RegistryAuth.builder()
      .serverAddress("https://index.docker.io/v1/")
      .username("dockerman")
      .password("sw4gy0lo")
      .email("dockerman@hub.com")
      .build();

  private static final RegistryAuth MY_AUTH_CONFIG = RegistryAuth.builder()
      .serverAddress("https://narnia.mydock.io/v1/")
      .username("megaman")
      .password("riffraff")
      .email("megaman@mydock.com")
      .build();

  private static final RegistryAuth IDENTITY_TOKEN_AUTH_CONFIG = RegistryAuth.builder()
      .serverAddress("docker.customdomain.com")
      .identityToken("52ce5fd5-eb60-42bf-931f-5eeec128211a")
      .build();

  private static final RegistryAuth EMPTY_AUTH_CONFIG = RegistryAuth.builder().build();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testFromDockerConfig_FullConfig() throws Exception {
    final RegistryAuth registryAuth = RegistryAuth.fromDockerConfig(getTestFilePath(
        "dockerConfig/fullConfig.json")).build();
    assertThat(registryAuth, equalTo(DOCKER_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_FullDockerCfg() throws Exception {
    final RegistryAuth registryAuth = RegistryAuth.fromDockerConfig(getTestFilePath(
        "dockerConfig/fullDockerCfg")).build();
    assertThat(registryAuth, equalTo(DOCKER_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_IdentityToken() throws Exception {
    final RegistryAuth authConfig = RegistryAuth.fromDockerConfig(getTestFilePath(
            "dockerConfig/identityTokenConfig.json")).build();
    assertThat(authConfig, equalTo(IDENTITY_TOKEN_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_IncompleteConfig() throws Exception {
    final RegistryAuth registryAuth = RegistryAuth.fromDockerConfig(getTestFilePath(
        "dockerConfig/incompleteConfig.json")).build();
    assertThat(registryAuth, equalTo(EMPTY_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_WrongConfigs() throws Exception {
    final RegistryAuth registryAuth1 = RegistryAuth.fromDockerConfig(getTestFilePath(
        "dockerConfig/wrongConfig1.json")).build();
    assertThat(registryAuth1, equalTo(EMPTY_AUTH_CONFIG));

    final RegistryAuth registryAuth2 = RegistryAuth.fromDockerConfig(getTestFilePath(
        "dockerConfig/wrongConfig2.json")).build();
    assertThat(registryAuth2, equalTo(EMPTY_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_MissingConfigFile() throws Exception {
    final Path randomPath = Paths.get(RandomStringUtils.randomAlphanumeric(16) + ".json");
    expectedException.expect(FileNotFoundException.class);
    RegistryAuth.fromDockerConfig(randomPath).build();
  }

  @Test
  public void testFromDockerConfig_MultiConfig() throws Exception {
    final RegistryAuth myDockParsed = RegistryAuth.fromDockerConfig(getTestFilePath(
        "dockerConfig/multiConfig.json"), "https://narnia.mydock.io/v1/").build();
    assertThat(myDockParsed, equalTo(MY_AUTH_CONFIG));
    final RegistryAuth dockerIoParsed = RegistryAuth.fromDockerConfig(getTestFilePath(
        "dockerConfig/multiConfig.json"), "https://index.docker.io/v1/").build();
    assertThat(dockerIoParsed, equalTo(DOCKER_AUTH_CONFIG));
  }

  private static Path getTestFilePath(final String path) {
    if (OsUtils.isLinux() || OsUtils.isOsX()) {
      return getLinuxPath(path);
    } else {
      return getWindowsPath(path);
    }
  }

  private static Path getWindowsPath(final String path) {
    final URL resource = RegistryAuthTest.class.getResource("/" + path);
    return Paths.get(resource.getPath().substring(1));
  }

  private static Path getLinuxPath(final String path) {
    return Paths.get(Resources.getResource(path).getPath());
  }
}
