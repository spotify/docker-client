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

import com.google.common.base.Strings;
import com.google.common.net.HostAndPort;

import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.google.common.base.Optional.fromNullable;
import static java.lang.Long.toHexString;
import static java.lang.String.format;
import static java.lang.System.getenv;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;


public class DockerClientTest {

  public static final int DOCKER_PORT = Integer.valueOf(env("DOCKER_PORT", "4160"));
  public static final String DOCKER_HOST = env("DOCKER_HOST", ":" + DOCKER_PORT);
  public static final String DOCKER_ADDRESS;
  public static final String DOCKER_ENDPOINT;

  @Rule public ExpectedException exception = ExpectedException.none();

  static {
    // Parse DOCKER_HOST
    final String stripped = DOCKER_HOST.replaceAll(".*://", "");
    final HostAndPort hostAndPort = HostAndPort.fromString(stripped);
    final String host = hostAndPort.getHostText();
    DOCKER_ADDRESS = Strings.isNullOrEmpty(host) ? "localhost" : host;
    DOCKER_ENDPOINT = format("http://%s:%d", DOCKER_ADDRESS,
                             hostAndPort.getPortOrDefault(DOCKER_PORT));
  }

  private static String env(final String key, final String defaultValue) {
    return fromNullable(getenv(key)).or(defaultValue);
  }

  private final DockerClient sut = new DefaultDockerClient(URI.create(DOCKER_ENDPOINT));

  private final String nameTag = toHexString(ThreadLocalRandom.current().nextLong());

  @After
  public void removeContainers() throws Exception {
    final List<Container> containers = sut.listContainers();
    for (Container container : containers) {
      final ContainerInfo info = sut.inspectContainer(container.id());
      if (info != null && info.name().contains(nameTag)) {
        sut.killContainer(info.id());
        sut.removeContainer(info.id());
      }
    }
  }

  @Test
  public void testPullWithTag() throws Exception {
    sut.pull("busybox:buildroot-2014.02");
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

  private String randomName() {
    return nameTag + '-' + toHexString(ThreadLocalRandom.current().nextLong());
  }
}