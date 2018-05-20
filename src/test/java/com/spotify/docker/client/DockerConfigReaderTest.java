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
import static org.junit.Assume.assumeTrue;

import com.google.common.io.Resources;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
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

  @Test
  public void testFromDockerConfig_AddressProtocol() throws IOException {
    final Path path = getTestFilePath("dockerConfig/protocolMissing.json");

    // Server address matches exactly what's in the config file
    final RegistryAuth noProto = reader.fromConfig(path, "docker.example.com");
    assertThat(noProto.serverAddress(), equalTo("docker.example.com"));

    // Server address doesn't have a protocol but the entry in the config file does (https)
    final RegistryAuth httpsProto = reader.fromConfig(path, "repo.example.com");
    assertThat(httpsProto.serverAddress(), equalTo("https://repo.example.com"));

    // Server address doesn't have a protocol but the entry in the config file does (http)
    final RegistryAuth httpProto = reader.fromConfig(path, "local.example.com");
    assertThat(httpProto.serverAddress(), equalTo("http://local.example.com"));
  }

  @Test
  public void testFromDockerConfig_CredsStore() throws Exception {
    assumeTrue("Need to have a credential store.", getAuthCredentialsExist());

    String domain1 = "https://test.fakedomain.com";
    String domain2 = "https://test.fakedomain2.com";

    String testAuth1 = "{\n" + "\t\"ServerURL\": \"" + domain1 + "\",\n"
                       + "\t\"Username\": \"david\",\n" + "\t\"Secret\": \"passw0rd1\"\n" + "}";
    String testAuth2 = "{\n" + "\t\"ServerURL\": \"" + domain2 + "\",\n"
                       + "\t\"Username\": \"carl\",\n" + "\t\"Secret\": \"myPassword\"\n" + "}";

    storeAuthCredential(testAuth1);
    storeAuthCredential(testAuth2);

    final Path path = getTestFilePath("dockerConfig/" + getCredsStoreFileName());
    final RegistryConfigs configs = reader.fromConfig(path);

    for (RegistryAuth authConfigs : configs.configs().values()) {
      if (domain1.equals(authConfigs.serverAddress())) {
        assertThat(authConfigs.username(), equalTo("david"));
        assertThat(authConfigs.password(), equalTo("passw0rd1"));
      } else if (domain2.equals(authConfigs.serverAddress())) {
        assertThat(authConfigs.username(), equalTo("carl"));
        assertThat(authConfigs.password(), equalTo("myPassword"));
      }
    }
    eraseAuthCredential(domain1);
    eraseAuthCredential(domain2);
  }

  private void eraseAuthCredential(String domain1) throws IOException, InterruptedException {
    // Erase the credentials from the store
    Process process = Runtime.getRuntime().exec(getCredsStore() + " erase");
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

    writer.write(domain1 + "\n");
    writer.flush();
    writer.close();

    process.waitFor();
  }

  private boolean getAuthCredentialsExist() throws InterruptedException {
    boolean returnValue = false;
    try {
      Process process = Runtime.getRuntime().exec(getCredsStore() + " list");
      returnValue = process.waitFor() == 0;
    } catch (IOException e) {
      // Ignored. This is ok, it just means the cred store doesn't exist on this system.
    }
    return returnValue;
  }

  private void storeAuthCredential(String testAuth1) throws IOException, InterruptedException {
    Process process = Runtime.getRuntime().exec(getCredsStore() + " store");
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

    writer.write(testAuth1 + "\n");
    writer.flush();
    writer.close();

    process.waitFor();
  }

  private static String getCredsStoreFileName() {
    if (OsUtils.isOsX()) {
      return "credsStoreConfigOSX.json";
    } else if (OsUtils.isLinux()) {
      return "credsStoreConfigLinux.json";
    } else {
      return "credsStoreConfigWin.json";
    }
  }

  private static String getCredsStore() {
    String credsStore;
    if (OsUtils.isOsX()) {
      credsStore = "docker-credential-osxkeychain";
    } else if (OsUtils.isLinux()) {
      credsStore =  "docker-credential-secretservice";
    } else {
      credsStore = "docker-credential-wincred";
    }
    return credsStore;
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

  @Test
  public void testParseNoAuths() throws Exception {
    final Path path = getTestFilePath("dockerConfig/noAuths.json");
    final RegistryConfigs configs = reader.fromConfig(path);
    assertThat(configs, equalTo(RegistryConfigs.empty()));
  }
}
