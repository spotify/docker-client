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

import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerExit;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.RemovedImage;
import com.spotify.docker.client.messages.Version;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * A client for interacting with dockerd.
 *
 * Note: All methods throw DockerException on unexpected docker response status codes.
 */
public interface DockerClient extends Closeable {

  /**
   * Ping the docker daemon. Returns "OK" if all is well, though that
   * it simply returns a 200 status is probably sufficient information.
   */
  String ping() throws DockerException, InterruptedException;

  /**
   * Get the docker version.
   */
  Version version() throws DockerException, InterruptedException;

  /**
   * Get docker instance information.
   */
  Info info() throws DockerException, InterruptedException;

  /**
   * List docker containers.
   *
   * @param params Container listing and filtering options.
   * @return A list of containers.
   */
  List<Container> listContainers(ListContainersParam... params)
      throws DockerException, InterruptedException;

  /**
   * Inspect a docker container.
   *
   * @param containerId The id of the container to inspect.
   * @return Info about the container.
   * @throws ContainerNotFoundException if the container was not found (404).
   */
  ContainerInfo inspectContainer(String containerId)
      throws DockerException, InterruptedException;

  /**
   * Inspect a docker container image.
   *
   * @param image The image to inspect.
   * @return Info about the image.
   * @throws ImageNotFoundException if the image was not found (404).
   */
  ImageInfo inspectImage(String image)
      throws DockerException, InterruptedException;

  /**
   * Remove a docker image.
   *
   * @param image The image to remove.
   * @return A list describing each image which was removed.
   * @throws ImageNotFoundException if the image was not found (404).
   */
  List<RemovedImage> removeImage(String image)
      throws DockerException, InterruptedException;


  /**
   * Remove a docker image.
   *
   * @param image The image to remove.
   * @param force Force image removal.
   * @param noPrune Do not delete untagged parents.
   * @return A list describing each image which was removed.
   * @throws ImageNotFoundException if the image was not found (404).
   */
  List<RemovedImage> removeImage(String image, boolean force, boolean noPrune)
      throws DockerException, InterruptedException;

  /**
   * Pull a docker container image.
   *
   * @param image The image to pull.
   */
  void pull(String image)
      throws DockerException, InterruptedException;

  /**
   * Pull a docker container image, using a custom ProgressMessageHandler
   *
   * @param image The image to pull.
   * @param handler The handler to use for processing each progress message received from Docker.
   */
  void pull(String image, ProgressHandler handler)
      throws DockerException, InterruptedException;

  /**
   * Push a docker container image.
   *
   * @param image The image to push.
   */
  void push(String image)
      throws DockerException, InterruptedException;

  /**
   * Push a docker container image, using a custom ProgressHandler
   *
   * @param image The image to push.
   * @param handler The handler to use for processing each progress message received from Docker.
   */
  void push(String image, ProgressHandler handler)
      throws DockerException, InterruptedException;

  /**
   * Tag a docker image.
   *
   * @param image The image to tag.
   * @param name The new name that will be applied to the image.
   */
  void tag(final String image, final String name)
      throws DockerException, InterruptedException;

  /**
   * Build a docker image.
   *
   * @param directory The directory containing the dockerfile.
   * @param params Additional flags to use during build.
   * @return The id of the built image if successful, otherwise null.
   */
  String build(final Path directory, final BuildParameter... params)
      throws DockerException, InterruptedException, IOException;

  /**
   * Build a docker image.
   *
   * @param directory The directory containing the dockerfile.
   * @param name The repository name and optional tag to apply to the built image.
   * @param params Additional flags to use during build.
   * @return The id of the built image if successful, otherwise null.
   */
  String build(final Path directory, final String name, final BuildParameter... params)
      throws DockerException, InterruptedException, IOException;

  /**
   * Build a docker image.
   *
   * @param directory The directory containing the dockerfile.
   * @param handler The handler to use for processing each progress message received from Docker.
   * @param params Additional flags to use during build.
   * @return The id of the built image if successful, otherwise null.
   */
  String build(final Path directory, final ProgressHandler handler, final BuildParameter... params)
      throws DockerException, InterruptedException, IOException;

  /**
   * Build a docker image.
   *
   * @param directory The directory containing the dockerfile.
   * @param name The repository name and optional tag to apply to the built image.
   * @param handler The handler to use for processing each progress message received from Docker.
   * @param params Additional flags to use during build.
   * @return The id of the built image if successful, otherwise null.
   */
  String build(final Path directory, final String name, final ProgressHandler handler,
               final BuildParameter... params)
      throws DockerException, InterruptedException, IOException;

  /**
   * Flags which can be passed to the <code>build</code> method.
   */
  public static enum BuildParameter {
    /** Suppress verbose build output. */
    QUIET("q", true),
    /** Do not use the cache when building the image. */
    NO_CACHE("nocache", true),
    /** Do not remove intermediate containers after a successful build. */
    NO_RM("rm", false),
    /** Always remove intermediate containers. */
    FORCE_RM("forcerm", true);

    final String queryParam;
    final boolean value;

    private BuildParameter(final String queryParam, final boolean value) {
      this.queryParam = queryParam;
      this.value = value;
    }
  }

  /**
   * Create a docker container.
   *
   * @param config The container configuration.
   * @return Container creation result with container id and eventual warnings from docker.
   * @throws ImageNotFoundException if the image was not found (404).
   */
  ContainerCreation createContainer(ContainerConfig config)
      throws DockerException, InterruptedException;

