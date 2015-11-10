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

package com.spotify.docker.it;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import com.spotify.docker.Polling;
import com.spotify.docker.client.ContainerNotFoundException;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ImagePushFailedException;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

public class PushIT {

  private static final int LONG_WAIT_SECONDS = 400;
  private static final int SECONDS_TO_WAIT_BEFORE_KILL = 120;

  private static final String REGISTRY_IMAGE = "registry:2";
  private static final String REGISTRY_NAME = "registry";

  private static final String AUTH_EMAIL = "test@example.com";
  private static final String AUTH_USERNAME = "testuser";
  private static final String AUTH_PASSWORD = "testpassword";
  private static final String IMAGE = "localhost:5000/testuser/test-image";

  private DockerClient client;

  @Rule
  public final TestName testName = new TestName();

  @BeforeClass
  public static void before() throws Exception {
    // Pull the registry image down once before the any test methods in this class run
    DefaultDockerClient.fromEnv().build().pull(REGISTRY_IMAGE);
  }

  @Before
  public void setup() throws Exception {
    final AuthConfig authConfig = AuthConfig.builder()
        .email(AUTH_EMAIL)
        .username(AUTH_USERNAME)
        .password(AUTH_PASSWORD)
        .build();
    client = DefaultDockerClient
        .fromEnv()
        .authConfig(authConfig)
        .build();

    System.out.printf("- %s\n", testName.getMethodName());
  }

  @Test
  public void testPushImageToPrivateAuthedRegistryWithoutAuth() throws Exception {
    final String containerId = startAuthedRegistry(client);

    // Make a DockerClient without AuthConfig
    final DefaultDockerClient client = DefaultDockerClient.fromEnv().build();

    // Push an image to the private registry and check it fails
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), IMAGE);

    // A bit ugly, but we can't use an @Rule Exception here because we
    // want to do customized cleanup after the exception is thrown.
    // We can't just do @Test(expected = ...) because we want to check the exception message.
    boolean correctExceptionThrown = false;
    try {
      client.push(IMAGE);
    } catch (ImagePushFailedException e) {
      if (e.getMessage().contains("no basic auth credentials")) {
        correctExceptionThrown = true;
      }
    }
    assertTrue(correctExceptionThrown);

    stopAndRemoveContainer(client, containerId);
  }

  @Test
  public void testPushImageToPrivateAuthedRegistryWithAuth() throws Exception {
    final String containerId = startAuthedRegistry(client);

    // Push an image to the private registry and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), IMAGE);
    client.push(IMAGE);
    // We should be able to pull it again
    client.pull(IMAGE);

    stopAndRemoveContainer(client, containerId);
  }

  @Test
  public void testPushImageToPrivateUnauthedRegistryWithoutAuth() throws Exception {
    final String containerId = startUnauthedRegistry(client);

    // Make a DockerClient without AuthConfig
    final DefaultDockerClient client = DefaultDockerClient.fromEnv().build();

    // Push an image to the private registry and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), IMAGE);
    client.push(IMAGE);
    // We should be able to pull it again
    client.pull(IMAGE);

    stopAndRemoveContainer(client, containerId);
  }

  @Test
  public void testPushImageToPrivateUnauthedRegistryWithAuth() throws Exception {
    final String containerId = startUnauthedRegistry(client);

    // Push an image to the private registry and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), IMAGE);
    client.push(IMAGE);
    // We should be able to pull it again
    client.pull(IMAGE);

    stopAndRemoveContainer(client, containerId);
  }

  private static String startUnauthedRegistry(final DockerClient client) throws Exception {
    final Map<String, List<PortBinding>> ports = Collections.singletonMap(
        "5000/tcp", Collections.singletonList(PortBinding.of("0.0.0.0", 5000)));
    final HostConfig hostConfig = HostConfig.builder().portBindings(ports)
        .build();

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(REGISTRY_IMAGE)
        .hostConfig(hostConfig)
        .build();

    return startAndAwaitContainer(client, containerConfig, REGISTRY_NAME);
  }

  private static String startAuthedRegistry(final DockerClient client) throws Exception {
    final Map<String, List<PortBinding>> ports = Collections.singletonMap(
        "5000/tcp", Collections.singletonList(PortBinding.of("0.0.0.0", 5000)));
    final HostConfig hostConfig = HostConfig.builder().portBindings(ports)
        .binds(ImmutableList.of(
            Resources.getResource("dockerRegistry/auth").getPath() + ":/auth",
            Resources.getResource("dockerRegistry/certs").getPath() + ":/certs"
        ))
        .build();

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(REGISTRY_IMAGE)
        .hostConfig(hostConfig)
        .env(ImmutableList.of(
            "REGISTRY_AUTH=htpasswd",
            "REGISTRY_AUTH_HTPASSWD_REALM=Registry Realm",
            "REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd",
            "REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.crt",
            "REGISTRY_HTTP_TLS_KEY=/certs/domain.key"
        ))
        .build();

    return startAndAwaitContainer(client, containerConfig, REGISTRY_NAME);
  }

  private static String startAndAwaitContainer(final DockerClient client,
                                               final ContainerConfig containerConfig,
                                               final String containerName)
      throws Exception {
    final ContainerCreation creation = client.createContainer(containerConfig, containerName);
    final String containerId = creation.id();
    client.startContainer(containerId);
    awaitRunning(client, containerId);
    return containerId;
  }

  private static void awaitRunning(final DockerClient client, final String containerId)
      throws Exception {
    Polling.await(LONG_WAIT_SECONDS, SECONDS, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        final ContainerInfo containerInfo = client.inspectContainer(containerId);
        return containerInfo.state().running() ? true : null;
      }
    });
  }

  private static void stopAndRemoveContainer(final DockerClient client,
                                             final String containerId)
      throws Exception {
    client.stopContainer(containerId, SECONDS_TO_WAIT_BEFORE_KILL);
    client.removeContainer(containerId, true);
    awaitStopped(client, containerId);
  }

  private static void awaitStopped(final DockerClient client,
                                   final String containerId)
      throws Exception {
    Polling.await(LONG_WAIT_SECONDS, SECONDS, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        boolean containerRemoved = false;
        try {
          client.inspectContainer(containerId);
        } catch (ContainerNotFoundException e) {
          containerRemoved = true;
        }

        return containerRemoved ? true : null;
      }
    });
  }
}
