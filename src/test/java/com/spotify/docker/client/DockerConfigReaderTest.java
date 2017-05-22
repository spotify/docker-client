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

/*
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.docker.client;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

import com.google.common.io.Resources;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("deprecated")
public class DockerConfigReaderTest {

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
      .email("dockerman@hub.com")
      .serverAddress("docker.customdomain.com")
      .identityToken("52ce5fd5-eb60-42bf-931f-5eeec128211a")
      .build();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private final DockerConfigReader reader = new DockerConfigReader();

  @Test
  public void testFromDockerConfig_FullConfig() throws Exception {
    final RegistryAuth registryAuth =
        reader.fromFirstConfig(getTestFilePath("dockerConfig/fullConfig.json"));
    assertThat(registryAuth, equalTo(DOCKER_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_FullDockerCfg() throws Exception {
    final RegistryAuth registryAuth =
        reader.fromFirstConfig(getTestFilePath("dockerConfig/fullDockerCfg"));
    assertThat(registryAuth, equalTo(DOCKER_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_IdentityToken() throws Exception {
    final RegistryAuth authConfig =
        reader.fromFirstConfig(getTestFilePath("dockerConfig/identityTokenConfig.json"));
    assertThat(authConfig, equalTo(IDENTITY_TOKEN_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_IncompleteConfig() throws Exception {
    final RegistryAuth registryAuth =
        reader.fromFirstConfig(getTestFilePath("dockerConfig/incompleteConfig.json"));

    final RegistryAuth expected = RegistryAuth.builder()
        .email("dockerman@hub.com")
        .serverAddress("https://different.docker.io/v1/")
        .build();

    assertThat(registryAuth, is(expected));
  }

  @Test
  public void testFromDockerConfig_WrongConfigs() throws Exception {
    final RegistryAuth registryAuth1 =
        reader.fromFirstConfig(getTestFilePath("dockerConfig/wrongConfig1.json"));
    assertThat(registryAuth1, is(emptyRegistryAuth()));

    final RegistryAuth registryAuth2 =
        reader.fromFirstConfig(getTestFilePath("dockerConfig/wrongConfig2.json"));
    assertThat(registryAuth2, is(emptyRegistryAuth()));
  }

  private static Matcher<RegistryAuth> emptyRegistryAuth() {
    return new CustomTypeSafeMatcher<RegistryAuth>("an empty RegistryAuth") {
      @Override
      protected boolean matchesSafely(final RegistryAuth item) {
        return item.email() == null
               && item.identityToken() == null
               && item.password() == null
               && item.email() == null;
      }
    };
  }

  @Test
  public void testFromDockerConfig_MissingConfigFile() throws Exception {
    final Path randomPath = Paths.get(RandomStringUtils.randomAlphanumeric(16) + ".json");
    expectedException.expect(FileNotFoundException.class);
    reader.fromFirstConfig(randomPath);
  }

  @Test
  public void testFromDockerConfig_MultiConfig() throws Exception {
    final Path path = getTestFilePath("dockerConfig/multiConfig.json");

    final RegistryAuth myDockParsed = reader.fromConfig(path, "https://narnia.mydock.io/v1/");
    assertThat(myDockParsed, equalTo(MY_AUTH_CONFIG));

    final RegistryAuth dockerIoParsed = reader.fromConfig(path, "https://index.docker.io/v1/");
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
    final URL resource = DockerConfigReaderTest.class.getResource("/" + path);
    return Paths.get(resource.getPath().substring(1));
  }

  private static Path getLinuxPath(final String path) {
    return Paths.get(Resources.getResource(path).getPath());
  }

  @Test
  public void testParseRegistryConfigs() throws Exception {
    final Path path = getTestFilePath("dockerConfig/multiConfig.json");
    final RegistryConfigs configs = reader.fromConfig(path);

    assertThat(configs.configs(), allOf(
        hasEntry("https://index.docker.io/v1/", DOCKER_AUTH_CONFIG),
        hasEntry("https://narnia.mydock.io/v1/", MY_AUTH_CONFIG)
    ));
  }
}
