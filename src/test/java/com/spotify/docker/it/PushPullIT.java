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

package com.spotify.docker.it;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.System.getenv;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.notNull;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.spotify.docker.Polling;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.BuildParam;
import com.spotify.docker.client.DockerClient.RemoveContainerParam;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImagePushFailedException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.GoogleContainerRegistryAuthSupplier;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryAuthSupplier;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.ws.rs.NotAuthorizedException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.mockito.internal.matchers.NotNull;

/**
 * These integration tests check we can push images to and pull from a private registry running as a
 * local container. Some tests in this class also check we can push to and pull from Docker Hub.
 * N.B. Docker Hub rate limits pushes, so they might fail if you run them too often :)
 */
@SuppressWarnings("AbbreviationAsWordInName")
public class PushPullIT {

  private static final int LONG_WAIT_SECONDS = 400;
  private static final int SECONDS_TO_WAIT_BEFORE_KILL = 120;

  private static final String REGISTRY_IMAGE = "registry:2";
  private static final String REGISTRY_NAME = "registry";

  private static final String LOCAL_AUTH_USERNAME = "testuser";
  private static final String LOCAL_AUTH_PASSWORD = "testpassword";
  private static final String LOCAL_IMAGE = "localhost:5000/testuser/test-image:latest";

  private static final String LOCAL_AUTH_USERNAME_2 = "testusertwo";
  private static final String LOCAL_AUTH_PASSWORD_2 = "testpasswordtwo";
  private static final String LOCAL_IMAGE_2 = "localhost:5000/testusertwo/test-image:latest";

  // Using a dummy individual's test account because organizations
  // cannot have private repos on Docker Hub.
  private static final String HUB_AUTH_USERNAME = "dxia4";
  private static final String HUB_AUTH_PASSWORD = "03yDT6Yee4iFaggi";
  private static final String HUB_PUBLIC_IMAGE =
      "dxia4/docker-client-test-push-public-image-with-auth";
  private static final String HUB_PRIVATE_IMAGE =
      "dxia4/docker-client-test-push-private-image-with-auth";

  private static final String HUB_AUTH_USERNAME2 = "dxia2";
  private static final String HUB_AUTH_PASSWORD2 = "Tv38KLPd]M";
  private static final String CIRROS_PRIVATE = "dxia/cirros-private";
  private static final String CIRROS_PRIVATE_LATEST = CIRROS_PRIVATE + ":latest";

  private DockerClient client;
  private String registryContainerId;

  @Rule
  public final TestName testName = new TestName();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @BeforeClass
  public static void before() throws Exception {
    // Pull the registry image down once before any test methods in this class run
    DefaultDockerClient.fromEnv().build().pull(REGISTRY_IMAGE);
  }

  @Before
  public void setup() throws Exception {
    final RegistryAuth registryAuth = RegistryAuth.builder()
        .username(LOCAL_AUTH_USERNAME)
        .password(LOCAL_AUTH_PASSWORD)
        .build();
    client = DefaultDockerClient
        .fromEnv()
        .registryAuth(registryAuth)
        .build();

    System.out.printf("- %s\n", testName.getMethodName());
  }

  @After
  @SuppressWarnings("deprecated")
  public void tearDown() throws Exception {
    if (!isNullOrEmpty(registryContainerId)) {
      client.stopContainer(registryContainerId, SECONDS_TO_WAIT_BEFORE_KILL);
      client.removeContainer(registryContainerId, RemoveContainerParam.removeVolumes());
      awaitStopped(client, registryContainerId);
    }
  }

