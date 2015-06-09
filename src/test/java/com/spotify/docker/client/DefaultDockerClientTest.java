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

package com.spotify.docker.client;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.SettableFuture;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.spotify.docker.client.DockerClient.AttachParameter;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerExit;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ExecState;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.ImageSearchResult;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.messages.RemovedImage;
import com.spotify.docker.client.messages.Version;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.spotify.docker.client.DefaultDockerClient.NO_TIMEOUT;
import static com.spotify.docker.client.DockerClient.BuildParameter.FORCE_RM;
import static com.spotify.docker.client.DockerClient.BuildParameter.NO_CACHE;
import static com.spotify.docker.client.DockerClient.BuildParameter.NO_RM;
import static com.spotify.docker.client.DockerClient.ListImagesParam.allImages;
import static com.spotify.docker.client.DockerClient.ListImagesParam.danglingImages;
import static com.spotify.docker.client.DockerClient.LogsParameter.STDERR;
import static com.spotify.docker.client.DockerClient.LogsParameter.STDOUT;
import static com.spotify.docker.client.messages.RemovedImage.Type.UNTAGGED;
import static java.lang.Long.toHexString;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

public class DefaultDockerClientTest {

  private static final boolean CIRCLECI = !isNullOrEmpty(getenv("CIRCLECI"));
  // Using a dummy individual's test account because organizations
  // cannot have private repos on Docker Hub.
  private static final String AUTH_EMAIL = "dxia@spotify.com";
  private static final String AUTH_USERNAME = "dxia";
  private static final String AUTH_PASSWORD = "gRjK8tcQ7q";
  // Using yet another dummy individual's test account because CircleCI throws a weird error
  // when trying to pull the same private repo a second time. ¯\_(ツ)_/
  private static final String AUTH_EMAIL2 = "dxia+2@spotify.com";
  private static final String AUTH_USERNAME2 = "dxia2";
  private static final String AUTH_PASSWORD2 = "Tv38KLPd]M";

  private static final Logger log = LoggerFactory.getLogger(DefaultDockerClientTest.class);

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Rule public final TestName testName = new TestName();

  private final String nameTag = toHexString(ThreadLocalRandom.current().nextLong());

  private URI dockerEndpoint;

  private DefaultDockerClient sut;

  private AuthConfig authConfig;

  @Before
  public void setup() throws Exception {
    authConfig = AuthConfig.builder().email(AUTH_EMAIL).username(AUTH_USERNAME)
        .password(AUTH_PASSWORD).build();
    final DefaultDockerClient.Builder builder = DefaultDockerClient.fromEnv();
    builder.readTimeoutMillis(120000);
    dockerEndpoint = builder.uri();

    sut = builder.build();

    System.out.printf("- %s\n", testName.getMethodName());
  }

  @After
  public void tearDown() throws Exception {
    // Remove containers
    final List<Container> containers = sut.listContainers();
    for (Container container : containers) {
      final ContainerInfo info = sut.inspectContainer(container.id());
      if (info != null && info.name().contains(nameTag)) {
        try {
          sut.killContainer(info.id());
        } catch (DockerRequestException e) {
          // Docker 1.6 sometimes fails to kill a container because it disappears.
          // https://github.com/docker/docker/issues/12738
          log.warn("Failed to kill container {}", info.id(), e);
        }
      }
    }

    // Close the client
    sut.close();
  }

  @Test
  public void testSearchImage() throws Exception {
    // when
    final List<ImageSearchResult> searchResult = sut.searchImages("busybox");
    // then
    assertThat(searchResult.size(), greaterThan(0));
  }
  
  @Test
  public void testPullWithTag() throws Exception {
    sut.pull("busybox:buildroot-2014.02");
  }

  @Test(expected = ImageNotFoundException.class)
  public void testPullBadImage() throws Exception {
    sut.pull(randomName());
  }

  @Test(expected = ImageNotFoundException.class)
  public void testPullPrivateRepoWithoutAuth() throws Exception {
    sut.pull("dxia/cirros-private:latest");
  }

  @Test
  public void testPullPrivateRepoWithAuth() throws Exception {
    AuthConfig authConfig = AuthConfig.builder().email(AUTH_EMAIL2).username(AUTH_USERNAME2)
        .password(AUTH_PASSWORD2).build();
    sut.pull("dxia2/scratch-private:latest", authConfig);
  }

  @Test(expected = ImageNotFoundException.class)
  public void testPullPrivateRepoWithBadAuth() throws Exception {
    AuthConfig badAuthConfig = AuthConfig.builder().email(AUTH_EMAIL).username(AUTH_USERNAME)
        .password("foobar").build();
    sut.pull("dxia/cirros-private:latest", badAuthConfig);
  }

  @Test
  public void testFailedPullDoesNotLeakConn() throws Exception {
    log.info("Connection pool stats: " + getClientConnectionPoolStats(sut).toString());

    // Pull a non-existent image 10 times and check that the number of leased connections is still 0
    // I.e. check that we are not leaking connections.
    for (int i = 0; i < 10; i++) {
      try {
        sut.pull("busybox:" + randomName());
      } catch (ImageNotFoundException ignored) {
      }
      log.info("Connection pool stats: " + getClientConnectionPoolStats(sut).toString());
    }

    assertThat(getClientConnectionPoolStats(sut).getLeased(), equalTo(0));
  }

