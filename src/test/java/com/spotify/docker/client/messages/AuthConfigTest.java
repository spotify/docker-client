/*
 * Copyright (c) 2014 Spotify AB.
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

package com.spotify.docker.client.messages;

import com.google.common.io.Resources;

import com.spotify.docker.client.OSUtils;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class AuthConfigTest {

  private static final AuthConfig DOCKER_AUTH_CONFIG = AuthConfig.builder()
      .serverAddress("https://index.docker.io/v1/")
      .username("dockerman")
      .password("sw4gy0lo")
      .email("dockerman@hub.com")
      .build();
  private static final AuthConfig MY_AUTH_CONFIG = AuthConfig.builder()
      .serverAddress("https://narnia.mydock.io/v1/")
      .username("megaman")
      .password("riffraff")
      .email("megaman@mydock.com")
      .build();
  private static final AuthConfig EMPTY_AUTH_CONFIG = AuthConfig.builder().build();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void verifyBuilder() throws Exception {
    final AuthConfig.Builder builder = AuthConfig.builder();

    // Input to setXXX
    final String username = "username";
    final String password = "password";
    final String email = "email";
    final String serverAddress = "serverAddress";

    // Check setXXX methods
    builder.username(username);
    builder.password(password);
    builder.email(email);
    builder.serverAddress(serverAddress);
    assertEquals("username", username, builder.username());
    assertEquals("password", password, builder.password());
    assertEquals("email", email, builder.email());
    assertEquals("serverAddress", serverAddress, builder.serverAddress());

    // Check final output
    final AuthConfig authConfig = builder.build();
    assertEquals("username", username, authConfig.username());
    assertEquals("password", password, authConfig.password());
    assertEquals("email", email, authConfig.email());
    assertEquals("serverAddress", serverAddress, authConfig.serverAddress());

    // Check toBuilder
    final AuthConfig.Builder rebuilder = authConfig.toBuilder();
    assertEquals("username", username, rebuilder.username());
    assertEquals("password", password, rebuilder.password());
    assertEquals("email", email, rebuilder.email());
    assertEquals("serverAddress", serverAddress, rebuilder.serverAddress());
  }

  @Test
  public void testDefaultServerAddress() throws Exception {
    final AuthConfig.Builder builder = AuthConfig.builder();
    assertThat(builder.serverAddress(), equalTo("https://index.docker.io/v1/"));
  }

  @Test
  public void testFromDockerConfig_FullConfig() throws Exception {
    final AuthConfig authConfig = AuthConfig.fromDockerConfig(getTestFilePath(
        "dockerConfig/fullConfig.json")).build();
    assertThat(authConfig, equalTo(DOCKER_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_FullDockerCfg() throws Exception {
    final AuthConfig authConfig = AuthConfig.fromDockerConfig(getTestFilePath(
        "dockerConfig/fullDockerCfg")).build();
    assertThat(authConfig, equalTo(DOCKER_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_IncompleteConfig() throws Exception {
    final AuthConfig authConfig = AuthConfig.fromDockerConfig(getTestFilePath(
        "dockerConfig/incompleteConfig.json")).build();
    assertThat(authConfig, equalTo(EMPTY_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_WrongConfigs() throws Exception {
    final AuthConfig authConfig1 = AuthConfig.fromDockerConfig(getTestFilePath(
        "dockerConfig/wrongConfig1.json")).build();
    assertThat(authConfig1, equalTo(EMPTY_AUTH_CONFIG));

    final AuthConfig authConfig2 = AuthConfig.fromDockerConfig(getTestFilePath(
        "dockerConfig/wrongConfig2.json")).build();
    assertThat(authConfig2, equalTo(EMPTY_AUTH_CONFIG));
  }

  @Test
  public void testFromDockerConfig_MissingConfigFile() throws Exception {
    final Path randomPath = Paths.get(RandomStringUtils.randomAlphanumeric(16) + ".json");
    expectedException.expect(FileNotFoundException.class);
    AuthConfig.fromDockerConfig(randomPath).build();
  }

  @Test
  public void testFromDockerConfig_MultiConfig() throws Exception {
    final AuthConfig myDockParsed = AuthConfig.fromDockerConfig(getTestFilePath(
        "dockerConfig/multiConfig.json"), "https://narnia.mydock.io/v1/").build();
    assertThat(myDockParsed, equalTo(MY_AUTH_CONFIG));
    final AuthConfig dockerIoParsed = AuthConfig.fromDockerConfig(getTestFilePath(
        "dockerConfig/multiConfig.json"), "https://index.docker.io/v1/").build();
    assertThat(dockerIoParsed, equalTo(DOCKER_AUTH_CONFIG));
  }

  private static Path getTestFilePath(final String path) {
    if (OSUtils.isLinux()) {
      return getLinuxPath(path);
    } else {
      return getWindowsPath(path);
    }
  }

  private static Path getWindowsPath(final String path) {
    final URL resource = AuthConfigTest.class.getResource("/" + path);
    return Paths.get(resource.getPath().substring(1));
  }

  private static Path getLinuxPath(final String path) {
    return Paths.get(Resources.getResource(path).getPath());
  }
}