  @Test
  public void testPushImageToPrivateAuthedRegistryWithoutAuth() throws Exception {
    registryContainerId = startAuthedRegistry(client);

    // Make a DockerClient without RegistryAuth
    final DefaultDockerClient client = DefaultDockerClient.fromEnv().build();

    // Push an image to the private registry and check it fails
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), LOCAL_IMAGE);

    exception.expect(ImagePushFailedException.class);
    client.push(LOCAL_IMAGE);
  }

  @Test
  public void testPushImageToPrivateAuthedRegistryWithAuth() throws Exception {
    registryContainerId = startAuthedRegistry(client);

    // Push an image to the private registry and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), LOCAL_IMAGE);
    client.tag(LOCAL_IMAGE, LOCAL_IMAGE_2);
    client.push(LOCAL_IMAGE);

    // Push the same image again under a different user
    final RegistryAuth registryAuth = RegistryAuth.builder()
        .username(LOCAL_AUTH_USERNAME_2)
        .password(LOCAL_AUTH_PASSWORD_2)
        .build();
    client.push(LOCAL_IMAGE_2, registryAuth);

    // We should be able to pull it again
    client.pull(LOCAL_IMAGE);
    client.pull(LOCAL_IMAGE_2);
  }

  @Test
  public void testPushImageToPrivateUnauthedRegistryWithoutAuth() throws Exception {
    registryContainerId = startUnauthedRegistry(client);

    // Make a DockerClient without RegistryAuth
    final DefaultDockerClient client = DefaultDockerClient.fromEnv().build();

    // Push an image to the private registry and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), LOCAL_IMAGE);
    client.push(LOCAL_IMAGE);
    // We should be able to pull it again
    client.pull(LOCAL_IMAGE);
  }

  @Test
  public void testPushImageToPrivateUnauthedRegistryWithAuth() throws Exception {
    registryContainerId = startUnauthedRegistry(client);

    // Push an image to the private registry and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    client.build(Paths.get(dockerDirectory), LOCAL_IMAGE);
    client.push(LOCAL_IMAGE);
    // We should be able to pull it again
    client.pull(LOCAL_IMAGE);
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

  @Test
  public void testPushHubPublicImageWithAuth() throws Exception {
    // Push an image to a public repo on Docker Hub and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    final DockerClient client = DefaultDockerClient
        .fromEnv()
        .registryAuth(RegistryAuth.builder()
                        .username(HUB_AUTH_USERNAME)
                        .password(HUB_AUTH_PASSWORD)
                        .build())
        .build();

    client.build(Paths.get(dockerDirectory), HUB_PUBLIC_IMAGE);
    client.push(HUB_PUBLIC_IMAGE);
  }


  @Test
  public void testPushGCRPrivateImageWithAuth() throws Exception {
    String gcrPrivateImage = getenv("GCR_PRIVATE_IMAGE");
    Assume.assumeTrue("WARNING: Integration test for GCR has not run. "
                      + "Set env variable GCR_PRIVATE_IMAGE "
                      + "(e.g. GCR_PRIVATE_IMAGE=us.gcr.io/my-project/busybox) to run.",
        gcrPrivateImage != null);

    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    RegistryAuthSupplier registryAuthSupplier = new GoogleContainerRegistryAuthSupplier();
    final DockerClient client = DefaultDockerClient
        .fromEnv()
        .registryAuthSupplier(registryAuthSupplier)
        .build();

    client.build(Paths.get(dockerDirectory), gcrPrivateImage);
    client.push(gcrPrivateImage);
    client.pull(gcrPrivateImage);
  }

  @Test
  public void testPushHubPrivateImageWithAuth() throws Exception {
    // Push an image to a private repo on Docker Hub and check it succeeds
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    final DockerClient client = DefaultDockerClient
        .fromEnv()
        .registryAuth(RegistryAuth.builder()
                        .username(HUB_AUTH_USERNAME)
                        .password(HUB_AUTH_PASSWORD)
                        .build())
        .build();

    client.build(Paths.get(dockerDirectory), HUB_PRIVATE_IMAGE);
    client.push(HUB_PRIVATE_IMAGE);
  }

  @Test
  public void testPullHubPrivateRepoWithBadAuth() throws Exception {
    final RegistryAuth badRegistryAuth = RegistryAuth.builder()
        .username(HUB_AUTH_USERNAME2)
        .password("foobar")
        .build();
    exception.expect(DockerException.class);
    exception.expectCause(isA(NotAuthorizedException.class));
    client.pull(CIRROS_PRIVATE_LATEST, badRegistryAuth);
  }

  @Test
  public void testBuildHubPrivateRepoWithAuth() throws Exception {
    final String dockerDirectory = Resources.getResource("dockerDirectoryNeedsAuth").getPath();
    final RegistryAuth registryAuth = RegistryAuth.builder()
        .username(HUB_AUTH_USERNAME2)
        .password(HUB_AUTH_PASSWORD2)
        .build();

    final DefaultDockerClient client = DefaultDockerClient.fromEnv()
        .registryAuth(registryAuth)
        .build();

    client.build(Paths.get(dockerDirectory), "testauth", BuildParam.pullNewerImage());
  }

  @Test
  public void testPullHubPrivateRepoWithAuth() throws Exception {
    final RegistryAuth registryAuth = RegistryAuth.builder()
        .username(HUB_AUTH_USERNAME2)
        .password(HUB_AUTH_PASSWORD2)
        .build();
    client.pull("dxia2/scratch-private:latest", registryAuth);
  }

  private static String startAuthedRegistry(final DockerClient client) throws Exception {
    final Map<String, List<PortBinding>> ports = Collections.singletonMap(
        "5000/tcp", Collections.singletonList(PortBinding.of("0.0.0.0", 5000)));
    final HostConfig hostConfig = HostConfig.builder().portBindings(ports)
        .binds(ImmutableList.of(
            Resources.getResource("dockerRegistry/auth").getPath() + ":/auth",
            Resources.getResource("dockerRegistry/certs").getPath() + ":/certs"
        ))
        /*
         *  Mounting volumes requires special permissions on Docker >= 1.10.
         *  Until a proper Seccomp profile is in place, run container privileged.
         */
        .privileged(true)
        .build();

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(REGISTRY_IMAGE)
        .hostConfig(hostConfig)
        .env(ImmutableList.of(
            "REGISTRY_AUTH=htpasswd",
            "REGISTRY_AUTH_HTPASSWD_REALM=Registry Realm",
            "REGISTRY_AUTH_HTPASSWD_PATH=/auth/htpasswd",
            "REGISTRY_HTTP_TLS_CERTIFICATE=/certs/domain.crt",
            "REGISTRY_HTTP_TLS_KEY=/certs/domain.key",
            "REGISTRY_HTTP_SECRET=super-secret"
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
