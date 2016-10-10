/*
 * Copyright (c) 2014 Spotify AB.
 * Copyright (c) 2016 ThoughtWorks, Inc.
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.SettableFuture;

import com.spotify.docker.client.DockerClient.AttachParameter;
import com.spotify.docker.client.DockerClient.BuildParam;
import com.spotify.docker.client.DockerClient.ExecCreateParam;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.exceptions.BadParamException;
import com.spotify.docker.client.exceptions.ConflictException;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.ContainerRenameConflictException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.exceptions.ImagePushFailedException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.exceptions.UnsupportedApiVersionException;
import com.spotify.docker.client.exceptions.VolumeNotFoundException;
import com.spotify.docker.client.messages.AttachedNetwork;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerChange;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerExit;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerMount;
import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.Event;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.ExecState;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.HostConfig.Ulimit;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageHistory;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.ImageSearchResult;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.Ipam;
import com.spotify.docker.client.messages.LogConfig;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkCreation;
import com.spotify.docker.client.messages.ProcessConfig;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.messages.RemovedImage;
import com.spotify.docker.client.messages.ServiceCreateOptions;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.TopResults;
import com.spotify.docker.client.messages.Version;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.VolumeList;
import com.spotify.docker.client.messages.swarm.ContainerSpec;
import com.spotify.docker.client.messages.swarm.Driver;
import com.spotify.docker.client.messages.swarm.EndpointSpec;
import com.spotify.docker.client.messages.swarm.NetworkAttachmentConfig;
import com.spotify.docker.client.messages.swarm.PortConfig;
import com.spotify.docker.client.messages.swarm.ResourceRequirements;
import com.spotify.docker.client.messages.swarm.RestartPolicy;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.ServiceMode;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.Swarm;
import com.spotify.docker.client.messages.swarm.Task;
import com.spotify.docker.client.messages.swarm.TaskSpec;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import java.util.regex.Pattern;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.getOnlyElement;
import static com.spotify.docker.client.DefaultDockerClient.NO_TIMEOUT;
import static com.spotify.docker.client.DockerClient.ListContainersParam.allContainers;
import static com.spotify.docker.client.DockerClient.ListContainersParam.withLabel;
import static com.spotify.docker.client.DockerClient.ListContainersParam.withStatusCreated;
import static com.spotify.docker.client.DockerClient.ListContainersParam.withStatusExited;
import static com.spotify.docker.client.DockerClient.ListContainersParam.withStatusPaused;
import static com.spotify.docker.client.DockerClient.ListContainersParam.withStatusRunning;
import static com.spotify.docker.client.DockerClient.ListImagesParam.allImages;
import static com.spotify.docker.client.DockerClient.ListImagesParam.byName;
import static com.spotify.docker.client.DockerClient.ListImagesParam.danglingImages;
import static com.spotify.docker.client.DockerClient.ListImagesParam.digests;
import static com.spotify.docker.client.DockerClient.ListVolumesParam.dangling;
import static com.spotify.docker.client.DockerClient.ListVolumesParam.driver;
import static com.spotify.docker.client.DockerClient.ListVolumesParam.name;
import static com.spotify.docker.client.DockerClient.LogsParam.follow;
import static com.spotify.docker.client.DockerClient.LogsParam.since;
import static com.spotify.docker.client.DockerClient.LogsParam.stderr;
import static com.spotify.docker.client.DockerClient.LogsParam.stdout;
import static com.spotify.docker.client.DockerClient.LogsParam.tail;
import static com.spotify.docker.client.DockerClient.LogsParam.timestamps;
import static com.spotify.docker.client.VersionCompare.compareVersion;
import static com.spotify.docker.client.messages.RemovedImage.Type.UNTAGGED;
import static java.lang.Long.toHexString;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

public class DefaultDockerClientTest {

  private static final String BUSYBOX = "busybox";
  private static final String BUSYBOX_LATEST = BUSYBOX + ":latest";
  private static final String BUSYBOX_BUILDROOT_2013_08_1 = BUSYBOX + ":buildroot-2013.08.1";
  private static final String MEMCACHED = "rohan/memcached-mini";
  private static final String MEMCACHED_LATEST = MEMCACHED + ":latest";
  private static final String CIRROS_PRIVATE = "dxia/cirros-private";
  private static final String CIRROS_PRIVATE_LATEST = CIRROS_PRIVATE + ":latest";

  private static final boolean CIRCLECI = !isNullOrEmpty(getenv("CIRCLECI"));
  private static final boolean TRAVIS = "true".equals(getenv("TRAVIS"));

  private static final String AUTH_EMAIL = "dxia+2@spotify.com";
  private static final String AUTH_USERNAME = "dxia2";
  private static final String AUTH_PASSWORD = "Tv38KLPd]M";

  private static final Logger log = LoggerFactory.getLogger(DefaultDockerClientTest.class);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Rule
  public final TestName testName = new TestName();

  private final String nameTag = toHexString(ThreadLocalRandom.current().nextLong());

  private URI dockerEndpoint;

  private DefaultDockerClient sut;

  private AuthConfig authConfig;

  private String dockerApiVersion;

  @Before
  public void setup() throws Exception {
    authConfig = AuthConfig.builder().email(AUTH_EMAIL).username(AUTH_USERNAME)
        .password(AUTH_PASSWORD).build();
    final DefaultDockerClient.Builder builder = DefaultDockerClient.fromEnv();
    // Make it easier to test no read timeout occurs by using a smaller value
    // Such test methods should end in 'NoTimeout'
    if (testName.getMethodName().endsWith("NoTimeout")) {
      builder.readTimeoutMillis(5000);
    } else {
      builder.readTimeoutMillis(120000);
    }

    dockerEndpoint = builder.uri();

    sut = builder.build();

    dockerApiVersion = sut.version().apiVersion();

    System.out.printf("- %s\n", testName.getMethodName());
  }

  @After
  public void tearDown() throws Exception {
    if (dockerApiVersionAtLeast("1.24")) {
      final List<Service> services = sut.listServices();
      for (final Service service : services) {
        if (service.spec().name().startsWith(nameTag)) {
          sut.removeService(service.id());
        }
      }
    }

    // Remove containers
    final List<Container> containers = sut.listContainers();
    for (final Container container : containers) {
      final ContainerInfo info = sut.inspectContainer(container.id());
      if (info != null && info.name().startsWith(nameTag)) {
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

  private void requireDockerApiVersionAtLeast(final String required, final String functionality)
      throws Exception {

    final String msg = String.format(
        "Docker API should be at least v%s to support %s but runtime version is %s",
        required, functionality, dockerApiVersion);

    assumeTrue(msg, dockerApiVersionAtLeast(required));
  }

  private boolean dockerApiVersionAtLeast(String expected) throws Exception {
    return compareVersion(dockerApiVersion, expected) >= 0;
  }

  private boolean dockerApiVersionLessThan(String expected) throws Exception {
    return compareVersion(dockerApiVersion, expected) < 0;
  }

  private void requireDockerApiVersionLessThan(final String required, final String functionality)
      throws Exception {

    final String actualVersion = sut.version().apiVersion();
    final String msg = String.format(
        "Docker API should be less than v%s to support %s but runtime version is %s",
        required, functionality, actualVersion);

    assumeTrue(msg, dockerApiVersionLessThan(required));
  }

  private boolean dockerApiVersionNot(final String expected) {
    return compareVersion(dockerApiVersion, expected) != 0;
  }

  private void requireDockerApiVersionNot(final String version, final String msg) {
    assumeTrue(msg, dockerApiVersionNot(version));
  }

  @Test
  public void testSearchImage() throws Exception {
    requireDockerApiVersionNot("1.19", "Docker 1.7.x sends the wrong Content-Type header for "
                                       + "/images/search. So we skip this test.");
    // when
    final List<ImageSearchResult> searchResult = sut.searchImages(BUSYBOX);
    // then
    assertThat(searchResult.size(), greaterThan(0));
  }

  @Test
  public void testPullWithTag() throws Exception {
    sut.pull(BUSYBOX_BUILDROOT_2013_08_1);
  }

  @Test(expected = ImageNotFoundException.class)
  public void testPullBadImage() throws Exception {
    // The Docker daemon on CircleCI won't throw ImageNotFoundException for some reason...
    assumeFalse(CIRCLECI);
    sut.pull(randomName());
  }

  @Test(expected = ImageNotFoundException.class)
  public void testPullPrivateRepoWithoutAuth() throws Exception {
    sut.pull(CIRROS_PRIVATE_LATEST);
  }

  @Test
  public void testBuildImageIdWithBuildargs() throws Exception {
    requireDockerApiVersionAtLeast("1.21", "build args");

    final String dockerDirectory = Resources.getResource("dockerDirectoryWithBuildargs").getPath();
    final String buildargs = "{\"testargument\":\"22-12-2015\"}";
    final BuildParam buildParam =
        BuildParam.create("buildargs", URLEncoder.encode(buildargs, "UTF-8"));
    sut.build(
        Paths.get(dockerDirectory),
        "test-buildargs",
        buildParam
    );
  }

  @Test
  public void testFailedPullDoesNotLeakConn() throws Exception {
    log.info("Connection pool stats: " + getClientConnectionPoolStats(sut).toString());

    // Pull a non-existent image 10 times and check that the number of leased connections is still 0
    // I.e. check that we are not leaking connections.
    for (int i = 0; i < 10; i++) {
      try {
        sut.pull(BUSYBOX + ":" + randomName());
      } catch (ImageNotFoundException ignored) {
      }
      log.info("Connection pool stats: " + getClientConnectionPoolStats(sut).toString());
    }

    assertThat(getClientConnectionPoolStats(sut).getLeased(), equalTo(0));
  }

  @Test
  public void testPullByDigest() throws Exception {
    // The current Docker client on CircleCI does allow you to pull images by digest.
    assumeFalse(CIRCLECI);

    // note: this digest may change over time, the value here may disappear from hub.docker.com
    sut.pull(BUSYBOX + "@sha256:4a887a2326ec9e0fa90cce7b4764b0e627b5d6afcb81a3f73c85dc29cea00048");
  }

  @Test
  public void testSave() throws Exception {
    // Ensure the local Docker instance has the busybox image so that save() will work
    sut.pull(BUSYBOX_LATEST);
    final File imageFile = save(BUSYBOX);
    assertTrue(imageFile.length() > 0);
  }

  private File save(final String image) throws Exception {
    final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    assertTrue("Temp directory " + tmpDir.getAbsolutePath() + " does not exist", tmpDir.exists());
    final File imageFile = new File(tmpDir, "busybox-" + System.nanoTime() + ".tar");
    //noinspection ResultOfMethodCallIgnored
    imageFile.createNewFile();
    imageFile.deleteOnExit();
    final byte[] buffer = new byte[2048];
    int read;
    try (OutputStream imageOutput = new BufferedOutputStream(new FileOutputStream(imageFile))) {
      try (InputStream imageInput = sut.save(image)) {
        while ((read = imageInput.read(buffer)) > -1) {
          imageOutput.write(buffer, 0, read);
        }
      }
    }
    return imageFile;
  }

  @Test
  public void testCreate() throws Exception {
    // Ensure the local Docker instance has the busybox image so that save() will work
    sut.pull(BUSYBOX_LATEST);
    final File imageFile = save(BUSYBOX);
    final String image = BUSYBOX + "test" + System.nanoTime();

    try (InputStream imagePayload = new BufferedInputStream(new FileInputStream(imageFile))) {
      sut.create(image, imagePayload);
    }

    final Collection<Image> images = Collections2.filter(sut.listImages(), new Predicate<Image>() {
      @Override
      public boolean apply(final Image img) {
        return img.repoTags() != null && img.repoTags().contains(image + ":latest");
      }
    });

    assertThat(images.size(), greaterThan(0));

    for (final Image img : images) {
      sut.removeImage(img.id());
    }
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
    if (dockerApiVersionAtLeast("1.22")) {
      assertThat(version.buildTime(), not(isEmptyOrNullString()));
    }
  }

  @Test
  public void testAuth() throws Exception {
    final int statusCode = sut.auth(authConfig);
    assertThat(statusCode, equalTo(200));
  }

  @Test
  public void testBadAuth() throws Exception {
    final AuthConfig badAuthConfig = AuthConfig.builder()
        .email(AUTH_EMAIL)
        .username(AUTH_USERNAME)
        .password("foobar")
        .build();
    final int statusCode = sut.auth(badAuthConfig);
    assertThat(statusCode, equalTo(401));
  }

  @Test
  public void testMissingAuthParam() throws Exception {
    requireDockerApiVersionNot("1.23", "https://github.com/docker/docker/issues/24093");
    requireDockerApiVersionNot("1.24", "https://github.com/docker/docker/issues/24093");
    final AuthConfig badAuthConfig = AuthConfig.builder()
        .email(AUTH_EMAIL)
        .username(AUTH_USERNAME)
        .build();
    final int statusCode = sut.auth(badAuthConfig);
    assertThat(statusCode, equalTo(500));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testInfo() throws Exception {
    final Info info = sut.info();
    assertThat(info.containers(), is(anything()));
    assertThat(info.debug(), is(anything()));
    assertThat(info.dockerRootDir(), not(isEmptyOrNullString()));
    assertThat(info.storageDriver(), not(isEmptyOrNullString()));
    assertThat(info.driverStatus(), is(anything()));
    if (dockerApiVersionLessThan("1.23")) {
      // Execution driver was removed in 1.24 https://github.com/docker/docker/pull/24501
      // But it also shows up as "" in 1.23, and I don't know why - JF
      assertThat(info.executionDriver(), not(isEmptyOrNullString()));
    }
    assertThat(info.id(), not(isEmptyOrNullString()));
    assertThat(info.ipv4Forwarding(), is(anything()));
    assertThat(info.images(), greaterThan(0));
    assertThat(info.indexServerAddress(), not(isEmptyOrNullString()));
    if (dockerApiVersionLessThan("1.23")) {
      // Init path seems to have been removed in API 1.23.
      // Still documented as of 2016-09-26, but InitPath field is not in /info - JF
      assertThat(info.initPath(), not(isEmptyOrNullString()));
    }
    assertThat(info.initSha1(), is(anything()));
    assertThat(info.kernelVersion(), not(isEmptyOrNullString()));
    assertThat(info.labels(), is(anything()));
    assertThat(info.memTotal(), greaterThan(0L));
    assertThat(info.memoryLimit(), not(nullValue()));
    assertThat(info.cpus(), greaterThan(0));
    assertThat(info.eventsListener(), is(anything()));
    assertThat(info.fileDescriptors(), is(anything()));
    assertThat(info.goroutines(), is(anything()));
    assertThat(info.name(), not(isEmptyOrNullString()));
    assertThat(info.operatingSystem(), not(isEmptyOrNullString()));
    assertThat(info.registryConfig(), notNullValue());
    assertThat(info.registryConfig().indexConfigs(), hasKey("docker.io"));
    assertThat(info.swapLimit(), not(nullValue()));

    if (dockerApiVersionAtLeast("1.18")) {
      assertThat(info.httpProxy(), is(anything()));
      assertThat(info.httpsProxy(), is(anything()));
      assertThat(info.noProxy(), is(anything()));
      assertThat(info.systemTime(), not(nullValue()));
    }

    if (dockerApiVersionAtLeast("1.19")) {
      assertThat(info.cpuCfsPeriod(), is(anything()));
      assertThat(info.cpuCfsQuota(), is(anything()));
      assertThat(info.experimentalBuild(), is(anything()));
      assertThat(info.oomKillDisable(), is(anything()));
    }

    if (dockerApiVersionAtLeast("1.21")) {
      assertThat(info.clusterStore(), is(anything()));
      assertEquals(info.serverVersion(), sut.version().version());
    }

    if (dockerApiVersionAtLeast("1.22")) {
      assertThat(info.architecture(), not(isEmptyOrNullString()));
      assertThat(info.containersRunning(), is(anything()));
      assertThat(info.containersStopped(), is(anything()));
      assertThat(info.containersPaused(), is(anything()));
      assertThat(info.osType(), not(isEmptyOrNullString()));
      assertThat(info.systemStatus(), is(anything()));
    }

    if (dockerApiVersionAtLeast("1.23")) {
      assertThat(info.cgroupDriver(), not(isEmptyOrNullString()));
      assertThat(info.kernelMemory(), is(anything()));
    }
  }

  @Test
  public void testRemoveImage() throws Exception {
    // Don't remove images on CircleCI. Their version of Docker causes failures when pulling an
    // image that shares layers with an image that has been removed. This causes tests after this
    // one to fail.
    assumeFalse(CIRCLECI);

    sut.pull("dxia/cirros:latest");
    sut.pull("dxia/cirros:0.3.0");
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
    sut.pull(BUSYBOX_LATEST);

    // Tag image
    final String newImageName = "test-repo:testTag";
    sut.tag(BUSYBOX, newImageName);

    // Verify tag was successful by trying to remove it.
    final RemovedImage removedImage = getOnlyElement(sut.removeImage(newImageName));
    assertThat(removedImage, equalTo(new RemovedImage(UNTAGGED, newImageName)));
  }

  @Test
  public void testTagForce() throws Exception {
    sut.pull(BUSYBOX_LATEST);
    sut.pull(BUSYBOX_BUILDROOT_2013_08_1);

    final String name = "test-repo/tag-force:sometag";
    // Assign name to first image
    sut.tag(BUSYBOX_LATEST, name);

    // Force-re-assign tag to another image
    sut.tag(BUSYBOX_BUILDROOT_2013_08_1, name, true);

    // Verify that re-tagging was successful
    final RemovedImage removedImage = getOnlyElement(sut.removeImage(name));
    assertThat(removedImage, is(new RemovedImage(UNTAGGED, name)));
  }

  @Test
  public void testInspectImage() throws Exception {
    sut.pull(BUSYBOX_BUILDROOT_2013_08_1);
    final ImageInfo info = sut.inspectImage(BUSYBOX_BUILDROOT_2013_08_1);
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

    //noinspection StatementWithEmptyBody
    if (dockerApiVersionLessThan("1.22")) {
      assertThat(info.parent(), not(isEmptyOrNullString()));
    } else {
      // The "parent" field can be empty because of changes in
      // image storage in 1.10. See https://github.com/docker/docker/issues/19650.
    }

    assertThat(info.size(), notNullValue());
    assertThat(info.virtualSize(), notNullValue());
  }

  @Test
  public void testCustomProgressMessageHandler() throws Exception {

    final List<ProgressMessage> messages = new ArrayList<>();

    sut.pull(BUSYBOX_LATEST, new ProgressHandler() {
      @Override
      public void progress(ProgressMessage message) throws DockerException {
        messages.add(message);
      }
    });

    // Verify that we have multiple messages, and each one has a non-null field
    assertThat(messages, not(empty()));
    for (final ProgressMessage message : messages) {
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
  public void testBuildImageIdPathToDockerFile() throws Exception {
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    final AtomicReference<String> imageIdFromMessage = new AtomicReference<>();

    final String returnedImageId = sut.build(
        Paths.get(dockerDirectory), "test", "innerDir/innerDockerfile", new ProgressHandler() {
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
  public void testBuildImageIdWithAuth() throws Exception {
    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    final AtomicReference<String> imageIdFromMessage = new AtomicReference<>();

    final DefaultDockerClient sut2 = DefaultDockerClient.fromEnv()
        .authConfig(authConfig)
        .build();

    final String returnedImageId = sut2.build(
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
    final String expectedId = dockerApiVersionLessThan("1.22") ? imageId : "sha256:" + imageId;
    assertThat(info.id(), startsWith(expectedId));
  }

  @Test
  public void testBuildWithPull() throws Exception {
    requireDockerApiVersionAtLeast("1.19", "build with pull");

    final String dockerDirectory = Resources.getResource("dockerDirectory").getPath();
    final String pullMsg = "Pulling from";

    // Build once to make sure we have cached images.
    sut.build(Paths.get(dockerDirectory));

    // Build again with PULL set, and verify we pulled the base image
    final AtomicBoolean pulled = new AtomicBoolean(false);
    sut.build(Paths.get(dockerDirectory), "test", new ProgressHandler() {
      @Override
      public void progress(ProgressMessage message) throws DockerException {
        if (!isNullOrEmpty(message.status()) && message.status().contains(pullMsg)) {
          pulled.set(true);
        }
      }
    }, BuildParam.pullNewerImage());
    assertTrue(pulled.get());
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
    }, BuildParam.noCache());
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
    }, BuildParam.noCache(), BuildParam.forceRm());
    assertTrue(removedContainer.get());

    // Set NO_RM and verify we don't get message that containers were removed.
    sut.build(Paths.get(dockerDirectory), "test", new ProgressHandler() {
      @Override
      public void progress(ProgressMessage message) throws DockerException {
        assertThat(message.stream(), not(containsString(removingContainers)));
      }
    }, BuildParam.noCache(), BuildParam.rm(false));
  }

  @Test
  public void testBuildNoTimeout() throws Exception {
    // The Dockerfile specifies a sleep of 10s during the build
    // Returned image id is last piece of output, so this confirms stream did not timeout
    final String dockerDirectory = Resources.getResource("dockerDirectorySleeping").getPath();
    final String returnedImageId = sut.build(
        Paths.get(dockerDirectory), "test", new ProgressHandler() {
          @Override
          public void progress(ProgressMessage message) throws DockerException {
            log.info(message.stream());
          }
        }, BuildParam.noCache());
    assertTrue(returnedImageId != null);
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
    sut.pull(BUSYBOX_LATEST, new AnsiProgressHandler(new PrintStream(out)));
    // The progress handler uses ascii escape characters to move the cursor around to nicely print
    // progress bars. This is hard to test programmatically, so let's just verify the output
    // contains some expected phrases.
    final String pullingStr = dockerApiVersionAtLeast("1.20") ?
                              "Pulling from library/busybox" : "Pulling from busybox";
    assertThat(out.toString(), allOf(containsString(pullingStr),
                                     containsString("Image is up to date")));
  }

  @Test
  public void testExportContainer() throws Exception {
    // Pull image
    sut.pull(BUSYBOX_LATEST);

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    final ImmutableSet.Builder<String> files = ImmutableSet.builder();
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
  @SuppressWarnings("deprecation")
  public void testCopyContainer() throws Exception {
    requireDockerApiVersionLessThan("1.24", "failCopyToContainer");

    // Pull image
    sut.pull(BUSYBOX_LATEST);

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    final ImmutableSet.Builder<String> files = ImmutableSet.builder();
    try (final TarArchiveInputStream tarStream =
             new TarArchiveInputStream(sut.copyContainer(id, "/bin"))) {
      TarArchiveEntry entry;
      while ((entry = tarStream.getNextTarEntry()) != null) {
        files.add(entry.getName());
      }
    }

    // Check that some common files exist
    assertThat(files.build(), both(hasItem("bin/")).and(hasItem("bin/wc")));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testFailCopyContainer() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "failCopyToContainer");

    exception.expect(UnsupportedApiVersionException.class);

    // Pull image
    sut.pull(BUSYBOX_LATEST);

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    sut.copyContainer(id, "/bin");
  }

  @Test
  public void testArchiveContainer() throws Exception {
    requireDockerApiVersionAtLeast("1.20", "copyToContainer");

    // Pull image
    sut.pull(BUSYBOX_LATEST);

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    final ImmutableSet.Builder<String> files = ImmutableSet.builder();
    try (final TarArchiveInputStream tarStream =
             new TarArchiveInputStream(sut.archiveContainer(id, "/bin"))) {
      TarArchiveEntry entry;
      while ((entry = tarStream.getNextTarEntry()) != null) {
        files.add(entry.getName());
      }
    }

    // Check that some common files exist
    assertThat(files.build(), both(hasItem("bin/")).and(hasItem("bin/wc")));
  }

  @Test
  public void testFailArchiveContainer() throws Exception {
    requireDockerApiVersionLessThan("1.20", "failCopyToContainer");

    exception.expect(UnsupportedApiVersionException.class);

    // Pull image
    sut.pull(BUSYBOX_LATEST);

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    sut.archiveContainer(id, "/bin");
  }

  @Test
  public void testCopyToContainer() throws Exception {
    requireDockerApiVersionAtLeast("1.20", "copyToContainer");

    // Pull image
    sut.pull(BUSYBOX_LATEST);

    // Create container
    final ContainerConfig config = ContainerConfig.builder().image(BUSYBOX_LATEST).build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String containerId = creation.id();

    final String dockerDirectory = Resources.getResource("dockerSslDirectory").getPath();
    try {
      sut.copyToContainer(Paths.get(dockerDirectory), containerId, "/tmp");
    } catch (Exception e) {
      fail("error to copy files to container");
    }
  }

  @Test
  public void testCommitContainer() throws Exception {
    // Pull image
    sut.pull(BUSYBOX_LATEST);

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    final String tag = randomName();
    final ContainerCreation dockerClientTest =
        sut.commitContainer(id, "mosheeshel/busybox", tag, config, "CommitedByTest-" + tag,
                            "DockerClientTest");

    final ImageInfo imageInfo = sut.inspectImage(dockerClientTest.id());
    assertThat(imageInfo.author(), is("DockerClientTest"));
    assertThat(imageInfo.comment(), is("CommitedByTest-" + tag));

  }

  @Test
  public void testStopContainer() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
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
    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
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
    // there could be one or two processes running, depending on if we happen to catch it in
    // between sleeps
    assertThat(topResults.processes(), hasSize(greaterThanOrEqualTo(1)));

    assertThat(topResults.titles(), either(hasItem("CMD")).or(hasItem("COMMAND")));

    final List<String> firstProcessStatus = topResults.processes().get(0);
    assertThat("All processes will run as 'root'", firstProcessStatus, hasItem("root"));
  }

  @Test
  public void testRestartContainer() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
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
  @SuppressWarnings("deprecation")
  public void integrationTest() throws Exception {
    // Pull image
    sut.pull(BUSYBOX_LATEST);

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
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

    final String dockerDirectory = Resources.getResource("dockerSslDirectory").getPath();

    // Copy files to container
    // Docker API should be at least v1.20 to support extracting an archive of files or folders
    // to a directory in a container
    if (dockerApiVersionAtLeast("1.20")) {
      try {
        sut.copyToContainer(Paths.get(dockerDirectory), id, "/tmp");
      } catch (Exception e) {
        fail("error copying files to container");
      }

      // Copy the same files from container
      final ImmutableSet.Builder<String> filesDownloaded = ImmutableSet.builder();
      try (TarArchiveInputStream tarStream = new TarArchiveInputStream(
          dockerApiVersionLessThan("1.24") ?
          sut.copyContainer(id, "/tmp") :
          sut.archiveContainer(id, "/tmp"))) {
        TarArchiveEntry entry;
        while ((entry = tarStream.getNextTarEntry()) != null) {
          filesDownloaded.add(entry.getName());
        }
      }

      // Check that we got back what we put in
      final File folder = new File(dockerDirectory);
      final File[] files = folder.listFiles();
      if (files != null) {
        for (final File file : files) {
          if (!file.isDirectory()) {
            Boolean found = false;
            for (final String fileDownloaded : filesDownloaded.build()) {
              if (fileDownloaded.contains(file.getName())) {
                found = true;
              }
            }
            assertTrue(found);
          }
        }
      }
    }

    // Kill container
    sut.killContainer(id);

    try {
      // Remove the container
      sut.removeContainer(id);
    } catch (DockerRequestException e) {
      // CircleCI doesn't let you remove a container :(
      if (!CIRCLECI) {
        // Verify that the container is gone
        exception.expect(ContainerNotFoundException.class);
        sut.inspectContainer(id);
      }
    }
  }

  @Test
  public void interruptTest() throws Exception {

    // Pull image
    sut.pull(BUSYBOX_LATEST);

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
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

  @Test(expected = DockerException.class)
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
    final CompletionService<ContainerExit> completion = new ExecutorCompletionService<>(executor);

    // Spawn and wait on many more containers than the connection pool size.
    // This should cause a timeout once the connection pool is exhausted.

    try (final DockerClient dockerClient = DefaultDockerClient.fromEnv()
        .connectionPoolSize(connectionPoolSize)
        .build()) {
      // Create container
      final ContainerConfig config = ContainerConfig.builder()
          .image(BUSYBOX_LATEST)
          .cmd("sh", "-c", "while :; do sleep 1; done")
          .build();
      final String name = randomName();
      final ContainerCreation creation = dockerClient.createContainer(config, name);
      final String id = creation.id();

      // Start the container
      dockerClient.startContainer(id);

      // Submit a bunch of waitContainer requests
      for (int i = 0; i < callableCount; i++) {
        //noinspection unchecked
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
    }
  }

  @Test
  public void testWaitContainer() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
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
    sut.pull(MEMCACHED_LATEST);
    final ContainerConfig config = ContainerConfig.builder()
        .image(MEMCACHED_LATEST)
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

    sut.pull(MEMCACHED_LATEST);
    final HostConfig hostConfig = HostConfig.builder()
        .securityOpt(userLabel, roleLabel, typeLabel, levelLabel)
        .build();
    final ContainerConfig config = ContainerConfig.builder()
        .image(MEMCACHED_LATEST)
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
    requireDockerApiVersionAtLeast("1.18", "Container creation with HostConfig");

    sut.pull(BUSYBOX_LATEST);

    final boolean privileged = true;
    final boolean publishAllPorts = true;
    final String dns = "1.2.3.4";
    final List<Ulimit> ulimits =
        Lists.newArrayList(
            Ulimit.builder()
                .name("nofile")
                .soft(1024)
                .hard(2048)
                .build()
        );
    final HostConfig expected = HostConfig.builder()
        .privileged(privileged)
        .publishAllPorts(publishAllPorts)
        .dns(dns)
        .cpuShares((long) 4096)
        .ulimits(ulimits)
        .build();

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
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
    assertThat(actual.cpuShares(), equalTo(expected.cpuShares()));
    assertEquals(ulimits, actual.ulimits());
  }

  @Test
  public void testContainerWithAppArmorLogs() throws Exception {
    requireDockerApiVersionAtLeast("1.21", "StopSignal and AppArmorProfile");

    sut.pull(BUSYBOX_LATEST);

    final boolean privileged = true;
    final boolean publishAllPorts = true;
    final String dns = "1.2.3.4";
    final HostConfig expected = HostConfig.builder().privileged(privileged)
        .publishAllPorts(publishAllPorts).dns(dns).cpuShares((long) 4096).build();

    final String stopSignal = "SIGTERM";

    final ContainerConfig config = ContainerConfig.builder().image(BUSYBOX_LATEST)
        .hostConfig(expected).stopSignal(stopSignal).build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    sut.startContainer(id);

    final ContainerInfo inspection = sut.inspectContainer(id);
    final HostConfig actual = inspection.hostConfig();

    assertThat(actual.privileged(), equalTo(expected.privileged()));
    assertThat(actual.publishAllPorts(), equalTo(expected.publishAllPorts()));
    assertThat(actual.dns(), equalTo(expected.dns()));
    assertThat(actual.cpuShares(), equalTo(expected.cpuShares()));
    assertThat(sut.inspectContainer(id).config().getStopSignal(), equalTo(config.getStopSignal()));
    assertThat(inspection.appArmorProfile(), equalTo(""));
    assertThat(inspection.execId(), equalTo(null));
    assertThat(inspection.logPath(), containsString(id + "-json.log"));
    assertThat(inspection.restartCount(), equalTo(0L));
    assertThat(inspection.mounts().isEmpty(), equalTo(true));

    // Wait for the container to exit
    sut.waitContainer(id);

    final List<Container> containers = sut.listContainers(allContainers(), withStatusExited());

    Container targetCont = null;
    for (final Container container : containers) {
      if (container.id().equals(id)) {
        targetCont = container;
        break;
      }
    }
    assertNotNull(targetCont);
    assertThat(targetCont.imageId(), equalTo(inspection.image()));
  }

  @Test
  public void testContainerWithCpuQuota() throws Exception {
    requireDockerApiVersionAtLeast("1.19", "Container Creation with HostConfig");

    assumeFalse(CIRCLECI);

    sut.pull(BUSYBOX_LATEST);

    final boolean privileged = true;
    final boolean publishAllPorts = true;
    final String dns = "1.2.3.4";
    final HostConfig expected = HostConfig.builder()
        .privileged(privileged)
        .publishAllPorts(publishAllPorts)
        .dns(dns)
        .cpuQuota((long) 50000)
        .build();

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
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
    assertThat(actual.cpuQuota(), equalTo(expected.cpuQuota()));
  }

  @Test
  public void testEventStream() throws Exception {
    requireDockerApiVersionNot("1.19", "Docker 1.7.x has a bug that breaks DockerClient.events(). "
                                       + "So we skip this test.");
    sut.pull(BUSYBOX_LATEST);
    final EventStream eventStream = sut.events();
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .build();
    final ContainerCreation container = sut.createContainer(config, randomName());
    sut.startContainer(container.id());

    assumeTrue("Sometimes there are no events, and it is unclear why.", eventStream.hasNext());

    final Event createEvent = eventStream.next();
    assertThat(createEvent.status(), equalTo("create"));
    assertThat(createEvent.id(), equalTo(container.id()));
    assertThat(createEvent.from(), startsWith("busybox:"));
    assertThat(createEvent.time(), notNullValue());

    Event startEvent = eventStream.next();
    if (dockerApiVersionAtLeast("1.22") && eventStream.hasNext()) {
      // For some reason, version 1.22 has an extra null Event sometimes. So we read the next one.
      startEvent = eventStream.next();
    }

    assertThat(startEvent.status(), equalTo("start"));
    assertThat(startEvent.id(), equalTo(container.id()));
    assertThat(startEvent.from(), startsWith("busybox:"));
    assertThat(startEvent.time(), notNullValue());

    eventStream.close();
  }

  @Test
  public void testEventStreamWithSinceTime() throws Exception {
    Thread.sleep(1000); // ensure we push to the next second
    // so we don't get events from the last test
    final Date date = new Date();
    sut.pull(BUSYBOX_LATEST);
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .build();
    final ContainerCreation container = sut.createContainer(config, randomName());
    sut.startContainer(container.id());

    final Predicate<Event> hasStatus = new Predicate<Event>() {
      @Override
      public boolean apply(final Event input) {
        return input.status() != null;
      }
    };

    try (EventStream stream = sut.events(DockerClient.EventsParam.since(date.getTime() / 1000))) {
      final Iterator<Event> events = Iterators.filter(stream, hasStatus);

      final Event pullEvent = events.next();
      assertThat(pullEvent.status(), equalTo("pull"));
      assertThat(pullEvent.id(), equalTo(BUSYBOX_LATEST));

      final Event createEvent = events.next();
      assertThat(createEvent.status(), equalTo("create"));
      assertThat(createEvent.id(), equalTo(container.id()));
      assertThat(createEvent.from(), startsWith("busybox:"));
      assertThat(createEvent.time(), notNullValue());

      final Event startEvent = events.next();
      assertThat(startEvent.status(), equalTo("start"));
      assertThat(startEvent.id(), equalTo(container.id()));
      assertThat(startEvent.from(), startsWith("busybox:"));
      assertThat(startEvent.time(), notNullValue());
    }
  }

  @Test(timeout = 5000)
  public void testEventStreamWithUntilTime() throws Exception {
    final EventStream eventStream =
        sut.events(DockerClient.EventsParam.until((new Date().getTime() + 2000) / 1000));

    while (eventStream.hasNext()) {
      eventStream.next();
    }

    eventStream.close();
  }

  @Test
  public void testListImages() throws Exception {
    sut.pull(BUSYBOX_LATEST);
    final List<Image> images = sut.listImages();
    assertThat(images.size(), greaterThan(0));

    // Verify that image contains valid values
    Image busybox = null;
    for (final Image image : images) {
      if (image.repoTags() != null && image.repoTags().contains(BUSYBOX_LATEST)) {
        busybox = image;
      }
    }
    assertNotNull(busybox);
    assertThat(busybox.virtualSize(), greaterThan(0L));
    assertThat(busybox.created(), not(isEmptyOrNullString()));
    assertThat(busybox.id(), not(isEmptyOrNullString()));
    assertThat(busybox.repoTags(), notNullValue());
    assertThat(busybox.repoTags().size(), greaterThan(0));
    assertThat(BUSYBOX_LATEST, isIn(busybox.repoTags()));
    if (dockerApiVersionLessThan("1.22")) {
      assertThat(busybox.parentId(), not(isEmptyOrNullString()));
    }

    final List<Image> imagesWithDigests = sut.listImages(digests());
    assertThat(imagesWithDigests.size(), greaterThan(0));
    busybox = null;
    for (final Image image : imagesWithDigests) {
      if (image.repoTags() != null && image.repoTags().contains(BUSYBOX_LATEST)) {
        busybox = image;
      }
    }
    assertNotNull(busybox);
    if (dockerApiVersionLessThan("1.22")) {
      assertThat(busybox.repoDigests(), notNullValue());
    }

    // Using allImages() should give us more images
    final List<Image> allImages = sut.listImages(allImages());
    assertThat(allImages.size(), greaterThan(images.size()));

    // Including just dangling images should give us fewer images
    final List<Image> danglingImages = sut.listImages(danglingImages());
    assertThat(danglingImages.size(), lessThan(images.size()));

    // Specifying both allImages() and danglingImages() should give us only dangling images
    final List<Image> allAndDanglingImages = sut.listImages(allImages(), danglingImages());
    assertThat(allAndDanglingImages.size(), equalTo(danglingImages.size()));

    // Can list by name
    final List<Image> imagesByName = sut.listImages(byName(BUSYBOX));
    assertThat(imagesByName.size(), greaterThan(0));
    final Set<String> repoTags = Sets.newHashSet();
    for (final Image imageByName : imagesByName) {
      if (imageByName.repoTags() != null) {
        repoTags.addAll(imageByName.repoTags());
      }
    }
    assertThat(BUSYBOX_LATEST, isIn(repoTags));
  }

  @Test
  public void testDockerDateFormat() throws Exception {
    // This is the created date for busybox converted from nanoseconds to milliseconds
    final Date expected = new StdDateFormat().parse("2015-09-18T17:44:28.145Z");

    // Verify the formatter works when used with the client
    sut.pull(BUSYBOX_BUILDROOT_2013_08_1);
    final ImageInfo imageInfo = sut.inspectImage(BUSYBOX_BUILDROOT_2013_08_1);
    assertThat(imageInfo.created(), equalTo(expected));
  }

  @Test(expected = DockerCertificateException.class)
  public void testBadDockerCertificates() throws Exception {
    // try building a DockerCertificates with specifying a cert path to something that
    // isn't a cert
    final Path certDir = Paths.get("src", "test", "resources", "dockerInvalidSslDirectory");
    DockerCertificates.builder().dockerCertPath(certDir).build();
  }

  @Test
  public void testNoDockerCertificatesInDir() throws Exception {
    final Path certDir = Paths.get(System.getProperty("java.io.tmpdir"));
    final Optional<DockerCertificates> result = DockerCertificates.builder()
        .dockerCertPath(certDir).build();
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void testSsl() throws Exception {
    assumeFalse(TRAVIS);

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
    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
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
  public void testExtraHosts() throws Exception {
    requireDockerApiVersionAtLeast("1.15", "Container Creation with HostConfig.ExtraHosts");

    sut.pull(BUSYBOX_LATEST);

    final HostConfig expected = HostConfig.builder()
        .extraHosts("extrahost:1.2.3.4")
        .build();

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .hostConfig(expected)
        .cmd("sh", "-c", "cat /etc/hosts | grep extrahost")
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    sut.startContainer(id);

    final HostConfig actual = sut.inspectContainer(id).hostConfig();

    assertThat(actual.extraHosts(), equalTo(expected.extraHosts()));

    final String logs;
    try (LogStream stream = sut.logs(id, stdout(), stderr())) {
      logs = stream.readFully();
    }
    assertThat(logs, containsString("1.2.3.4"));

  }

  @Test
  public void testLogDriver() throws Exception {
    requireDockerApiVersionAtLeast("1.21", "Container Creation with HostConfig.LogConfig");

    sut.pull(BUSYBOX_LATEST);
    final String name = randomName();

    final Map<String, String> logOptions = new HashMap<>();
    logOptions.put("max-size", "10k");
    logOptions.put("max-file", "2");
    logOptions.put("labels", name);

    final LogConfig logConfig = LogConfig.create("json-file", logOptions);
    assertThat(logConfig.logType(), equalTo("json-file"));
    assertThat(logConfig.logOptions(), equalTo(logOptions));

    final HostConfig expected = HostConfig.builder()
        .logConfig(logConfig)
        .build();

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .hostConfig(expected)
        .build();

    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();
    sut.startContainer(id);

    final HostConfig actual = sut.inspectContainer(id).hostConfig();

    assertThat(actual.logConfig(), equalTo(expected.logConfig()));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testContainerVolumesOldStyle() throws Exception {
    requireDockerApiVersionLessThan(
        "1.20", "Creating a container with volumes and inspecting volumes in old style");

    sut.pull(BUSYBOX_LATEST);

    final HostConfig hostConfig = HostConfig.builder()
        .binds(Bind.from("/local/path")
                   .to("/remote/path")
                   .build())
        .build();
    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .volumes("/foo")
        .hostConfig(hostConfig)
        .build();
    final String id = sut.createContainer(volumeConfig, randomName()).id();
    final ContainerInfo volumeContainer = sut.inspectContainer(id);

    final List<String> expectedDestinations = Lists.newArrayList("/foo", "/remote/path");
    final Set<String> actualDestinations = volumeContainer.volumes().keySet();

    // To make sure two sets are equal, when they may be in different orders,
    // we check that each one contains all the elements of the other.
    // Equivalent to, in math, proving two sets are one-to-one by proving
    // they are injective ("into") and surjective ("onto").
    assertThat(actualDestinations, everyItem(isIn(expectedDestinations)));
    assertThat(expectedDestinations, everyItem(isIn(actualDestinations)));

    // The local paths returned from ContainerInfo.volumes() are paths in the docker
    // file system. So they are not predictable (at least by me, the test writer,
    // John Flavin.) However, the local path we asked for will always be included as part of
    // the path that is returned. So we can just check that one in the list of items
    // we got back contains our expected path.
    final String expectedLocalPath = "/local/path";
    assertThat(volumeContainer.volumes().values(), hasItem(containsString(expectedLocalPath)));

    assertThat(volumeContainer.config().volumes(), hasItem("/foo"));
  }

  @Test
  public void testContainerVolumeNoCopy() throws Exception {
    requireDockerApiVersionAtLeast(
        "1.23", "Creating a container with volumes with nocopy mode");

    sut.pull(BUSYBOX_LATEST);

    sut.createVolume(Volume.builder().name("avolume").build());
    sut.createVolume(Volume.builder().name("avolume2").build());

    final Bind bind1 =
        Bind.from("avolume")
            .to("/some/other/path")
            .readOnly(true)
            .build();

    final Bind bind2 =
        Bind.from("avolume2")
            .to("/some/other/path2")
            .noCopy(true)
            .build();

    final HostConfig hostConfig = HostConfig.builder()
        .appendBinds(bind1, bind2)
        .build();

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .hostConfig(hostConfig)
        .build();

    final String id = sut.createContainer(config, randomName()).id();
    final ContainerInfo info = sut.inspectContainer(id);

    final List<ContainerMount> mounts = info.mounts();

    assertThat(mounts.size(), equalTo(2));

    assertThat(Iterables.find(mounts, new Predicate<ContainerMount>() {
      @Override
      public boolean apply(ContainerMount mount) {
        return mount.source().contains("/avolume/")
               && "/some/other/path".equals(mount.destination())
               && !mount.rw();
      }
    }, null), notNullValue());

    assertThat(Iterables.find(mounts, new Predicate<ContainerMount>() {
      @Override
      public boolean apply(ContainerMount mount) {
        return mount.source().contains("/avolume2/")
               && "/some/other/path2".equals(mount.destination())
               && mount.rw()
               && "nocopy".equals(mount.mode());
      }
    }, null), notNullValue());

  }

  @Test
  public void testContainerVolumes() throws Exception {
    requireDockerApiVersionAtLeast(
        "1.20", "Creating a container with volumes and inspecting volumes in new style");

    sut.pull(BUSYBOX_LATEST);

    final Bind bind =
        Bind.from("/some/path")
            .to("/some/other/path")
            .readOnly(true)
            .build();
    final HostConfig hostConfig = HostConfig.builder()
        .appendBinds("/local/path:/remote/path")
        .appendBinds(bind)
        .build();
    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .volumes("/foo")
        .hostConfig(hostConfig)
        .build();
    final String id = sut.createContainer(volumeConfig, randomName()).id();
    final ContainerInfo volumeContainer = sut.inspectContainer(id);
    final List<ContainerMount> containerMounts = volumeContainer.mounts();

    final List<String> expectedDesintations =
        Lists.newArrayList("/foo", "/remote/path", "/some/other/path");
    final List<String> actualDesintations =
        Lists.transform(Lists.newArrayList(containerMounts),
                        new Function<ContainerMount, String>() {
                          @Override
                          public String apply(ContainerMount containerMount) {
                            return containerMount.destination();
                          }
                        });
    assertThat(expectedDesintations, everyItem(isIn(actualDesintations)));

    final List<String> expectedSources =
        Lists.newArrayList("/local/path", "/some/path");
    final List<String> actualSources =
        Lists.transform(Lists.newArrayList(containerMounts),
                        new Function<ContainerMount, String>() {
                          @Override
                          public String apply(ContainerMount containerMount) {
                            return containerMount.source();
                          }
                        });
    assertThat(expectedSources, everyItem(isIn(actualSources)));

    assertThat(volumeContainer.config().volumes(), hasItem("/foo"));
  }

  @Test
  public void testVolumesFrom() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final String volumeContainer = randomName();
    final String mountContainer = randomName();

    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
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
        .image(BUSYBOX_LATEST)
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
    try (LogStream stream = sut.logs(info.id(), stdout(), stderr())) {
      logs = stream.readFully();
    }
    assertThat(logs, containsString("bar"));
  }

  @Test
  public void testAttachContainer() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final String volumeContainer = randomName();

    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .volumes("/foo")
        // TODO (mbrown): remove sleep - added to make sure container is still alive when attaching
        //.cmd("ls", "-la")
        .cmd("sh", "-c", "ls -la; sleep 3")
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

  @Test
  public void testLogNoTimeout() throws Exception {
    final String volumeContainer = createSleepingContainer();
    final StringBuffer result = new StringBuffer();
    try (final LogStream stream = sut.logs(volumeContainer, stdout(), stderr(), follow())) {
      try {
        while (stream.hasNext()) {
          final String r = UTF_8.decode(stream.next().content()).toString();
          log.info(r);
          result.append(r);
        }
      } catch (Exception e) {
        log.info(e.getMessage());
      }
    }
    verifyNoTimeoutContainer(volumeContainer, result);
  }

  @Test
  public void testAttachLogNoTimeout() throws Exception {
    final String volumeContainer = createSleepingContainer();
    final StringBuffer result = new StringBuffer();
    try (final LogStream stream = sut.attachContainer(
        volumeContainer, AttachParameter.STDOUT, AttachParameter.STDERR,
        AttachParameter.STREAM, AttachParameter.STDIN)) {
      try {
        while (stream.hasNext()) {
          final String r = UTF_8.decode(stream.next().content()).toString();
          log.info(r);
          result.append(r);
        }
      } catch (Exception e) {
        log.info(e.getMessage());
      }
    }
    verifyNoTimeoutContainer(volumeContainer, result);
  }

  @Test
  public void testLogsNoStdOut() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final String container = randomName();

    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("sh", "-c",
             "echo This message goes to stdout && echo This message goes to stderr 1>&2")
        .build();
    sut.createContainer(volumeConfig, container);
    sut.startContainer(container);
    sut.waitContainer(container);

    final ContainerInfo info = sut.inspectContainer(container);
    assertThat(info.state().running(), is(false));
    assertThat(info.state().exitCode(), is(0));

    final String logs;
    try (LogStream stream = sut.logs(info.id(), stdout(false), stderr())) {
      logs = stream.readFully();
    }
    assertThat(logs, containsString("This message goes to stderr"));
    assertThat(logs, not(containsString("This message goes to stdout")));
  }

  @Test
  public void testLogsNoStdErr() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final String container = randomName();

    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("sh", "-c",
             "echo This message goes to stdout && echo This message goes to stderr 1>&2")
        .build();
    sut.createContainer(volumeConfig, container);
    sut.startContainer(container);
    sut.waitContainer(container);

    final ContainerInfo info = sut.inspectContainer(container);
    assertThat(info.state().running(), is(false));
    assertThat(info.state().exitCode(), is(0));

    final String logs;
    try (LogStream stream = sut.logs(info.id(), stdout(), stderr(false))) {
      logs = stream.readFully();
    }
    assertThat(logs, containsString("This message goes to stdout"));
    assertThat(logs, not(containsString("This message goes to stderr")));
  }

  @Test
  public void testLogsTimestamps() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final String container = randomName();

    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("echo", "This message should have a timestamp")
        .build();
    sut.createContainer(volumeConfig, container);
    sut.startContainer(container);
    sut.waitContainer(container);

    final ContainerInfo info = sut.inspectContainer(container);
    assertThat(info.state().running(), is(false));
    assertThat(info.state().exitCode(), is(0));

    final String logs;
    try (LogStream stream = sut.logs(info.id(), stdout(), stderr(), timestamps())) {
      logs = stream.readFully();
    }

    final Pattern timestampPattern = Pattern.compile(
        "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z.*$", Pattern.DOTALL);
    assertTrue(timestampPattern.matcher(logs).matches());
    assertThat(logs, containsString("This message should have a timestamp"));
  }

  @Test
  public void testLogsTail() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final String container = randomName();

    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("sh", "-c", "echo 1 && echo 2 && echo 3 && echo 4")
        .build();
    sut.createContainer(volumeConfig, container);
    sut.startContainer(container);
    sut.waitContainer(container);

    final ContainerInfo info = sut.inspectContainer(container);
    assertThat(info.state().running(), is(false));
    assertThat(info.state().exitCode(), is(0));

    final String logs;
    try (LogStream stream = sut.logs(info.id(), stdout(), stderr(), tail(2))) {
      logs = stream.readFully();
    }

    assertThat(logs, not(containsString("1")));
    assertThat(logs, not(containsString("2")));
    assertThat(logs, containsString("3"));
    assertThat(logs, containsString("4"));
  }

  @Test
  public void testLogsSince() throws Exception {
    requireDockerApiVersionAtLeast("1.19", "/logs?since=timestamp");

    sut.pull(BUSYBOX_LATEST);

    final String container = randomName();

    final ContainerConfig volumeConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("echo", "This was printed too late")
        .build();
    sut.createContainer(volumeConfig, container);
    sut.startContainer(container);
    sut.waitContainer(container);

    final ContainerInfo info = sut.inspectContainer(container);
    assertThat(info.state().running(), is(false));
    assertThat(info.state().exitCode(), is(0));

    final String logs;
    // Get logs since the current timestamp. This should return nothing.
    try (LogStream stream = sut.logs(info.id(), stdout(), stderr(),
                                     since((int) (System.currentTimeMillis() / 1000L)))) {
      logs = stream.readFully();
    }

    assertThat(logs, not(containsString("This message was printed too late")));
  }

  @Test
  public void testLogsTty() throws DockerException, InterruptedException {
    final String container = randomName();
    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .attachStdout(true)
        .tty(true)
        .cmd("sh", "-c", "ls")
        .build();

    sut.createContainer(containerConfig, container);
    sut.startContainer(container);
    final LogStream logStream = sut.logs(container, DockerClient.LogsParam.stdout());

    while (logStream.hasNext()) {
      final String line = UTF_8.decode(logStream.next().content()).toString();
      log.info(line);
    }
    sut.waitContainer(container);
    final ContainerInfo info = sut.inspectContainer(container);
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
    sut.createContainer(config, name);
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
  public void testExec() throws Exception {
    requireDockerApiVersionAtLeast("1.15", "Exec");
    assumeThat("Only native (libcontainer) driver supports Exec",
               sut.info().executionDriver(), startsWith("native"));

    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        // make sure the container's busy doing something upon startup
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    sut.startContainer(containerId);

    final ExecCreation execCreation = sut.execCreate(
        containerId,
        new String[] {"ls", "-la"},
        ExecCreateParam.attachStdout(),
        ExecCreateParam.attachStderr());
    final String execId = execCreation.id();

    log.info("execId = {}", execId);

    try (final LogStream stream = sut.execStart(execId)) {
      final String output = stream.readFully();
      log.info("Result:\n{}", output);
      assertThat(output, containsString("total"));
    }
  }

  @Test
  public void testExecInspect() throws Exception {
    requireDockerApiVersionAtLeast("1.16", "Exec Inspect");
    assumeThat("Only native (libcontainer) driver supports Exec",
               sut.info().executionDriver(), startsWith("native"));

    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    sut.startContainer(containerId);

    final List<ExecCreateParam> createParams = Lists.newArrayList(
        ExecCreateParam.attachStdout(),
        ExecCreateParam.attachStderr(),
        ExecCreateParam.attachStdin(),
        ExecCreateParam.tty());

    // some functionality in this test depends on API 1.19 (exec user)
    final boolean execUserSupported = dockerApiVersionAtLeast("1.19");
    if (execUserSupported) {
      createParams.add(ExecCreateParam.user("1000"));
    }

    final ExecCreation execCreation = sut.execCreate(
        containerId, new String[] {"sh", "-c", "exit 2"},
        createParams.toArray(new ExecCreateParam[createParams.size()]));
    final String execId = execCreation.id();

    log.info("execId = {}", execId);
    try (final LogStream stream = sut.execStart(execId)) {
      stream.readFully();
    }

    final ExecState state = sut.execInspect(execId);
    assertThat(state.id(), is(execId));
    assertThat(state.running(), is(false));
    assertThat(state.exitCode(), is(2));
    assertThat(state.openStdin(), is(true));
    assertThat(state.openStderr(), is(true));
    assertThat(state.openStdout(), is(true));

    final ProcessConfig processConfig = state.processConfig();
    assertThat(processConfig.privileged(), is(false));
    if (execUserSupported) {
      assertThat(processConfig.user(), is("1000"));
    }
    assertThat(processConfig.tty(), is(true));
    assertThat(processConfig.entrypoint(), is("sh"));
    assertThat(processConfig.arguments(),
               Matchers.<List<String>>is(ImmutableList.of("-c", "exit 2")));

    if (dockerApiVersionLessThan("1.22")) {
      final ContainerInfo containerInfo = state.container();
      assertThat(containerInfo.path(), is("sh"));
      assertThat(containerInfo.args(),
                 Matchers.<List<String>>is(ImmutableList.of("-c", "while :; do sleep 1; done")));
      assertThat(containerInfo.config().image(), is(BUSYBOX_LATEST));
    } else {
      assertNotNull(state.containerID(), "containerID");
    }
  }

  @Test
  public void testListContainers() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final String label = "foo";
    final String labelValue = "bar";

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .labels(ImmutableMap.of(label, labelValue))
        .build();
    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    // filters={"status":["created"]}
    // can only filter by created status in docker API version >= 1.20 - the status of "created"
    // did not exist in docker prior to 1.8.0
    final DockerClient.ListContainersParam[] createdParams =
        dockerApiVersionAtLeast("1.20")
        ? new DockerClient.ListContainersParam[] {allContainers(), withStatusCreated()}
        : new DockerClient.ListContainersParam[] {allContainers()};

    final List<Container> created = sut.listContainers(createdParams);
    assertThat("listContainers is unexpectedly empty", created, not(empty()));
    assertThat(containerId, isIn(containersToIds(created)));

    // filters={"status":["running"]}
    sut.startContainer(containerId);
    final List<Container> running = sut.listContainers(withStatusRunning());
    assertThat(containerId, isIn(containersToIds(running)));

    // filters={"status":["paused"]}
    sut.pauseContainer(containerId);
    final List<Container> paused = sut.listContainers(withStatusPaused());
    assertThat(containerId, isIn(containersToIds(paused)));

    // filters={"status":["exited"]}
    sut.unpauseContainer(containerId);
    sut.stopContainer(containerId, 0);
    final List<Container> allExited = sut.listContainers(allContainers(), withStatusExited());
    assertThat(containerId, isIn(containersToIds(allExited)));

    // filters={"status":["created","paused","exited"]}
    // Will work, i.e. multiple "status" filters are ORed
    final List<Container> multipleStati = sut.listContainers(
        allContainers(),
        withStatusCreated(),
        withStatusPaused(),
        withStatusExited());
    assertThat(containerId, isIn(containersToIds(multipleStati)));

    // filters={"status":["exited"],"labels":["foo=bar"]}
    // Shows that labels play nicely with other filters
    final List<Container> statusAndLabels = sut.listContainers(
        allContainers(),
        withStatusExited(),
        withLabel(label, labelValue));
    assertThat(containerId, isIn(containersToIds(statusAndLabels)));

    if (dockerApiVersionAtLeast("1.21")) {
      for (final Container c : running) {
        assertThat(c.imageId(), is(notNullValue()));
      }
    }

    if (dockerApiVersionAtLeast("1.22")) {
      for (final Container c : running) {
        assertThat(c.networkSettings(), is(notNullValue()));
      }
    }

    if (dockerApiVersionAtLeast("1.23")) {
      for (final Container c : running) {
        assertThat(c.state(), equalTo("running"));
        assertThat(c.mounts(), is(notNullValue()));
      }
    }
  }

  @Test
  public void testContainerLabels() throws Exception {
    requireDockerApiVersionAtLeast("1.18", "labels");
    sut.pull(BUSYBOX_LATEST);

    final Map<String, String> labels = ImmutableMap.of(
        "name", "starship", "foo", "bar"
    );

    // Create container
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .labels(labels)
        .cmd("sleep", "1000")
        .build();
    final String name = randomName();
    final ContainerCreation creation = sut.createContainer(config, name);
    final String id = creation.id();

    // Start the container
    sut.startContainer(id);

    final ContainerInfo containerInfo = sut.inspectContainer(id);
    assertThat(containerInfo.config().labels(), is(labels));

    final Map<String, String> labels2 = ImmutableMap.of(
        "name", "starship", "foo", "baz"
    );

    // Create second container with different labels
    final ContainerConfig config2 = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .labels(labels2)
        .cmd("sleep", "1000")
        .build();
    final String name2 = randomName();
    final ContainerCreation creation2 = sut.createContainer(config2, name2);
    final String id2 = creation2.id();

    // Start the second container
    sut.startContainer(id2);

    final ContainerInfo containerInfo2 = sut.inspectContainer(id2);
    assertThat(containerInfo2.config().labels(), is(labels2));

    // Check that both containers are listed when we filter with a "name" label
    final List<Container> containers =
        sut.listContainers(withLabel("name"));
    final List<String> ids = containersToIds(containers);
    assertThat(ids.size(), equalTo(2));
    assertThat(ids, containsInAnyOrder(id, id2));

    // Check that the first container is listed when we filter with a "foo=bar" label
    final List<Container> barContainers =
        sut.listContainers(withLabel("foo", "bar"));
    final List<String> barIds = containersToIds(barContainers);
    assertThat(barIds.size(), equalTo(1));
    assertThat(barIds, contains(id));

    // Check that the second container is listed when we filter with a "foo=baz" label
    final List<Container> bazContainers =
        sut.listContainers(withLabel("foo", "baz"));
    final List<String> bazIds = containersToIds(bazContainers);
    assertThat(bazIds.size(), equalTo(1));
    assertThat(bazIds, contains(id2));

    // Check that no containers are listed when we filter with a "foo=qux" label
    final List<Container> quxContainers =
        sut.listContainers(withLabel("foo", "qux"));
    assertThat(quxContainers.size(), equalTo(0));
  }

  @Test
  public void testImageLabels() throws Exception {
    requireDockerApiVersionAtLeast("1.17", "image labels");

    final String dockerDirectory =
        Resources.getResource("dockerDirectoryWithImageLabels").getPath();

    // Create test images
    final String barDir = (new File(dockerDirectory, "barDir")).toString();
    final String barName = randomName();
    final String barId = sut.build(Paths.get(barDir), barName);

    final String bazName = randomName();
    final String bazDir = (new File(dockerDirectory, "bazDir")).toString();
    final String bazId = sut.build(Paths.get(bazDir), bazName);

    // Check that both test images are listed when we filter with a "name" label
    final List<Image> nameImages = sut.listImages(
        ListImagesParam.withLabel("name"));
    final List<String> nameIds =
        dockerApiVersionLessThan("1.22") ?
        imagesToShortIds(nameImages) :
        imagesToShortIdsAndRemoveSha256(nameImages);

    assertThat(barId, isIn(nameIds));
    assertThat(bazId, isIn(nameIds));

    // Check that the first image is listed when we filter with a "foo=bar" label
    final List<Image> barImages = sut.listImages(
        ListImagesParam.withLabel("foo", "bar"));
    final List<String> barIds =
        dockerApiVersionLessThan("1.22") ?
        imagesToShortIds(barImages) :
        imagesToShortIdsAndRemoveSha256(barImages);
    assertThat(barId, isIn(barIds));

    // Check that we find the first image again when searching with the full
    // set of labels in a Map
    final List<Image> barImages2 = sut.listImages(
        ListImagesParam.withLabel("foo", "bar"),
        ListImagesParam.withLabel("name", "testtesttest"));
    final List<String> barIds2 =
        dockerApiVersionLessThan("1.22") ?
        imagesToShortIds(barImages2) :
        imagesToShortIdsAndRemoveSha256(barImages2);
    assertThat(barId, isIn(barIds2));

    // Check that the second image is listed when we filter with a "foo=baz" label
    final List<Image> bazImages = sut.listImages(
        ListImagesParam.withLabel("foo", "baz"));
    final List<String> bazIds =
        dockerApiVersionLessThan("1.22") ?
        imagesToShortIds(bazImages) :
        imagesToShortIdsAndRemoveSha256(bazImages);
    assertThat(bazId, isIn(bazIds));

    // Check that no containers are listed when we filter with a "foo=qux" label
    final List<Image> quxImages = sut.listImages(
        ListImagesParam.withLabel("foo", "qux"));
    assertThat(quxImages, hasSize(0));

    // Clean up test images
    sut.removeImage(barName, true, true);
    sut.removeImage(bazName, true, true);
  }

  @Test
  public void testMacAddress() throws Exception {
    requireDockerApiVersionAtLeast("1.18", "Mac Address");

    sut.pull(MEMCACHED_LATEST);
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("sleep", "1000")
        .macAddress("12:34:56:78:9a:bc")
        .build();
    final ContainerCreation container = sut.createContainer(config, randomName());
    sut.startContainer(container.id());
    final ContainerInfo containerInfo = sut.inspectContainer(container.id());
    assertThat(containerInfo, notNullValue());
    assertThat(containerInfo.config().macAddress(), equalTo("12:34:56:78:9a:bc"));
  }

  @Test
  public void testStats() throws Exception {
    requireDockerApiVersionAtLeast("1.19", "stats without streaming");

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .build();
    final ContainerCreation container = sut.createContainer(config, randomName());
    sut.startContainer(container.id());

    final ContainerStats stats = sut.stats(container.id());
    assertThat(stats.read(), notNullValue());
    assertThat(stats.precpuStats(), notNullValue());
    assertThat(stats.cpuStats(), notNullValue());
    assertThat(stats.memoryStats(), notNullValue());
    assertThat(stats.blockIoStats(), notNullValue());
    if (dockerApiVersionLessThan("1.21")) {
      assertThat(stats.network(), notNullValue());
    } else {
      assertThat(stats.networks(), notNullValue());
    }
  }

  @Test
  public void testNetworks() throws Exception {
    requireDockerApiVersionAtLeast("1.21", "createNetwork and listNetworks");

    assumeFalse(CIRCLECI);

    final String networkName = randomName();
    final Ipam ipam =
        Ipam.builder().driver("default").config("192.168.0.0/24", "192.168.0.0/24", "192.168.0.1")
            .build();
    final NetworkConfig networkConfig =
        NetworkConfig.builder().name(networkName).driver("bridge").checkDuplicate(true).ipam(ipam)
            .build();

    final NetworkCreation networkCreation = sut.createNetwork(networkConfig);
    assertThat(networkCreation.id(), is(notNullValue()));
    assertThat(networkCreation.warnings(), is(nullValue()));

    final List<Network> networks = sut.listNetworks();
    assertTrue(networks.size() > 0);

    Network network = null;
    for (final Network n : networks) {
      if (n.name().equals(networkName)) {
        network = n;
      }
    }
    assertThat(network, is(notNullValue()));
    //noinspection ConstantConditions
    assertThat(network.id(), is(notNullValue()));
    assertThat(sut.inspectNetwork(network.id()).name(), is(networkName));
    assertThat(network.ipam(), equalTo(ipam));

    sut.removeNetwork(network.id());

    exception.expect(NetworkNotFoundException.class);
    sut.inspectNetwork(network.id());

  }

  @Test
  public void testNetworksConnectContainer() throws Exception {
    requireDockerApiVersionAtLeast("1.21", "createNetwork and listNetworks");

    assumeFalse(CIRCLECI);
    final String networkName = randomName();
    final String containerName = randomName();
    final NetworkCreation networkCreation =
        sut.createNetwork(NetworkConfig.builder().name(networkName).build());
    assertThat(networkCreation.id(), is(notNullValue()));
    final ContainerConfig containerConfig =
        ContainerConfig.builder().image(BUSYBOX_LATEST).cmd("sh", "-c", "while :; do sleep 1; done")
            .build();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    assertThat(containerCreation.id(), is(notNullValue()));
    sut.startContainer(containerCreation.id());
    sut.connectToNetwork(containerCreation.id(), networkCreation.id());
    Network network = sut.inspectNetwork(networkCreation.id());
    assertThat(network.containers().size(), equalTo(1));
    assertThat(network.containers().get(containerCreation.id()), notNullValue());

    final ContainerInfo containerInfo = sut.inspectContainer(containerCreation.id());
    assertThat(containerInfo.networkSettings().networks().size(), is(2));
    final AttachedNetwork attachedNetwork =
        containerInfo.networkSettings().networks().get(networkName);
    assertThat(attachedNetwork, is(notNullValue()));
    if (dockerApiVersionAtLeast("1.22")) {
      assertThat(attachedNetwork.networkId(), is(notNullValue()));
    }
    assertThat(attachedNetwork.endpointId(), is(notNullValue()));
    assertThat(attachedNetwork.gateway(), is(notNullValue()));
    assertThat(attachedNetwork.ipAddress(), is(notNullValue()));
    assertThat(attachedNetwork.ipPrefixLen(), is(notNullValue()));
    assertThat(attachedNetwork.macAddress(), is(notNullValue()));
    assertThat(attachedNetwork.ipv6Gateway(), is(notNullValue()));
    assertThat(attachedNetwork.globalIPv6Address(), is(notNullValue()));
    assertThat(attachedNetwork.globalIPv6PrefixLen(), greaterThanOrEqualTo(0));

    sut.disconnectFromNetwork(containerCreation.id(), networkCreation.id());
    network = sut.inspectNetwork(networkCreation.id());
    assertThat(network.containers().size(), equalTo(0));

    sut.stopContainer(containerCreation.id(), 1);
    sut.removeContainer(containerCreation.id());
    sut.removeNetwork(networkCreation.id());

  }

  @Test
  public void testRestartPolicyAlways() throws Exception {
    testRestartPolicy(HostConfig.RestartPolicy.always());
  }

  @Test
  public void testRestartUnlessStopped() throws Exception {
    testRestartPolicy(HostConfig.RestartPolicy.unlessStopped());
  }

  @Test
  public void testRestartOnFailure() throws Exception {
    testRestartPolicy(HostConfig.RestartPolicy.onFailure(5));
  }

  @Test
  public void testRenameContainer() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final String originalName = randomName();
    final String newName = randomName();

    // Create a container with originalName
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .build();
    final ContainerCreation creation = sut.createContainer(config, originalName);
    final String id = creation.id();
    assertThat(sut.inspectContainer(id).name(), equalToIgnoreLeadingSlash(originalName));

    // Rename to newName
    sut.renameContainer(id, newName);
    assertThat(sut.inspectContainer(id).name(), equalToIgnoreLeadingSlash(newName));

    // We should no longer find a container with originalName
    try {
      sut.inspectContainer(originalName);
      fail("There should be no container with name " + originalName);
    } catch (ContainerNotFoundException e) {
      // Note, even though property in ContainerNotFoundException is named containerId,
      // in this case it holds the name, since that is what we passed to inspectContainer.
      assertThat(e.getContainerId(), equalToIgnoreLeadingSlash(originalName));
    }

    // Try to rename to a disallowed name (not matching /?[a-zA-Z0-9_-]+).
    // Should get IllegalArgumentException.
    final String badName = "abc123.!*";
    try {
      sut.renameContainer(id, badName);
      fail("We should not be able to rename a container " + badName);
    } catch (IllegalArgumentException ignored) {
      // Pass
    }

    // Try to rename to null
    try {
      sut.renameContainer(id, null);
      fail("We should not be able to rename a container null");
    } catch (IllegalArgumentException ignored) {
      // Pass
    }

    // Create another container with originalName
    final ContainerConfig config2 = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .build();
    final ContainerCreation creation2 = sut.createContainer(config2, originalName);
    final String id2 = creation2.id();
    assertThat(sut.inspectContainer(id2).name(), equalToIgnoreLeadingSlash(originalName));

    // Try to rename another container to newName. Should get a ContainerRenameConflictException.
    try {
      sut.renameContainer(id2, newName);
      fail("We should not be able to rename container " + id2 + " to " + newName);
    } catch (ContainerRenameConflictException e) {
      assertThat(e.getContainerId(), equalTo(id2));
      assertThat(e.getNewName(), equalToIgnoreLeadingSlash(newName));
    } catch (DockerRequestException ignored) {
      // This is a docker bug. It responds with HTTP 500 when it should be HTTP 409.
      // See https://github.com/docker/docker/issues/21016.
      // Fixed in version 1.11.0, so we should see a lower version.
      assertThat(compareVersion(sut.version().version(), "1.11.0"), lessThan(0));
    }

    // Rename a non-existent id. Should get ContainerNotFoundException.
    final String badId = "no_container_with_this_id_should_exist_otherwise_things_are_weird";
    try {
      sut.renameContainer(badId, randomName());
      fail("There should be no container with id " + badId);
    } catch (ContainerNotFoundException e) {
      assertThat(e.getContainerId(), equalTo(badId));
    }
  }

  @Test
  public void testInspectContainerChanges() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("/bin/sh", "-c", "echo foo > /tmp/foo.txt")
        .build();
    final ContainerCreation creation = sut.createContainer(config, randomName());
    final String id = creation.id();
    sut.startContainer(id);

    final ContainerChange expected = new ContainerChange();
    expected.kind(1);
    expected.path("/tmp/foo.txt");

    assertThat(expected, isIn(sut.inspectContainerChanges(id)));
  }

  @Test
  public void testResizeTty() throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("/bin/sh", "-c", "while :; do sleep 1; done")
        .build();
    final ContainerCreation creation = sut.createContainer(config, randomName());
    final String id = creation.id();

    try {
      sut.resizeTty(id, 100, 0);
      fail("Should get an exception resizing TTY with width=0");
    } catch (BadParamException e) {
      final Map<String, String> params = e.getParams();
      assertThat(params, hasKey("w"));
      assertEquals("0", params.get("w"));
    }

    try {
      sut.resizeTty(id, 100, 80);
      fail("Should get an exception resizing TTY for non-running container");
    } catch (DockerRequestException e) {
      if (dockerApiVersionLessThan("1.20")) {
        assertEquals(
            String.format("Cannot resize container %s, container is not running\n", id),
            e.message());
      } else if (dockerApiVersionLessThan("1.24")) {
        assertEquals(String.format("Container %s is not running\n", id),
                     e.message());
      } else {
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> jsonMessage =
            mapper.readValue(e.message(), new TypeReference<Map<String, String>>() {
            });
        assertThat(jsonMessage, hasKey("message"));
        assertEquals(String.format("Container %s is not running", id),
                     jsonMessage.get("message"));
      }
    }

    sut.startContainer(id);

    sut.resizeTty(id, 100, 80);

    // We didn't get an exception, so everything went fine
  }

  @Test
  public void testHistory() throws Exception {
    sut.pull(BUSYBOX_LATEST);
    final List<ImageHistory> imageHistoryList = sut.history(BUSYBOX_LATEST);
    assertThat(imageHistoryList, hasSize(2));

    final ImageHistory busyboxHistory = imageHistoryList.get(0);
    assertThat(busyboxHistory.id(), not(isEmptyOrNullString()));
    assertNotNull(busyboxHistory.created());
    assertThat(busyboxHistory.createdBy(), startsWith("/bin/sh -c #(nop)"));
    assertThat(BUSYBOX_LATEST, isIn(busyboxHistory.tags()));
    assertEquals(0L, busyboxHistory.size().longValue());
    assertThat(busyboxHistory.comment(), isEmptyOrNullString());
  }

  @Test
  public void testCreateVolume() throws Exception {
    requireDockerApiVersionAtLeast("1.21", "volumes");

    // Create bare volume
    final Volume blankVolume = sut.createVolume();
    assertThat(blankVolume, not(nullValue()));
    sut.removeVolume(blankVolume);

    // Create volume with attributes
    final ImmutableMap<String, String> labels = ImmutableMap.of("foo", "bar");
    final String volName = randomName();
    final Volume toCreate;
    if (dockerApiVersionLessThan("1.23")) {
      toCreate = Volume.builder()
          .name(volName)
          .driver("local")
          .build();
    } else {
      toCreate = Volume.builder()
          .name(volName)
          .driver("local")
          .labels(labels)
          .build();
    }
    final Volume created = sut.createVolume(toCreate);
    assertEquals(toCreate.name(), created.name());
    assertEquals(toCreate.driver(), created.driver());
    assertEquals(toCreate.driverOpts(), created.driverOpts());

    // mountpoint gets set by server regardless of whatever we ask for
    assertNotEquals(toCreate.mountpoint(), created.mountpoint());

    if (dockerApiVersionAtLeast("1.23")) {
      assertEquals(labels, created.labels());
    }

    if (dockerApiVersionAtLeast("1.24")) {
      assertEquals("local", created.scope());
    }

    sut.removeVolume(created);
  }

  @Test
  public void testInspectVolume() throws Exception {
    requireDockerApiVersionAtLeast("1.21", "volumes");
    final Volume volume = sut.createVolume();

    assertEquals(volume, sut.inspectVolume(volume.name()));
    sut.removeVolume(volume);

    final String badVolumeName = "this-is-a-very-unlikely-volume-name";

    exception.expect(VolumeNotFoundException.class);
    exception.expect(volumeNotFoundExceptionWithName(badVolumeName));
    sut.inspectVolume(badVolumeName);
  }

  private static Matcher<VolumeNotFoundException>
  volumeNotFoundExceptionWithName(final String volumeName) {
    final String description = "for volume name " + volumeName;
    return new CustomTypeSafeMatcher<VolumeNotFoundException>(description) {
      @Override
      protected boolean matchesSafely(final VolumeNotFoundException e) {
        return e.getVolumeName().equals(volumeName);
      }
    };
  }

  @Test
  public void testListVolumes() throws Exception {
    requireDockerApiVersionAtLeast("1.21", "volumes");
    final Volume volume = sut.createVolume();
    final String volumeName = volume.name();
    final String volumeDriver = volume.driver();

    final VolumeList volumeList = sut.listVolumes();
    if (volumeList.warnings() != null && volumeList.warnings().isEmpty()) {
      for (final String warning : volumeList.warnings()) {
        log.warn(warning);
      }
    }
    assertThat(volume, isIn(volumeList.volumes()));

    final VolumeList volumeListWithDangling = sut.listVolumes(dangling());
    if (volumeListWithDangling.warnings() != null &&
        !volumeListWithDangling.warnings().isEmpty()) {
      for (final String warning : volumeListWithDangling.warnings()) {
        log.warn(warning);
      }
    }
    assertThat(volume, isIn(volumeListWithDangling.volumes()));

    if (dockerApiVersionAtLeast("1.24")) {
      final VolumeList volumeListByName = sut.listVolumes(name(volumeName));
      if (volumeListByName.warnings() != null &&
          !volumeListByName.warnings().isEmpty()) {
        for (final String warning : volumeListByName.warnings()) {
          log.warn(warning);
        }
      }
      assertThat(volume, isIn(volumeListByName.volumes()));

      final VolumeList volumeListByDriver = sut.listVolumes(driver(volumeDriver));
      if (volumeListByDriver.warnings() != null &&
          !volumeListByDriver.warnings().isEmpty()) {
        for (final String warning : volumeListByDriver.warnings()) {
          log.warn(warning);
        }
      }
      assertThat(volume, isIn(volumeListByDriver.volumes()));
    }

    if (dockerApiVersionAtLeast("1.24")) {
      assertEquals("local", volume.scope());
      assertThat(volume.status(), is(anything())); // I don't know what is in the status object - JF
    }

    sut.removeVolume(volume);
  }

  @Test
  public void testRemoveVolume() throws Exception {
    requireDockerApiVersionAtLeast("1.21", "volumes");
    // Create a volume and remove it
    final Volume volume1 = sut.createVolume();
    sut.removeVolume(volume1);

    // Remove non-existent volume
    exception.expect(VolumeNotFoundException.class);
    exception.expect(volumeNotFoundExceptionWithName(volume1.name()));
    sut.removeVolume(volume1);

    // Create a volume, assign it to a container, and try to remove it.
    // Should get a ConflictException.
    final Volume volume2 = sut.createVolume();
    final HostConfig hostConfig = HostConfig.builder()
        .binds(Bind.from(volume2).to("/tmp").build())
        .build();
    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX)
        .hostConfig(hostConfig)
        .build();
    final ContainerCreation container = sut.createContainer(config, randomName());

    exception.expect(ConflictException.class);
    sut.removeVolume(volume2);

    // Clean up
    sut.removeContainer(container.id());
    sut.removeVolume(volume2);
  }

  private static Matcher<String> equalToIgnoreLeadingSlash(final String expected) {
    final String description = "a String equal to " + expected + ", ignoring any leading '/'";
    return new CustomTypeSafeMatcher<String>(description) {
      @Override
      protected boolean matchesSafely(final String actual) {
        return actual.startsWith("/")
               ? actual.substring(1).equals(expected)
               : actual.equals(expected);
      }
    };
  }

  private void testRestartPolicy(HostConfig.RestartPolicy restartPolicy) throws Exception {
    sut.pull(BUSYBOX_LATEST);

    final HostConfig hostConfig = HostConfig.builder()
        .restartPolicy(restartPolicy)
        .build();

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        // make sure the container's busy doing something upon startup
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .hostConfig(hostConfig)
        .build();

    final String containerName = randomName();
    final ContainerCreation containerCreation = sut.createContainer(containerConfig, containerName);
    final String containerId = containerCreation.id();

    final ContainerInfo info = sut.inspectContainer(containerId);

    assertThat(info.hostConfig().restartPolicy().name(), is(restartPolicy.name()));
    final Integer retryCount = restartPolicy.maxRetryCount() == null ?
                               0 : restartPolicy.maxRetryCount();

    assertThat(info.hostConfig().restartPolicy().maxRetryCount(), is(retryCount));
  }

  @Test
  public void testIpcMode() throws Exception {
    requireDockerApiVersionAtLeast("1.18", "IpcMode");

    final HostConfig hostConfig = HostConfig.builder()
        .ipcMode("host")
        .build();

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .cmd("sh", "-c", "while :; do sleep 1; done")
        .hostConfig(hostConfig)
        .build();

    final ContainerCreation container = sut.createContainer(config, randomName());
    final String containerId = container.id();
    sut.startContainer(containerId);

    final ContainerInfo info = sut.inspectContainer(containerId);

    assertThat(info.hostConfig().ipcMode(), is("host"));
  }

  @Test
  public void testShmSize() throws Exception {
    requireDockerApiVersionAtLeast("1.22", "ShmSize");

    // Pull image
    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .hostConfig(HostConfig.builder()
                        .shmSize(10000000L)
                        .build())
        .build();

    final ContainerCreation container = sut.createContainer(config, randomName());
    final ContainerInfo info = sut.inspectContainer(container.id());

    assertThat(info.hostConfig().shmSize(), is(10000000L));
  }

  @Test
  public void testOomKillDisable() throws Exception {
    requireDockerApiVersionAtLeast("1.20", "OomKillDisable");

    // Pull image
    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .hostConfig(HostConfig.builder()
                        .oomKillDisable(true) // Defaults to false
                        .build())
        .build();

    final ContainerCreation container = sut.createContainer(config, randomName());
    final ContainerInfo info = sut.inspectContainer(container.id());

    assertThat(info.hostConfig().oomKillDisable(), is(true));
  }

  @Test
  public void testOomScoreAdj() throws Exception {
    requireDockerApiVersionAtLeast("1.22", "OomScoreAdj");

    // Pull image
    sut.pull(BUSYBOX_LATEST);

    final ContainerConfig config = ContainerConfig.builder()
        .image(BUSYBOX_LATEST)
        .hostConfig(HostConfig.builder()
                        .oomScoreAdj(500) // Defaults to 0
                        .build())
        .build();

    final ContainerCreation container = sut.createContainer(config, randomName());
    final ContainerInfo info = sut.inspectContainer(container.id());

    assertThat(info.hostConfig().oomScoreAdj(), is(500));
  }

  @Test
  public void testInspectSwarm() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");

    final Swarm swarm = sut.inspectSwarm();
    assertThat(swarm.createdAt(), is(notNullValue()));
    assertThat(swarm.updatedAt(), is(notNullValue()));
    assertThat(swarm.id(), is(not(isEmptyOrNullString())));
    assertThat(swarm.joinTokens().worker(), is(not(isEmptyOrNullString())));
    assertThat(swarm.joinTokens().manager(), is(not(isEmptyOrNullString())));
  }

  @Test
  public void testCreateServiceWithNetwork() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");

    final String networkName = randomName();
    final String serviceName = randomName();

    final NetworkCreation networkCreation = sut
            .createNetwork(NetworkConfig.builder().driver("overlay")
                    // TODO: workaround for https://github.com/docker/docker/issues/25735
                    .ipam(Ipam.builder().driver("default").build())
                    //
                    .name(networkName).build());

    final String networkId = networkCreation.id();
    
    assertThat(networkId, is(notNullValue()));

    final TaskSpec taskSpec = TaskSpec.builder()
            .withContainerSpec(ContainerSpec.builder().withImage("alpine")
                    .withCommands(new String[] { "ping", "-c1000", "localhost" }).build())
            .build();

    final ServiceSpec spec = ServiceSpec.builder().withName(serviceName)
            .withTaskTemplate(taskSpec).withServiceMode(ServiceMode.withReplicas(1L))
            .withNetworks(NetworkAttachmentConfig.builder().withTarget(networkName).build())
            .build();

    final ServiceCreateResponse response = sut.createService(spec, new ServiceCreateOptions());
    assertThat(response.id(), is(notNullValue()));

    final Service inspectService = sut.inspectService(serviceName);
    assertThat(inspectService.spec().networks().size(), is(1));
    assertThat(Iterables.find(inspectService.spec().networks(), 
      new Predicate<NetworkAttachmentConfig>() {

      @Override
      public boolean apply(NetworkAttachmentConfig config) {
        return networkId.equals(config.target());
      }
    }, null), is(notNullValue()));
    
    sut.removeService(serviceName);
    sut.removeNetwork(networkName);
  }
  
  @Test
  public void testCreateService() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");

    final ServiceSpec spec = createServiceSpec(randomName());

    final ServiceCreateResponse response = sut.createService(spec, new ServiceCreateOptions());
    assertThat(response.id(), is(notNullValue()));
  }

  @Test
  public void testInspectService() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");

    final String[] commandLine = {"ping", "-c4", "localhost"};
    final TaskSpec taskSpec = TaskSpec
        .builder()
        .withContainerSpec(ContainerSpec.builder().withImage("alpine")
                               .withCommands(commandLine).build())
        .withLogDriver(Driver.builder().withName("json-file").withOption("max-file", "3")
                           .withOption("max-size", "10M").build())
        .withResources(ResourceRequirements.builder()
                           .withLimits(com.spotify.docker.client.messages.swarm.Resources.builder().
                               withMemoryBytes(10 * 1024 * 1024L).build())
                           .build())
        .withRestartPolicy(RestartPolicy.builder().withCondition("on-failure")
                               .withDelay(10000000).withMaxAttempts(10).build())
        .build();

    final EndpointSpec endpointSpec = EndpointSpec.builder()
        .withPorts(new PortConfig[] {PortConfig.builder().withName("web")
                                         .withProtocol("tcp").withPublishedPort(8080)
                                         .withTargetPort(80).build()})
        .build();
    final ServiceMode serviceMode = ServiceMode.withReplicas(4);

    final String serviceName = randomName();
    final ServiceSpec spec = ServiceSpec.builder().withName(serviceName).withTaskTemplate(taskSpec)
        .withServiceMode(serviceMode)
        .withEndpointSpec(endpointSpec)
        .build();

    final ServiceCreateResponse response = sut.createService(spec, new ServiceCreateOptions());

    final Service service = sut.inspectService(response.id());

    assertThat(service.spec().name(), is(serviceName));
    assertThat(service.spec().taskTemplate().containerSpec().image(), is("alpine"));
    assertThat(service.spec().taskTemplate().containerSpec().command(),
               equalTo(Arrays.asList(commandLine)));
  }

  @Test
  public void testUpdateService() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");
    final ServiceSpec spec = createServiceSpec(randomName());

    final ServiceCreateResponse response = sut.createService(spec, new ServiceCreateOptions());
    assertThat(response.id(), is(notNullValue()));

    Service service = sut.inspectService(response.id());
    assertThat(service.spec().mode().replicated().replicas(), is(4L));

    // update service with same spec, but bump the number of replicas by 1
    sut.updateService(response.id(), service.version().index(), ServiceSpec.builder()
        .withName(service.spec().name())
        .withTaskTemplate(service.spec().taskTemplate())
        .withServiceMode(ServiceMode.withReplicas(5))
        .withEndpointSpec(service.spec().endpointSpec())
        .withUpdateConfig(service.spec().updateConfig())
        .build());
    service = sut.inspectService(response.id());
    assertThat(service.spec().mode().replicated().replicas(), is(5L));
  }


  @Test
  public void testListServices() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");
    List<Service> services = sut.listServices();
    assertThat(services, is(empty()));

    final ServiceSpec spec = createServiceSpec(randomName());

    sut.createService(spec, new ServiceCreateOptions());

    services = sut.listServices();
    assertThat(services.size(), is(1));
  }

  @Test
  public void testListServicesFilterById() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");
    final ServiceSpec spec = createServiceSpec(randomName());
    final ServiceCreateResponse response = sut.createService(spec, new ServiceCreateOptions());

    final List<Service> services = sut
        .listServices(Service.find().withServiceId(response.id()).build());
    assertThat(services.size(), is(1));
    assertThat(services.get(0).id(), is(response.id()));
  }

  @Test
  public void testListServicesFilterByName() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");
    final String serviceName = randomName();
    final ServiceSpec spec = createServiceSpec(serviceName);
    sut.createService(spec, new ServiceCreateOptions());

    final List<Service> services =
        sut.listServices(Service.find().withServiceName(serviceName).build());
    assertThat(services.size(), is(1));
    assertThat(services.get(0).spec().name(), is(serviceName));
  }

  @Test
  public void testRemoveService() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");

    final ServiceSpec spec = createServiceSpec(randomName());
    final ServiceCreateResponse response = sut.createService(spec, new ServiceCreateOptions());
    assertThat(sut.listServices(), is(not(empty())));
    sut.removeService(response.id());
    assertThat(sut.listServices(), is(empty()));
  }

  @Test
  public void testInspectTask() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");

    final ServiceSpec spec = createServiceSpec(randomName());
    assertThat(sut.listTasks().size(), is(0));
    final ServiceCreateResponse response = sut.createService(spec, new ServiceCreateOptions());
    Thread.sleep(2000); // to give it a while to spin containers
    final Task task = sut.listTasks().get(0);
    final Task inspectTask = sut.inspectTask(task.id());
    assertThat(task, equalTo(inspectTask));
  }

  @Test
  public void testListTasks() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");

    final ServiceSpec spec = createServiceSpec(randomName());
    assertThat(sut.listTasks().size(), is(0));
    final ServiceCreateResponse response = sut.createService(spec, new ServiceCreateOptions());
    Thread.sleep(2000); // to give it a while to spin containers
    assertThat(sut.listTasks().size(), is(4));
  }

  @Test
  public void testListTaskWithCriteria() throws Exception {
    requireDockerApiVersionAtLeast("1.24", "swarm support");

    final ServiceSpec spec = createServiceSpec(randomName());
    assertThat(sut.listTasks().size(), is(0));
    final ServiceCreateResponse response = sut.createService(spec, new ServiceCreateOptions());
    Thread.sleep(2000); // to give it a while to spin containers

    final Task task = sut.listTasks().get(1);

    final List<Task> tasksWithId = sut.listTasks(Task.find().withTaskId(task.id()).build());

    assertThat(tasksWithId.size(), is(1));
    assertThat(tasksWithId.get(0), equalTo(task));
  }

  private ServiceSpec createServiceSpec(final String serviceName) {
    final TaskSpec taskSpec = TaskSpec
        .builder()
        .withContainerSpec(ContainerSpec.builder().withImage("alpine")
                               .withCommands(new String[] {"ping", "-c1000", "localhost"}).build())
        .build();

    final ServiceMode serviceMode = ServiceMode.withReplicas(4);

    return ServiceSpec.builder().withName(serviceName).withTaskTemplate(taskSpec)
        .withServiceMode(serviceMode)
        .build();
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

  private String createSleepingContainer() throws Exception {
    sut.pull(BUSYBOX_LATEST);
    final String volumeContainer = randomName();
    final ContainerConfig volumeConfig = ContainerConfig.builder().image(BUSYBOX_LATEST)
        .cmd("sh", "-c",
             "for i in `seq 1 7`; do "
             + "sleep ${i} ;"
             + "echo \"Seen output after ${i} seconds.\" ;"
             + "done;"
             + "echo Finished ;")
        .build();
    sut.createContainer(volumeConfig, volumeContainer);
    sut.startContainer(volumeContainer);
    return volumeContainer;
  }

  private void verifyNoTimeoutContainer(final String volumeContainer, final StringBuffer result)
      throws Exception {
    log.info("Reading has finished, waiting for program to end.");
    sut.waitContainer(volumeContainer);
    final ContainerInfo info = sut.inspectContainer(volumeContainer);
    assertThat(result.toString().contains("Finished"), is(true));
    assertThat(info.state().running(), is(false));
    assertThat(info.state().exitCode(), is(0));
  }

  private List<String> containersToIds(final List<Container> containers) {
    final Function<Container, String> containerToId = new Function<Container, String>() {
      @Override
      public String apply(final Container container) {
        return container.id();
      }
    };
    return Lists.transform(containers, containerToId);
  }

  private List<String> imagesToShortIds(final List<Image> images) {
    final Function<Image, String> imageToShortId = new Function<Image, String>() {
      @Override
      public String apply(final Image image) {
        return image.id().substring(0, 12);
      }
    };
    return Lists.transform(images, imageToShortId);
  }

  private List<String> imagesToShortIdsAndRemoveSha256(final List<Image> images) {
    final Function<Image, String> imageToShortId = new Function<Image, String>() {
      @Override
      public String apply(final Image image) {
        return image.id().replaceFirst("sha256:", "").substring(0, 12);
      }
    };
    return Lists.transform(images, imageToShortId);
  }
}