  @Test
  public void testPingReturnsOk() throws Exception {
    final String pingResponse = sut.ping();
    assertThat(pingResponse, equalTo("OK"));
  }

  @Test
  public void testVersion() throws Exception {
    final Version version = sut.version();
    assertThat(version.apiVersion(), not(isEmptyOrNullString()));
    assertThat(version.arch(), not(isEmptyOrNullString()));
    assertThat(version.gitCommit(), not(isEmptyOrNullString()));
    assertThat(version.goVersion(), not(isEmptyOrNullString()));
    assertThat(version.kernelVersion(), not(isEmptyOrNullString()));
    assertThat(version.os(), not(isEmptyOrNullString()));
    assertThat(version.version(), not(isEmptyOrNullString()));
  }

  @Test
  public void testAuth() throws Exception {
    final int statusCode = sut.auth(authConfig);
    assertThat(statusCode, equalTo(200));
  }

  @Test
  public void testBadAuth() throws Exception {
    AuthConfig badAuthConfig = AuthConfig.builder().email(AUTH_EMAIL).username(AUTH_USERNAME)
        .password("foobar").build();
    final int statusCode = sut.auth(badAuthConfig);
    assertThat(statusCode, equalTo(401));
  }

  @Test
  public void testMissingAuthParam() throws Exception {
    AuthConfig badAuthConfig =
        AuthConfig.builder().email(AUTH_EMAIL).username(AUTH_USERNAME).build();
    final int statusCode = sut.auth(badAuthConfig);
    assertThat(statusCode, equalTo(500));
  }

  @Test
  public void testInfo() throws Exception {
    final Info info = sut.info();
    assertThat(info.executionDriver(), not(isEmptyOrNullString()));
    assertThat(info.initPath(), not(isEmptyOrNullString()));
    assertThat(info.kernelVersion(), not(isEmptyOrNullString()));
    assertThat(info.storageDriver(), not(isEmptyOrNullString()));
    assertThat(info.memoryLimit(), not(nullValue()));
    assertThat(info.swapLimit(), not(nullValue()));
  }

  @Test
  public void testRemoveImage() throws Exception {
    // Don't remove images on CircleCI. Their version of Docker causes failures when pulling an
    // image that shares layers with an image that has been removed. This causes tests after this
    // one to fail.
    assumeThat(getenv("CIRCLECI"), isEmptyOrNullString());

    sut.pull("dxia/cirros");
    final String imageLatest = "dxia/cirros:latest";
    final String imageVersion = "dxia/cirros:0.3.0";

    final Set<RemovedImage> removedImages = Sets.newHashSet();
    removedImages.addAll(sut.removeImage(imageLatest));
    removedImages.addAll(sut.removeImage(imageVersion));

    assertThat(removedImages, hasItems(
        new RemovedImage(UNTAGGED, imageLatest),
        new RemovedImage(UNTAGGED, imageVersion)
    ));

    // Try to inspect deleted image and make sure ImageNotFoundException is thrown
    try {
      sut.inspectImage(imageLatest);
      fail("inspectImage should have thrown ImageNotFoundException");
    } catch (ImageNotFoundException e) {
      // we should get exception because we deleted image
    }
  }

  @Test
  public void testTag() throws Exception {
    sut.pull("busybox");

    // Tag image
    final String newImageName = "testRepo:testTag";
    sut.tag("busybox", newImageName);

    // Verify tag was successful by trying to remove it.
    final RemovedImage removedImage = getOnlyElement(sut.removeImage(newImageName));
    assertThat(removedImage, equalTo(new RemovedImage(UNTAGGED, newImageName)));
  }

  @Test
  public void testTagForce() throws Exception {
    sut.pull("busybox");

    final String name = "testRepo/tagForce:sometag";
    // Assign name to first image
    sut.tag("busybox:latest", name);

    // Force-re-assign tag to another image
    sut.tag("busybox:buildroot-2014.02", name, true);

    // Verify that re-tagging was successful
    final RemovedImage removedImage = getOnlyElement(sut.removeImage(name));
    assertThat(removedImage, is(new RemovedImage(UNTAGGED, name)));
  }

  @Test
  public void testInspectImage() throws Exception {
    sut.pull("busybox");
    final ImageInfo info = sut.inspectImage("busybox");
    assertThat(info, notNullValue());
    assertThat(info.architecture(), not(isEmptyOrNullString()));
    assertThat(info.author(), not(isEmptyOrNullString()));
    assertThat(info.config(), notNullValue());
    assertThat(info.container(), not(isEmptyOrNullString()));
    assertThat(info.containerConfig(), notNullValue());
    assertThat(info.comment(), notNullValue());
    assertThat(info.created(), notNullValue());
    assertThat(info.dockerVersion(), not(isEmptyOrNullString()));
    assertThat(info.id(), not(isEmptyOrNullString()));
    assertThat(info.os(), equalTo("linux"));
    assertThat(info.parent(), not(isEmptyOrNullString()));
    assertThat(info.size(), notNullValue());
    assertThat(info.virtualSize(), notNullValue());
  }