  /**
   * Create a docker container.
   *
   * @param config The container configuration.
   * @param name   The container name.
   * @return Container creation result with container id and eventual warnings from docker.
   * @throws ImageNotFoundException if the image was not found (404).
   */
  ContainerCreation createContainer(ContainerConfig config, String name)
      throws DockerException, InterruptedException;

  /**
   * Start a docker container.
   *
   * @param containerId The id of the container to start.
   * @throws ContainerNotFoundException if the container was not found (404).
   */
  void startContainer(String containerId)
      throws DockerException, InterruptedException;

  /**
   * Start a docker container.
   *
   * @param containerId The id of the container to start.
   * @param hostConfig  The docker host configuration to use when starting the container.
   * @throws ContainerNotFoundException if the container was not found (404).
   */
  void startContainer(String containerId, HostConfig hostConfig)
      throws DockerException, InterruptedException;

  void stopContainer(String containerId, int secondsToWaitBeforeKilling)
      throws DockerException, InterruptedException;

  /**
   * Wait for a docker container to exit.
   *
   * @param containerId The id of the container to wait for.
   * @return Exit response with status code.
   * @throws ContainerNotFoundException if the container was not found (404).
   */
  ContainerExit waitContainer(String containerId)
      throws DockerException, InterruptedException;

  /**
   * Kill a docker container.
   *
   * @param containerId The id of the container to kill.
   * @throws ContainerNotFoundException if the container was not found (404).
   */
  void killContainer(String containerId)
      throws DockerException, InterruptedException;

  /**
   * Remove a docker container.
   *
   * @param containerId The id of the container to remove.
   * @throws ContainerNotFoundException if the container was not found (404).
   */
  void removeContainer(String containerId)
      throws DockerException, InterruptedException;

  /**
   * Remove a docker container.
   *
   * @param containerId   The id of the container to remove.
   * @param removeVolumes Whether to remove volumes as well.
   * @throws ContainerNotFoundException if the container was not found (404).
   */
  void removeContainer(String containerId, boolean removeVolumes)
      throws DockerException, InterruptedException;

  /**
   * Export a docker container as a tar archive.
   *
   * @param containerId The id of the container to export.
   * @return A stream in tar format that contains the contents of the container file system.
   */
  InputStream exportContainer(String containerId)
      throws DockerException, InterruptedException;

  /**
   * Copies some files out of a container.
   *
   * @param containerId The id of the container to copy files from.
   * @param path        The path inside of the container to copy.  If this is a directory, it will
   *                    be copied recursively.  If this is a file, only that file will be copied.
   * @return A stream in tar format that contains the copied files.  If a directory was copied, the
   * directory will be at the root of the tar archive (so {@code copy(..., "/usr/share")} will
   * result in a directory called {@code share} in the tar archive).  The directory name is
   * completely resolved, so copying {@code "/usr/share/././."} will still create a directory called
   * {@code "share"} in the tar archive.  If a single file was copied, that file will be the sole
   * entry in the tar archive.  Copying {@code "."} or equivalently {@code "/"} will result in the
   * tar archive containing a single folder named after the container ID.
   */
  InputStream copyContainer(String containerId, String path)
      throws DockerException, InterruptedException;

  /**
   * Get docker container logs.
   *
   * @param containerId The id of the container to get logs for.
   * @param params      Params for controlling what streams to get and whether to tail or not.
   * @return A log message stream.
   * @throws ContainerNotFoundException if the container was not found (404).
   */
  LogStream logs(String containerId, LogsParameter... params)
      throws DockerException, InterruptedException;

  /**
   * Closes any and all underlying connections to docker, and release resources.
   */
  @Override
  void close();

  /**
   * Parameters for {@link #logs(String, LogsParameter...)}
   */
  public static enum LogsParameter {
    FOLLOW,
    STDOUT,
    STDERR,
    TIMESTAMPS,
  }

  /**
   * Parameters for {@link #listContainers(ListContainersParam...)}
   */
  public static class ListContainersParam {

    private final String name;
    private final String value;

    public ListContainersParam(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Parameter name.
     */
    public String name() {
      return name;
    }

    /**
     * Parameter value.
     */
    public String value() {
      return value;
    }

    /**
     * Show all containers. Only running containers are shown by default
     */
    public static ListContainersParam allContainers() {
      return allContainers(true);
    }

    /**
     * Show all containers. Only running containers are shown by default
     */
    public static ListContainersParam allContainers(final boolean all) {
      return create("all", String.valueOf(all));
    }

    /**
     * Show <code>limit</code> last created containers, include non-running ones.
     */
    public static ListContainersParam limitContainers(final Integer limit) {
      return create("limit", String.valueOf(limit));
    }

    /**
     * Show only containers created since id, include non-running ones.
     */
    public static ListContainersParam containersCreatedSince(final String id) {
      return create("since", String.valueOf(id));
    }

    /**
     * Show only containers created before id, include non-running ones.
     */
    public static ListContainersParam containersCreatedBefore(final String id) {
      return create("before", String.valueOf(id));
    }

    /**
     * Show the containers sizes.
     */
    public static ListContainersParam withContainerSizes(final Boolean size) {
      return create("size", String.valueOf(size));
    }

    /**
     * Create a custom parameter.
     */
    public static ListContainersParam create(final String name, final String value) {
      return new ListContainersParam(name, value);
    }
  }
}
