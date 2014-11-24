/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerExit;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.messages.RemovedImage;
import com.spotify.docker.client.messages.TopResults;
import com.spotify.docker.client.messages.Version;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.spotify.docker.client.DefaultDockerClient.NO_TIMEOUT;
import static com.spotify.docker.client.DockerClient.BuildParameter.FORCE_RM;
import static com.spotify.docker.client.DockerClient.BuildParameter.NO_CACHE;
import static com.spotify.docker.client.DockerClient.BuildParameter.NO_RM;
import static com.spotify.docker.client.DockerClient.ListImagesParam.allImages;
import static com.spotify.docker.client.DockerClient.ListImagesParam.danglingImages;
import static com.spotify.docker.client.DockerClient.LogsParameter.STDERR;
import static com.spotify.docker.client.DockerClient.LogsParameter.STDOUT;
import static com.spotify.docker.client.messages.RemovedImage.Type.DELETED;
import static com.spotify.docker.client.messages.RemovedImage.Type.UNTAGGED;
import static java.lang.Long.toHexString;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class DefaultDockerClientTest {

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Rule public final TestName testName = new TestName();

  private final String nameTag = toHexString(ThreadLocalRandom.current().nextLong());

  private URI dockerEndpoint;

  private DefaultDockerClient sut;

  @Before
  public void setup() throws Exception {
    final DefaultDockerClient.Builder builder = DefaultDockerClient.fromEnv();
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
        sut.killContainer(info.id());
      }
    }

    // Close the client
    sut.close();
  }

  @Test
  public void testPullWithTag() throws Exception {
    sut.pull("busybox:buildroot-2014.02");
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
  public void testInfo() throws Exception {
    final Info info = sut.info();
    assertThat(info.executionDriver(), not(isEmptyOrNullString()));
    assertThat(info.initPath(), not(isEmptyOrNullString()));
    assertThat(info.kernelVersion(), not(isEmptyOrNullString()));
    assertThat(info.storageDriver(), not(isEmptyOrNullString()));
    assertThat(info.sockets(), not(empty()));
    assertThat(info.memoryLimit(), not(nullValue()));
    assertThat(info.swapLimit(), not(nullValue()));
  }

  @Test
  public void testRemoveImage() throws Exception {
    sut.pull("cirros");
    final String imageLatest = "cirros:latest";
    final String imageVersion = "cirros:0.3.0";

    final Set<RemovedImage> removedImages = Sets.newHashSet();
    removedImages.addAll(sut.removeImage(imageLatest));
    removedImages.addAll(sut.removeImage(imageVersion));

    assertThat(removedImages, containsInAnyOrder(
        new RemovedImage(UNTAGGED, imageLatest),
        new RemovedImage(UNTAGGED, imageVersion),
        new RemovedImage(DELETED,
                         "d20c88c95b28c21fff7e3d2d98a9ab85daebad04d6185ff572068167c20d7374"),
        new RemovedImage(DELETED,
                         "47595debf9e9440a28b20af6e9b2f83ca4d0ce4902bcea5e506c2ad42374bf33"),
        new RemovedImage(DELETED,
                         "f8184986c5454e9486bb32155cf0eb69b477893cc0717a29f1ff504f44e026d8"),
        new RemovedImage(DELETED,
                         "5dd62fd3b727a250becfbb341e80fa7047a9fb5812f7ee184973d2e74d6bfd4d"),
        new RemovedImage(DELETED,
                         "16a464be5494a73be34a3055b77ae00d072a4f9389897d1a786de0079219aaeb")
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
  public void testTopProcessesOfContainer() throws Exception {
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

    // Ensure that it's running so we can check the active processes
    {
      final ContainerInfo containerInfo = sut.inspectContainer(containerId);
      assertThat(containerInfo.state().running(), equalTo(true));
    }

    final TopResults topResults = sut.topContainer(containerId, null);

    assertThat(topResults.titles(), not(Matchers.empty()));
    // there could be one or two processes running, depending on if we happen to catch it in between sleeps
    assertThat(topResults.processes(), hasSize(greaterThanOrEqualTo(1)));

    assertThat(topResults.titles(), hasItem("CMD"));

    final List<String> firstProcessStatus = topResults.processes().get(0);
    assertThat("All processes will run as 'root'", firstProcessStatus, hasItem("root"));
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

    // Remove the container
    sut.removeContainer(id);

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
  public void testContainerWithHostConfig() throws Exception {
    sut.pull("busybox");

    final ContainerConfig config = ContainerConfig.builder()
        .image("busybox")
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    final boolean privileged = true;
    final boolean publishAllPorts = true;
    final String dns = "1.2.3.4";

    final HostConfig expected = HostConfig.builder()
        .privileged(privileged)
        .publishAllPorts(publishAllPorts)
        .dns(dns)
        .build();
    sut.startContainer(id, expected);

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
    final Date expected = new StdDateFormat().parse("2014-10-01T20:46:08.914Z");
    final DockerDateFormat dateFormat = new DockerDateFormat();
    // Verify DockerDateFormat handles millisecond precision correctly
    final Date milli = dateFormat.parse("2014-10-01T20:46:08.914Z");
    assertThat(milli, equalTo(expected));
    // Verify DockerDateFormat converts nanosecond precision down to millisecond precision
    final Date nano = dateFormat.parse("2014-10-01T20:46:08.914288461Z");
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

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(imageName)
        .exposedPorts(expose)
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    final HostConfig hostConfig = HostConfig.builder()
        .privileged(true)
        .publishAllPorts(true)
        .build();
    sut.startContainer(containerId, hostConfig);

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

    final ContainerConfig mountConfig = ContainerConfig.builder()
        .image("busybox")
        .cmd("ls", "/foo")
        .build();
    final HostConfig mountHostConfig = HostConfig.builder()
        .volumesFrom(volumeContainer)
        .build();
    sut.createContainer(mountConfig, mountContainer);
    sut.startContainer(mountContainer, mountHostConfig);
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
}