  @Test
  public void testCustomProgressMessageHandler() throws Exception {

    final List<ProgressMessage> messages = new ArrayList<>();

    sut.pull("busybox", new ProgressHandler() {
      @Override
      public void progress(ProgressMessage message) throws DockerException {
        messages.add(message);
      }
    });

    // Verify that we have multiple messages, and each one has a non-null field
    assertThat(messages, not(empty()));
    for (ProgressMessage message : messages) {
      assertTrue(message.error() != null ||
                 message.id() != null ||
                 message.progress() != null ||
                 message.progressDetail() != null ||
                 message.status() != null ||
                 message.stream() != null);
    }
  }

  @Test
  public void testBuildImageId() throws Exception {
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    final AtomicReference<String> imageIdFromMessage = new AtomicReference<>();

    final String returnedImageId = sut.build(
        Paths.get(dockerDirectory), "test", new ProgressHandler() {
          @Override
          public void progress(ProgressMessage message) throws DockerException {
            final String imageId = message.buildImageId();
            if (imageId != null) {
              imageIdFromMessage.set(imageId);
            }
          }
        });

    assertThat(returnedImageId, is(imageIdFromMessage.get()));
  }

  @Test
  public void testFailedBuildDoesNotLeakConn() throws Exception {
    final String dockerDirectory =
        Resources.getResource("dockerDirectoryNonExistentImage").getPath();

    log.info("Connection pool stats: " + getClientConnectionPoolStats(sut).toString());

    // Build an image from a bad Dockerfile 10 times and check that the number of
    // leased connections is still 0.
    // I.e. check that we are not leaking connections.
    for (int i = 0; i < 10; i++) {
      try {
        sut.build(Paths.get(dockerDirectory), "test");
      } catch (DockerException ignored) {
      }

      log.info("Connection pool stats: " + getClientConnectionPoolStats(sut).toString());
    }

    assertThat(getClientConnectionPoolStats(sut).getLeased(), equalTo(0));
  }

  @Test
  public void testBuildName() throws Exception {
    final String imageName = "test-build-name";
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    final String imageId = sut.build(Paths.get(dockerDirectory), imageName);
    final ImageInfo info = sut.inspectImage(imageName);
    assertThat(info.id(), startsWith(imageId));
  }

  @Test
  public void testBuildNoCache() throws Exception {
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    final String usingCache = "Using cache";

    // Build once to make sure we have cached images.
    sut.build(Paths.get(dockerDirectory));

    // Build again and make sure we used cached image by parsing output.
    final AtomicBoolean usedCache = new AtomicBoolean(false);
    sut.build(Paths.get(dockerDirectory), "test", new ProgressHandler() {
      @Override
      public void progress(ProgressMessage message) throws DockerException {
        if (message.stream().contains(usingCache)) {
          usedCache.set(true);
        }
      }
    });
    assertTrue(usedCache.get());

    // Build again with NO_CACHE set, and verify we don't use cache.
    sut.build(Paths.get(dockerDirectory), "test", new ProgressHandler() {
      @Override
      public void progress(ProgressMessage message) throws DockerException {
        assertThat(message.stream(), not(containsString(usingCache)));
      }
    }, NO_CACHE);
  }

  @Test
  public void testBuildNoRm() throws Exception {
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    final String removingContainers = "Removing intermediate container";

    // Test that intermediate containers are removed with FORCE_RM by parsing output. We must
    // set NO_CACHE so that docker will generate some containers to remove.
    final AtomicBoolean removedContainer = new AtomicBoolean(false);
    sut.build(Paths.get(dockerDirectory), "test", new ProgressHandler() {
      @Override
      public void progress(ProgressMessage message) throws DockerException {
        if (containsIgnoreCase(message.stream(), removingContainers)) {
          removedContainer.set(true);
        }
      }
    }, NO_CACHE, FORCE_RM);
    assertTrue(removedContainer.get());

    // Set NO_RM and verify we don't get message that containers were removed.
    sut.build(Paths.get(dockerDirectory), "test", new ProgressHandler() {
      @Override
      public void progress(ProgressMessage message) throws DockerException {
        assertThat(message.stream(), not(containsString(removingContainers)));
      }
    }, NO_CACHE, NO_RM);
  }

  @Test
  public void testGetImageIdFromBuild() {
    // Include a new line because that's what docker returns.
    final ProgressMessage message1 = new ProgressMessage()
        .stream("Successfully built 2d6e00052167\n");
    assertThat(message1.buildImageId(), is("2d6e00052167"));

    final ProgressMessage message2 = new ProgressMessage().id("123");
    assertThat(message2.buildImageId(), nullValue());

    final ProgressMessage message3 = new ProgressMessage().stream("Step 2 : CMD[]");
    assertThat(message3.buildImageId(), nullValue());
  }

  @Test
  public void testAnsiProgressHandler() throws Exception {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    sut.pull("busybox", new AnsiProgressHandler(new PrintStream(out)));
    // The progress handler uses ascii escape characters to move the cursor around to nicely print
    // progress bars. This is hard to test programmatically, so let's just verify the output
    // contains some expected phrases.
    assertThat(out.toString(), allOf(containsString("Pulling repository busybox"),
                                     containsString("Download complete")));
  }

