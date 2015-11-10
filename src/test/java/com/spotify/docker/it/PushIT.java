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

import com.google.common.io.Resources;

import com.spotify.docker.Polling;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ImagePushFailedException;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.test.CreateContainer;
import com.spotify.docker.test.DockerContainer;
import com.spotify.docker.test.PortBinding;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import java.nio.file.Paths;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;

public class PushIT {

  private static final int LONG_WAIT_SECONDS = 400;

  private static final String REGISTRY_IMAGE = "registry:2";
  private static final String REGISTRY_NAME = "registry";

  private static final String AUTH_EMAIL = "test@example.com";
  private static final String AUTH_USERNAME = "testuser";
  private static final String AUTH_PASSWORD = "testpassword";
  private static final String IMAGE = "localhost:5000/testuser/test-image";

  private static DockerClient client;

  @Rule
  public final TestName testName = new TestName();

  @Rule
  public DockerContainer dockerContainer = new DockerContainer(client);

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void before() throws Exception {
    final AuthConfig authConfig = AuthConfig.builder()
        .email(AUTH_EMAIL)
        .username(AUTH_USERNAME)
        .password(AUTH_PASSWORD)
        .build();
    final DefaultDockerClient.Builder builder = DefaultDockerClient
        .fromEnv()
        .authConfig(authConfig);

    client = builder.build();
  }

  @Before
  public void setup() throws Exception {
    System.out.printf("- %s\n", testName.getMethodName());
  }

  @Test
  @CreateContainer(
      image = REGISTRY_IMAGE,
      name = REGISTRY_NAME,
      portBindings = @PortBinding(hostPort = 5000, containerPort = 5000, protocol = "tcp"),
      binds = {
          "/Users/david/src/docker-client/src/test/resources/dockerRegistry/auth:/auth",
          "/Users/david/src/docker-client/src/test/resources/dockerRegistry/certs:/certs"
      },
      env = {
          "REGISTRY_AUTH=htpasswd",
          "REGISTRY_AUTH_HTPASSWD_REALM=Registry Realm",
          "REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd",
          "REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.crt",
          "REGISTRY_HTTP_TLS_KEY=/certs/domain.key",
      },
      start = true
  )
  public void testPushImageToPrivateAuthedRegistryWithoutAuth() throws Exception {
    // Wait for docker registry to be pulled and started
    awaitRunning(dockerContainer.getContainerId());

    // Make a DockerClient without AuthConfig
    final DefaultDockerClient client = DefaultDockerClient.fromEnv().build();

    // Push an image to the private registry and check it fails
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), IMAGE);
    exception.expect(ImagePushFailedException.class);
    exception.expectMessage("no basic auth credentials");
    client.push(IMAGE);
  }

  @Test
  @CreateContainer(
      image = REGISTRY_IMAGE,
      name = REGISTRY_NAME,
      portBindings = @PortBinding(hostPort = 5000, containerPort = 5000, protocol = "tcp"),
      binds = {
          "/Users/david/src/docker-client/src/test/resources/dockerRegistry/auth:/auth",
          "/Users/david/src/docker-client/src/test/resources/dockerRegistry/certs:/certs"
      },
      env = {
          "REGISTRY_AUTH=htpasswd",
          "REGISTRY_AUTH_HTPASSWD_REALM=Registry Realm",
          "REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd",
          "REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.crt",
          "REGISTRY_HTTP_TLS_KEY=/certs/domain.key",
      },
      start = true
  )
  public void testPushImageToPrivateAuthedRegistryWithAuth() throws Exception {
    // Wait for docker registry to be pulled and started
    awaitRunning(dockerContainer.getContainerId());

    // Push an image to the private registry and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), IMAGE);
    client.push(IMAGE);
    // We should be able to pull it again
    client.pull(IMAGE);
  }

  @Test
  @CreateContainer(
      image = REGISTRY_IMAGE,
      name = REGISTRY_NAME,
      portBindings = @PortBinding(hostPort = 5000, containerPort = 5000, protocol = "tcp"),
      start = true
  )
  public void testPushImageToPrivateUnauthedRegistryWithoutAuth() throws Exception {
    // Wait for docker registry to be pulled and started
    awaitRunning(dockerContainer.getContainerId());

    // Make a DockerClient without AuthConfig
    final DefaultDockerClient client = DefaultDockerClient.fromEnv().build();

    // Push an image to the private registry and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), IMAGE);
    client.push(IMAGE);
    // We should be able to pull it again
    client.pull(IMAGE);
  }

  @Test
  @CreateContainer(
      image = REGISTRY_IMAGE,
      name = REGISTRY_NAME,
      portBindings = @PortBinding(hostPort = 5000, containerPort = 5000, protocol = "tcp"),
      start = true
  )
  public void testPushImageToPrivateUnauthedRegistryWithAuth() throws Exception {
    // Wait for docker registry to be pulled and started
    awaitRunning(dockerContainer.getContainerId());

    // Push an image to the private registry and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), IMAGE);
    client.push(IMAGE);
    // We should be able to pull it again
    client.pull(IMAGE);
  }

  private static void awaitRunning(final String containerId) throws Exception {
    Polling.await(LONG_WAIT_SECONDS, SECONDS, new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        final ContainerInfo containerInfo = client.inspectContainer(containerId);
        return containerInfo.state().running() ? true : null;
      }
    });
  }
}