  @Test
  public void testExportContainer() throws Exception {
    // Pull image
    sut.pull("busybox");

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image("busybox")
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    ImmutableSet.Builder<String> files = ImmutableSet.builder();
    try (TarArchiveInputStream tarStream = new TarArchiveInputStream(sut.exportContainer(id))) {
      TarArchiveEntry entry;
      while ((entry = tarStream.getNextTarEntry()) != null) {
        files.add(entry.getName());
      }
    }

    // Check that some common files exist
    assertThat(files.build(), both(hasItem("bin/")).and(hasItem("bin/sh")));
  }

  @Test
  public void testCopyContainer() throws Exception {
    // Pull image
    sut.pull("busybox");

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image("busybox")
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    ImmutableSet.Builder<String> files = ImmutableSet.builder();
    try (TarArchiveInputStream tarStream =
             new TarArchiveInputStream(sut.copyContainer(id, "/usr/bin"))) {
      TarArchiveEntry entry;
      while ((entry = tarStream.getNextTarEntry()) != null) {
        files.add(entry.getName());
      }
    }

    // Check that some common files exist
    assertThat(files.build(), both(hasItem("bin/")).and(hasItem("bin/wc")));
  }

  @Test
  public void testCommitContainer() throws Exception {
    // Pull image
    sut.pull("busybox");

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image("busybox")
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    String tag = randomName();
    ContainerCreation
        dockerClientTest =
        sut.commitContainer(id,"mosheeshel/busybox",tag, config, "CommitedByTest-" + tag,
                            "DockerClientTest");

    ImageInfo imageInfo = sut.inspectImage(dockerClientTest.id());
    assertThat(imageInfo.author(), is("DockerClientTest"));
    assertThat(imageInfo.comment(), is("CommitedByTest-" + tag));

  }

  @Test
  public void testStopContainer() throws Exception {
    sut.pull("busybox");

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image("busybox")
        // make sure the container's busy doing something upon startup
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    sut.startContainer(containerId);

    // Must be running
    {
      final ContainerInfo containerInfo = sut.inspectContainer(containerId);
      assertThat(containerInfo.state().running(), equalTo(true));
    }

    sut.stopContainer(containerId, 5);

    // Must no longer be running
    {
      final ContainerInfo containerInfo = sut.inspectContainer(containerId);
      assertThat(containerInfo.state().running(), equalTo(false));
    }
  }

  @Test
  public void testRestartContainer() throws Exception {
    sut.pull("busybox");

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image("busybox")
            // make sure the container's busy doing something upon startup
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    sut.startContainer(containerId);

    // Must be running
    {
      final ContainerInfo containerInfo = sut.inspectContainer(containerId);
      assertThat(containerInfo.state().running(), equalTo(true));
    }

    final ContainerInfo tempContainerInfo = sut.inspectContainer(containerId);
    final Integer originalPid = tempContainerInfo.state().pid();

    sut.restartContainer(containerId);

    // Should be running with short run time
    {
      final ContainerInfo containerInfoLatest = sut.inspectContainer(containerId);
      assertTrue(containerInfoLatest.state().running());
      assertThat(containerInfoLatest.state().pid(), not(equalTo(originalPid)));
    }
  }

  @Test
  public void integrationTest() throws Exception {

    // Pull image
    sut.pull("busybox");

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image("busybox")
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();
    assertThat(creation.getWarnings(), anyOf(is(empty()), is(nullValue())));
    assertThat(id, is(any(String.class)));

    // Inspect using container ID
    {
      final ContainerInfo info = sut.inspectContainer(id);
      assertThat(info.id(), equalTo(id));
      assertThat(info.config().image(), equalTo(config.image()));
      assertThat(info.config().cmd(), equalTo(config.cmd()));
    }

    // Inspect using container name
    {
      final ContainerInfo info = sut.inspectContainer(name);
      assertThat(info.config().image(), equalTo(config.image()));
      assertThat(info.config().cmd(), equalTo(config.cmd()));
    }

    // Start container
    sut.startContainer(id);

    // Kill container
    sut.killContainer(id);

    try {
      // Remove the container
      sut.removeContainer(id);
    } catch (DockerRequestException e) {
      if (CIRCLECI) {
        // yeah, circleci throws an exception whenever you try to RM a container.
        // fortunately it does actually remove it though.
      } else {
        throw e;
      }
    }

    // Verify that the container is gone
    exception.expect(ContainerNotFoundException.class);
    sut.inspectContainer(id);
  }

  @Test
  public void interruptTest() throws Exception {

    // Pull image
    sut.pull("busybox");

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image("busybox")
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    // Start container
    sut.startContainer(id);

    // Wait for container on a thread
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final SettableFuture<Boolean> started = SettableFuture.create();
    final SettableFuture<Boolean> interrupted = SettableFuture.create();

    final Future<ContainerExit> exitFuture = executorService.submit(new Callable<ContainerExit>() {
      @Override
      public ContainerExit call() throws Exception {
        try {
          started.set(true);
          return sut.waitContainer(id);
        } catch (InterruptedException e) {
          interrupted.set(true);
          throw e;
        }
      }
    });

    // Interrupt waiting thread
    started.get();
    executorService.shutdownNow();
    try {
      exitFuture.get();
      fail();
    } catch (ExecutionException e) {
      assertThat(e.getCause(), instanceOf(InterruptedException.class));
    }

    // Verify that the thread was interrupted
    assertThat(interrupted.get(), is(true));
  }

  @Test
  public void testFailedPushDoesNotLeakConn() throws Exception {
    log.info("Connection pool stats: " + getClientConnectionPoolStats(sut).toString());

    // Push a non-existent image 10 times and check that the number of
    // leased connections is still 0.
    // I.e. check that we are not leaking connections.
    for (int i = 0; i < 10; i++) {
      try {
        sut.push("foobarboooboo" + randomName());
      } catch (ImagePushFailedException ignored) {
      }

      log.info("Connection pool stats: " + getClientConnectionPoolStats(sut).toString());
    }

    assertThat(getClientConnectionPoolStats(sut).getLeased(), equalTo(0));
  }

  @Test(expected = DockerTimeoutException.class)
  public void testConnectTimeout() throws Exception {
    // Attempt to connect to reserved IP -> should timeout
    try (final DefaultDockerClient connectTimeoutClient = DefaultDockerClient.builder()
        .uri("http://240.0.0.1:2375")
        .connectTimeoutMillis(100)
        .readTimeoutMillis(NO_TIMEOUT)
        .build()) {
      connectTimeoutClient.version();
    }
  }

  @Test(expected = DockerTimeoutException.class)
  public void testReadTimeout() throws Exception {
    try (final ServerSocket s = new ServerSocket()) {
      // Bind and listen but do not accept -> read will time out.
      s.bind(new InetSocketAddress("127.0.0.1", 0));
      awaitConnectable(s.getInetAddress(), s.getLocalPort());
      final DockerClient connectTimeoutClient = DefaultDockerClient.builder()
          .uri("http://127.0.0.1:" + s.getLocalPort())
          .connectTimeoutMillis(NO_TIMEOUT)
          .readTimeoutMillis(100)
          .build();
      connectTimeoutClient.version();
    }
  }

  @Test(expected = DockerTimeoutException.class)
  public void testConnectionRequestTimeout() throws Exception {
    final int connectionPoolSize = 1;
    final int callableCount = connectionPoolSize * 100;

    final ExecutorService executor = Executors.newCachedThreadPool();
    final CompletionService completion = new ExecutorCompletionService(executor);

    // Spawn and wait on many more containers than the connection pool size.
    // This should cause a timeout once the connection pool is exhausted.

    final DockerClient dockerClient = DefaultDockerClient.fromEnv()
        .connectionPoolSize(connectionPoolSize)
        .build();
    try {
      // Create container
      final ContainerConfig config = ContainerConfig.builder()
          .image("busybox")
          .cmd("sh", "-c", "while :; do sleep 1; done")
          .build();
      final String name = randomName();
      final ContainerCreation creation = dockerClient.createContainer(config, name);
      final String id = creation.id();

      // Start the container
      dockerClient.startContainer(id);

      // Submit a bunch of waitContainer requests
      for (int i = 0; i < callableCount; i++) {
        completion.submit(new Callable<ContainerExit>() {
          @Override
          public ContainerExit call() throws Exception {
            return dockerClient.waitContainer(id);
          }
        });
      }

      // Wait for the requests to complete or throw expected exception
      for (int i = 0; i < callableCount; i++) {
        try {
          completion.take().get();
        } catch (ExecutionException e) {
          Throwables.propagateIfInstanceOf(e.getCause(), DockerTimeoutException.class);
          throw e;
        }
      }
    } finally {
      executor.shutdown();
      dockerClient.close();
    }
  }

  @Test
  public void testWaitContainer() throws Exception {
    sut.pull("busybox");

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image("busybox")
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    // Start the container
    sut.startContainer(id);

    // Wait for container on a thread
    final ExecutorService executorService = Executors.newSingleThreadExecutor();
    final Future<ContainerExit> exitFuture = executorService.submit(new Callable<ContainerExit>() {
      @Override
      public ContainerExit call() throws Exception {
        return sut.waitContainer(id);
      }
    });

    // Wait for 40 seconds, then kill the container
    Thread.sleep(40000);
    sut.killContainer(id);

    // Ensure that waiting on the container worked without exception
    exitFuture.get();
  }

  @Test
  public void testInspectContainerWithExposedPorts() throws Exception {
    sut.pull("rohan/memcached-mini");
    final ContainerConfig config = ContainerConfig.builder()
        .image("rohan/memcached-mini")
        .build();
    final ContainerCreation container = sut.createContainer(config, randomName());
    sut.startContainer(container.id());
    final ContainerInfo containerInfo = sut.inspectContainer(container.id());
    assertThat(containerInfo, notNullValue());
    assertThat(containerInfo.networkSettings().ports(), hasEntry("11211/tcp", null));
  }

  @Test
  public void testInspectContainerWithSecurityOpts() throws Exception {
    final String userLabel = "label:user:dxia";
    final String roleLabel = "label:role:foo";
    final String typeLabel = "label:type:bar";
    final String levelLabel = "label:level:9001";

    sut.pull("rohan/memcached-mini");
    final HostConfig hostConfig = HostConfig.builder()
        .securityOpt(userLabel, roleLabel, typeLabel, levelLabel)
        .build();
    final ContainerConfig config = ContainerConfig.builder()
            .image("rohan/memcached-mini")
            .hostConfig(hostConfig)
            .build();

    final ContainerCreation container = sut.createContainer(config, randomName());
    sut.startContainer(container.id());
    final ContainerInfo containerInfo = sut.inspectContainer(container.id());
    assertThat(containerInfo, notNullValue());
    assertThat(containerInfo.hostConfig().securityOpt(),
               hasItems(userLabel, roleLabel, typeLabel, levelLabel));
  }

  @Test
  public void testContainerWithHostConfig() throws Exception {
    assumeTrue("Docker API should be at least v1.18 to support Container Creation with " +
               "HostConfig, got " + sut.version().apiVersion(),
                versionCompare(sut.version().apiVersion(),"1.18") >= 0);

    sut.pull("busybox");

    final boolean privileged = true;
    final boolean publishAllPorts = true;
    final String dns = "1.2.3.4";
    final HostConfig expected = HostConfig.builder()
            .privileged(privileged)
            .publishAllPorts(publishAllPorts)
            .dns(dns)
            .cpuShares((long) 4096)
            .build();


    final ContainerConfig config = ContainerConfig.builder()
        .image("busybox")
        .hostConfig(expected)
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    sut.startContainer(id);

    final HostConfig actual = sut.inspectContainer(id).hostConfig();

    assertThat(actual.privileged(), equalTo(expected.privileged()));
    assertThat(actual.publishAllPorts(), equalTo(expected.publishAllPorts()));
    assertThat(actual.dns(), equalTo(expected.dns()));
  }

  @Test
  public void testListImages() throws Exception {
    sut.pull("busybox");
    final List<Image> images = sut.listImages();
    assertThat(images.size(), greaterThan(0));

    // Verify that image contains valid values
    final Image image = images.get(0);
    assertThat(image.virtualSize(), greaterThan(0L));
    assertThat(image.created(), not(isEmptyOrNullString()));
    assertThat(image.id(), not(isEmptyOrNullString()));
    assertThat(image.parentId(), not(isEmptyOrNullString()));

    // Using allImages() should give us more images
    final List<Image> allImages = sut.listImages(allImages());
    assertThat(allImages.size(), greaterThan(images.size()));

    // Including just dangling images should give us less images
    final List<Image> danglingImages = sut.listImages(danglingImages());
    assertThat(danglingImages.size(), lessThan(images.size()));

    // Specifying both allImages() and danglingImages() should give us only dangling images
    final List<Image> allAndDanglingImages = sut.listImages(allImages(), danglingImages());
    assertThat(allAndDanglingImages.size(), equalTo(danglingImages.size()));
  }

  @Test
  public void testDockerDateFormat() throws Exception {
    // This is the created date for busybox converted from nanoseconds to milliseconds
    final Date expected = new StdDateFormat().parse("2015-04-17T22:01:13.062Z");
    final DockerDateFormat dateFormat = new DockerDateFormat();
    // Verify DockerDateFormat handles millisecond precision correctly
    final Date milli = dateFormat.parse("2015-04-17T22:01:13.062Z");
    assertThat(milli, equalTo(expected));
    // Verify DockerDateFormat converts nanosecond precision down to millisecond precision
    final Date nano = dateFormat.parse("2015-04-17T22:01:13.062208605Z");
    assertThat(nano, equalTo(expected));
    // Verify the formatter works when used with the client
    sut.pull("busybox");
    final ImageInfo imageInfo = sut.inspectImage("busybox");
    assertThat(imageInfo.created(), equalTo(expected));
  }

  @Test
  public void testSsl() throws Exception {
    // Build a run a container that contains a Docker instance configured with our SSL cert/key
    final String imageName = "test-docker-ssl";
    final String expose = "2376/tcp";

    final String dockerDirectory = Resources.getResource("dockerSslDirectory").getPath();
    sut.build(Paths.get(dockerDirectory), imageName);

    final HostConfig hostConfig = HostConfig.builder()
            .privileged(true)
            .publishAllPorts(true)
            .build();
    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(imageName)
        .exposedPorts(expose)
        .hostConfig(hostConfig)
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    sut.startContainer(containerId);

    // Determine where the Docker instance inside the container we just started is exposed
    final String host;
    if (dockerEndpoint.getScheme().equalsIgnoreCase("unix")) {
      host = "localhost";
    } else {
      host = dockerEndpoint.getHost();
    }

    final ContainerInfo containerInfo = sut.inspectContainer(containerId);
    assertThat(containerInfo.state().running(), equalTo(true));

    final String port = containerInfo.networkSettings().ports().get(expose).get(0).hostPort();

    // Try to connect using SSL and our known cert/key
    final DockerCertificates certs = new DockerCertificates(Paths.get(dockerDirectory));
    final DockerClient c = new DefaultDockerClient(URI.create(format("https://%s:%s", host, port)),
                                                   certs);

    // We need to wait for the docker process inside the docker container to be ready to accept
    // connections on the port. Otherwise, this test will fail.
    // Even though we've checked that the container is running, this doesn't mean the process
    // inside the container is ready.
    final long deadline =
        System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(TimeUnit.MINUTES.toMillis(1));
    while (System.nanoTime() < deadline) {
      try (final Socket ignored = new Socket(host, Integer.parseInt(port))) {
        break;
      } catch (IOException ignored) {
      }
      Thread.sleep(500);
    }

    assertThat(c.ping(), equalTo("OK"));

    sut.stopContainer(containerId, 10);
  }

  @Test
  public void testPauseContainer() throws Exception {
    sut.pull("busybox");

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image("busybox")
            // make sure the container's busy doing something upon startup
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    sut.startContainer(containerId);

    // Must be running
    {
      final ContainerInfo containerInfo = sut.inspectContainer(containerId);
      assertThat(containerInfo.state().running(), equalTo(true));
    }

    sut.pauseContainer(containerId);

    // Must be paused
    {
      final ContainerInfo containerInfo = sut.inspectContainer(containerId);
      assertThat(containerInfo.state().paused(), equalTo(true));
    }

    sut.unpauseContainer(containerId);

    // Must no longer be paused
    {
      final ContainerInfo containerInfo = sut.inspectContainer(containerId);
      assertThat(containerInfo.state().paused(), equalTo(false));
    }
  }

  @Test
  public void testVolumesFrom() throws Exception {
    sut.pull("busybox");

    final String volumeContainer = randomName();
    final String mountContainer = randomName();

    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image("busybox")
        .volumes("/foo")
        .cmd("touch", "/foo/bar")
        .build();
    sut.createContainer(volumeConfig, volumeContainer);
    sut.startContainer(volumeContainer);
    sut.waitContainer(volumeContainer);

    final HostConfig mountHostConfig = HostConfig.builder()
        .volumesFrom(volumeContainer)
        .build();
    final ContainerConfig mountConfig = ContainerConfig.builder()
            .image("busybox")
            .hostConfig(mountHostConfig)
            .cmd("ls", "/foo")
            .build();

    sut.createContainer(mountConfig, mountContainer);
    sut.startContainer(mountContainer);
    sut.waitContainer(mountContainer);

    final ContainerInfo info = sut.inspectContainer(mountContainer);
    assertThat(info.state().running(), is(false));
    assertThat(info.state().exitCode(), is(0));

    final String logs;
    try (LogStream stream = sut.logs(info.id(), STDOUT, STDERR)) {
      logs = stream.readFully();
    }
    assertThat(logs, containsString("bar"));
  }

  @Test
  public void testAttachLog() throws Exception {
    sut.pull("busybox");

    final String volumeContainer = randomName();

    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image("busybox")
        .volumes("/foo")
        .cmd("ls", "-la")
        .build();
    sut.createContainer(volumeConfig, volumeContainer);
    sut.startContainer(volumeContainer);

    final String logs;
    try (LogStream stream = sut.attachContainer(volumeContainer,
        AttachParameter.LOGS, AttachParameter.STDOUT,
        AttachParameter.STDERR, AttachParameter.STREAM)) {
      logs = stream.readFully();
    }
    assertThat(logs, containsString("total"));

    sut.waitContainer(volumeContainer);
    final ContainerInfo info = sut.inspectContainer(volumeContainer);
    assertThat(info.state().running(), is(false));
    assertThat(info.state().exitCode(), is(0));
  }

  @Test(expected = ContainerNotFoundException.class)
  public void testStartBadContainer() throws Exception {
    sut.startContainer(randomName());
  }

  @Test(expected = ImageNotFoundException.class)
  public void testCreateContainerWithBadImage() throws Exception {
    final ContainerConfig config = ContainerConfig.builder()
        .image(randomName())
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
  }

  @Test(expected = ContainerNotFoundException.class)
  public void testKillBadContainer() throws Exception {
    sut.killContainer(randomName());
  }

  @Test(expected = ContainerNotFoundException.class)
  public void testPauseBadContainer() throws Exception {
    sut.pauseContainer(randomName());
  }

  @Test(expected = ContainerNotFoundException.class)
  public void testRemoveBadContainer() throws Exception {
    sut.removeContainer(randomName());
  }

  @Test(expected = ContainerNotFoundException.class)
  public void testRestartBadContainer() throws Exception {
    sut.restartContainer(randomName());
  }

  @Test(expected = ContainerNotFoundException.class)
  public void testStopBadContainer() throws Exception {
    sut.stopContainer(randomName(), 10);
  }

  @Test(expected = ImageNotFoundException.class)
  public void testTagBadImage() throws Exception {
    sut.tag(randomName(), randomName());
  }

  @Test(expected = ContainerNotFoundException.class)
  public void testUnpauseBadContainer() throws Exception {
    sut.unpauseContainer(randomName());
  }

  @Test(expected = ImageNotFoundException.class)
  public void testRemoveBadImage() throws Exception {
    sut.removeImage(randomName());
  }

  @Test
  public void testExec() throws DockerException, InterruptedException, IOException {
    assumeTrue("Docker API should be at least v1.15 to support Exec, got "
            + sut.version().apiVersion(), versionCompare(sut.version().apiVersion(), "1.15") >= 0);
    assumeThat("Only native (libcontainer) driver supports Exec",
            sut.info().executionDriver(), startsWith("native"));

    sut.pull("busybox");

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image("busybox")
        // make sure the container's busy doing something upon startup
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    sut.startContainer(containerId);

    String execId = sut.execCreate(containerId, new String[] {"ls", "-la"},
            DockerClient.ExecParameter.STDOUT,
            DockerClient.ExecParameter.STDERR);

    log.info("execId = {}", execId);

    try (LogStream stream = sut.execStart(execId)) {
      final String output = stream.readFully();
      log.info("Result:\n{}", output);
      assertThat(output, containsString("total"));
    }
  }

  @Test
  public void testExecInspect() throws DockerException, InterruptedException, IOException {
    assumeTrue("Docker API should be at least v1.15 to support Exec, got "
               + sut.version().apiVersion(), versionCompare(sut.version().apiVersion(), "1.15") >= 0);
    assumeThat("Only native (libcontainer) driver supports Exec",
               sut.info().executionDriver(), startsWith("native"));

    sut.pull("busybox");

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image("busybox")
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    sut.startContainer(containerId);

    String execId = sut.execCreate(containerId, new String[] {"sh", "-c", "exit 2"},
                                   DockerClient.ExecParameter.STDOUT,
                                   DockerClient.ExecParameter.STDERR);

    log.info("execId = {}", execId);
    try (LogStream stream = sut.execStart(execId)) {
      stream.readFully();
    }

    ExecState state = sut.execInspect(execId);
    assertThat(state.running(), is(false));
    assertThat(state.exitCode(), is(2));
  }

  @Test
  public void testExitedListContainersParam()
      throws DockerException, InterruptedException, UnsupportedEncodingException {
    sut.pull("busybox");

    final String randomLong = Long.toString(ThreadLocalRandom.current().nextLong());
    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image("busybox")
        .cmd("sh", "-c", "echo " + randomLong)
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    sut.startContainer(containerId);
    sut.waitContainer(containerId);

    final List<Container> containers = sut.listContainers(
        DockerClient.ListContainersParam.allContainers(),
        DockerClient.ListContainersParam.exitedContainers());
    assertThat(containers.size(), greaterThan(0));
    assertThat(containers.get(0).command(), containsString(randomLong));
  }

  @Test
  public void testLabels() throws DockerException, InterruptedException {
    assumeTrue("Docker API should be at least v1.18 to support Labels, got "
            + sut.version().apiVersion(), versionCompare(sut.version().apiVersion(), "1.18") >= 0);
    sut.pull("busybox");

    Map<String, String> labels = new HashMap<>();
    labels.put("name", "starship");
    labels.put("version", "1.6.2");

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
            .image("busybox")
            .labels(labels)
            .cmd("sleep", "1000")
            .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    // Start the container
    sut.startContainer(id);

    ContainerInfo containerInfo = sut.inspectContainer(id);
    assertThat(containerInfo.config().labels().keySet(), contains("name", "version"));
    assertThat(containerInfo.config().labels().values(), contains("starship", "1.6.2"));
  }

  @Test
  public void testMacAddress() throws Exception {
    assumeTrue("Docker API should be at least v1.18 to support Mac Address, got "
            + sut.version().apiVersion(), versionCompare(sut.version().apiVersion(), "1.18") >= 0);
    sut.pull("rohan/memcached-mini");
    final ContainerConfig config = ContainerConfig.builder()
            .image("busybox")
            .cmd("sleep", "1000")
            .macAddress("12:34:56:78:9a:bc")
            .build();
    final ContainerCreation container = sut.createContainer(config, randomName());
    sut.startContainer(container.id());
    final ContainerInfo containerInfo = sut.inspectContainer(container.id());
    assertThat(containerInfo, notNullValue());
    assertThat(containerInfo.config().macAddress(), equalTo("12:34:56:78:9a:bc"));
  }



  /**
   * Compares two version strings.
   * <p>
   * https://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
   * </p>
   * Use this instead of String.compareTo() for a non-lexicographical
   * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
   *
   * @param str1 a string of ordinal numbers separated by decimal points.
   * @param str2 a string of ordinal numbers separated by decimal points.
   * @return The result is a negative integer if str1 is _numerically_ less than str2.
   * The result is a positive integer if str1 is _numerically_ greater than str2.
   * The result is zero if the strings are _numerically_ equal.
   * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
   */
  private int versionCompare(String str1, String str2) {
    String[] vals1 = str1.split("\\.");
    String[] vals2 = str2.split("\\.");
    int i = 0;
    // set index to first non-equal ordinal or length of shortest version string
    while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
      i++;
    }
    // compare first non-equal ordinal number
    if (i < vals1.length && i < vals2.length) {
      int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
      return Integer.signum(diff);
    }
    // the strings are equal or one string is a substring of the other
    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
    else {
      return Integer.signum(vals1.length - vals2.length);
    }
  }

  private String randomName() {
    return nameTag + '-' + toHexString(ThreadLocalRandom.current().nextLong());
  }

  private void awaitConnectable(final InetAddress address, final int port)
      throws InterruptedException {
    while (true) {
      try (Socket ignored = new Socket(address, port)) {
        return;
      } catch (IOException e) {
        Thread.sleep(100);
      }
    }
  }

  private PoolStats getClientConnectionPoolStats(final DefaultDockerClient client) {
    return ((PoolingHttpClientConnectionManager) client.getClient().getConfiguration()
        .getProperty(ApacheClientProperties.CONNECTION_MANAGER)).getTotalStats();
  }

  private PoolStats getNoTimeoutClientConnectionPoolStats(final DefaultDockerClient client) {
    return ((PoolingHttpClientConnectionManager) client.getNoTimeoutClient().getConfiguration()
        .getProperty(ApacheClientProperties.CONNECTION_MANAGER)).getTotalStats();
  }
}
