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

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerChange;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerExit;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.ContainerStats;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.ExecState;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.ImageHistory;
import com.spotify.docker.client.messages.ImageInfo;
import com.spotify.docker.client.messages.ImageSearchResult;
import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.Network;
import com.spotify.docker.client.messages.NetworkConfig;
import com.spotify.docker.client.messages.NetworkCreation;
import com.spotify.docker.client.messages.RemovedImage;
import com.spotify.docker.client.messages.ServiceCreateOptions;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.TopResults;
import com.spotify.docker.client.messages.Version;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.VolumeList;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.Swarm;
import com.spotify.docker.client.messages.swarm.Task;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * A client for interacting with dockerd.
 *
 * Note: All methods throw DockerException on unexpected docker response status codes.
 */
public interface DockerClient extends Closeable {

  /**
   * Ping the docker daemon. Returns "OK" if all is well, though that it simply returns a 200 status
   * is probably sufficient information.
   *
   * @return String "OK"
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  String ping() throws DockerException, InterruptedException;

  /**
   * Get the docker version.
   *
   * @return docker version
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  Version version() throws DockerException, InterruptedException;

  /**
   * Check auth configuration.
   *
   * @param authConfig The authentication config needed to pull the image.
   * @return status code of auth request
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  int auth(final AuthConfig authConfig) throws DockerException, InterruptedException;

  /**
   * Get docker instance information.
   *
   * @return docker info
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  Info info() throws DockerException, InterruptedException;

  /**
   * List docker containers.
   *
   * @param params Container listing and filtering options.
   * @return A list of containers.
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                            if one or more params were bad (400)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<Container> listContainers(ListContainersParam... params)
      throws DockerException, InterruptedException;

  /**
   * List docker images.
   *
   * @param params Image listing and filtering options.
   * @return A list of images.
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                            if one or more params were bad (400)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<Image> listImages(ListImagesParam... params) throws DockerException, InterruptedException;

  /**
   * Inspect a docker container.
   *
   * @param containerId The id of the container to inspect.
   * @return Info about the container.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                            if container was not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ContainerInfo inspectContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Create a new image from a container's changes.
   *
   * @param containerId The id of the container to commit.
   * @param comment     commit message.
   * @param author      image author.
   * @param tag         image tag.
   * @param repo        repository to commit to.
   * @param config      ContainerConfig to commit.
   * @return ContainerCreation reply.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                            if container was not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ContainerCreation commitContainer(final String containerId,
                                    final String repo,
                                    final String tag,
                                    final ContainerConfig config,
                                    final String comment,
                                    final String author)
      throws DockerException, InterruptedException;

  /**
   * Inspect a docker container image.
   *
   * @param image The image to inspect.
   * @return Info about the image.
   * @throws com.spotify.docker.client.exceptions.ImageNotFoundException
   *                            if image was not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ImageInfo inspectImage(String image) throws DockerException, InterruptedException;

  /**
   * Remove a docker image.
   *
   * @param image The image to remove.
   * @return A list describing each image which was removed.
   * @throws com.spotify.docker.client.exceptions.ImageNotFoundException
   *                            if image was not found (404)
   * @throws com.spotify.docker.client.exceptions.ConflictException
   *                            conflict (409)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<RemovedImage> removeImage(String image) throws DockerException, InterruptedException;


  /**
   * Remove a docker image.
   *
   * @param image   The image to remove.
   * @param force   Force image removal.
   * @param noPrune Do not delete untagged parents.
   * @return A list describing each image which was removed.
   * @throws com.spotify.docker.client.exceptions.ImageNotFoundException
   *                            if image was not found (404)
   * @throws com.spotify.docker.client.exceptions.ConflictException
   *                            conflict (409)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<RemovedImage> removeImage(String image, boolean force, boolean noPrune)
      throws DockerException, InterruptedException;

  /**
   * Search for images on Docker Hub
   *
   * This method is broken for Docker 1.7.x because of a Docker bug.
   * See https://github.com/docker/docker/pull/14850.
   *
   * @param term the search term
   * @return a list of matches for the given search term
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<ImageSearchResult> searchImages(String term) throws DockerException, InterruptedException;


  /**
   * Creates a single image from a tarball. This method also tags the image
   * with the given image name upon loading completion.
   *
   * @param image        the name to assign to the image.
   * @param imagePayload the image's payload (i.e.: the stream corresponding to the image's .tar
   *                     file).
   * @throws DockerException      if a server error occurred (500).
   * @throws InterruptedException if the thread is interrupted.
   *
   * @deprecated Use {@link #load(InputStream)} to load a set of image layers from a tarball. Use
   * {@link #create(String, InputStream)} to create a single image from the contents of a tarball.
   */
  @Deprecated
  void load(String image, InputStream imagePayload)
      throws DockerException, InterruptedException;


  /**
   * Creates a single image from a tarball. This method also tags the image
   * with the given image name upon loading completion.
   *
   * @param image        the name to assign to the image.
   * @param imagePayload the image's payload (i.e.: the stream corresponding to the image's .tar
   *                     file).
   * @param handler      The handler to use for processing each progress message received from
   *                     Docker.
   * @throws DockerException      if a server error occurred (500).
   * @throws InterruptedException if the thread is interrupted.
   *
   * @deprecated Use {@link #load(InputStream)} to load a set of image layers from a tarball. Use
   * {@link #create(String, InputStream, ProgressHandler)} to create a single image from the
   * contents of a tarball.
   */
  @Deprecated
  void load(String image, InputStream imagePayload, ProgressHandler handler)
      throws DockerException, InterruptedException;


  /**
   * Creates a single image from a tarball. This method also tags the image
   * with the given image name upon loading completion.
   *
   * @param image        the name to assign to the image.
   * @param imagePayload the image's payload (i.e.: the stream corresponding to the image's .tar
   *                     file).
   * @param authConfig   The authentication config needed to pull the image.
   * @throws DockerException      if a server error occurred (500).
   * @throws InterruptedException if the thread is interrupted.
   *
   * @deprecated Use {@link #load(InputStream)} to load a set of image layers from a tarball. Use
   * {@link #create(String, InputStream)} to create a single image from the contents of a tarball.
   */
  @Deprecated
  void load(String image, InputStream imagePayload, AuthConfig authConfig)
      throws DockerException, InterruptedException;


  /**
   * Creates a single image from a tarball. This method also tags the image
   * with the given image name upon loading completion.
   *
   * @param image        the name to assign to the image.
   * @param imagePayload the image's payload (i.e.: the stream corresponding to the image's .tar
   *                     file).
   * @param authConfig   The authentication config needed to pull the image.
   * @param handler      The handler to use for processing each progress message received from
   *                     Docker.
   * @throws DockerException      if a server error occurred (500).
   * @throws InterruptedException if the thread is interrupted.
   *
   * @deprecated Use {@link #load(InputStream)} to load a set of image layers from a tarball. Use
   * {@link #create(String, InputStream, ProgressHandler)} to create a single image from the
   * contents of a tarball.
   */
  @Deprecated
  void load(String image, InputStream imagePayload, AuthConfig authConfig,
            ProgressHandler handler) throws DockerException, InterruptedException;

  /**
   * Load a set of images and tags from a tarball.
   *
   * @param imagePayload the image's payload (i.e.: the stream corresponding to the image's .tar
   *                     file).
   * @throws DockerException      if a server error occurred (500).
   * @throws InterruptedException if the thread is interrupted.
   */
  void load(InputStream imagePayload) throws DockerException, InterruptedException;

  /**
   * Creates a single image from a tarball. This method also tags the image
   * with the given image name upon loading completion.
   *
   * @param image        the name to assign to the image.
   * @param imagePayload the image's payload (i.e.: the stream corresponding to the image's .tar
   *                     file).
   * @throws DockerException      if a server error occurred (500).
   * @throws InterruptedException if the thread is interrupted.
   */
  void create(String image, InputStream imagePayload)
          throws DockerException, InterruptedException;

  /**
   * Creates a single image from a tarball. This method also tags the image
   * with the given image name upon loading completion.
   *
   * @param image        the name to assign to the image.
   * @param imagePayload the image's payload (i.e.: the stream corresponding to the image's .tar
   *                     file).
   * @param handler      The handler to use for processing each progress message received from
   *                     Docker.
   * @throws DockerException      if a server error occurred (500).
   * @throws InterruptedException if the thread is interrupted.
   */
  void create(String image, InputStream imagePayload, ProgressHandler handler)
          throws DockerException, InterruptedException;

  /**
   * Get a tarball containing all images and metadata for the repository specified.
   * @param images the name(s) of one or more images to save. If a specific name and tag
   *              (e.g. ubuntu:latest), then only that image (and its parents) are returned.
   *              If an image ID, similarly only that image (and its parents) are returned,
   *              but with the exclusion of the 'repositories' file in the tarball,
   *              as there were no image names referenced.
   * @return the images' .tar streams.
   * @throws DockerException      if a server error occurred (500).
   * @throws IOException          if the server started returning, but an I/O error occurred in the
   *                              context of processing it on the client-side.
   * @throws InterruptedException if the thread is interrupted.
   */
  InputStream save(String... images) throws DockerException, IOException, InterruptedException;

  /**
   * Get a tarball containing all images and metadata for the repository specified.
   * @param image the name or id of the image to save. If a specific name and tag
   *              (e.g. ubuntu:latest), then only that image (and its parents) are returned.
   *              If an image ID, similarly only that image (and its parents) are returned,
   *              but with the exclusion of the 'repositories' file in the tarball,
   *              as there were no image names referenced.
   * @param authConfig The authentication config needed to pull the image.
   * @return the image's .tar stream.
   * @throws DockerException      if a server error occurred (500).
   * @throws IOException          if the server started returning, but an I/O error occurred in the
   *                              context of processing it on the client-side.
   * @throws InterruptedException if the thread is interrupted.
   *
   * @deprecated AuthConfig is not required. Use {@link #save(String...)}.
   */
  @Deprecated
  InputStream save(String image, AuthConfig authConfig)
      throws DockerException, IOException, InterruptedException;

  /**
   * Get a tarball containing all images and metadata for one or more repositories.
   * @param images the name or id of the image to save.
   *               if it is a specific name and tag (e.g. ubuntu:latest), then only that image
   *               (and its parents) are returned; if it is an image ID, similarly only that
   *               image (and its parents) are returned and there would be no names referenced
   *               in the 'repositories' file for this image ID.
   * @return a tar stream containing the image(s)
   * @throws DockerException      if a server error occurred (500).
   * @throws IOException          if the server started returning, but an I/O error occurred in the
   *                              context of processing it on the client-side.
   * @throws InterruptedException if the thread is interrupted.
   */
  InputStream saveMultiple(String... images)
      throws DockerException, IOException, InterruptedException;

  /**
   * List processes running inside the container by using <code>ps</code>.
   *
   * @param containerId the id of the container to examine
   * @return the titles and process list for the container
   * @throws DockerException      if a server error occurred (500).
   * @throws InterruptedException if the thread is interrupted.
   */
  TopResults topContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * List processes running inside the container using <code>ps</code> and the given arguments.
   *
   * @param containerId the id of the container to examine
   * @param psArgs the arguments to pass to <code>ps</code>
   *               inside the container, e.g., <code>"-ef"</code>
   * @return the titles and process list for the container
   * @throws DockerException      if a server error occurred (500).
   * @throws InterruptedException if the thread is interrupted.
   */
  TopResults topContainer(String containerId, String psArgs)
      throws DockerException, InterruptedException;

  /**
   * Pull a docker container image.
   *
   * @param image The image to pull.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void pull(String image) throws DockerException, InterruptedException;

  /**
   * Pull a docker container image, using a custom ProgressMessageHandler
   *
   * @param image   The image to pull.
   * @param handler The handler to use for processing each progress message received from Docker.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void pull(String image, ProgressHandler handler) throws DockerException, InterruptedException;

  /**
   * Pull a private docker container image.
   *
   * @param image      The image to pull.
   * @param authConfig The authentication config needed to pull the image.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void pull(String image, AuthConfig authConfig) throws DockerException, InterruptedException;

  /**
   * Pull a private docker container image, using a custom ProgressMessageHandler.
   *
   * @param image      The image to pull.
   * @param authConfig The authentication config needed to pull the image.
   * @param handler    The handler to use for processing each progress message received from
   *                   Docker.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void pull(String image, AuthConfig authConfig, ProgressHandler handler)
      throws DockerException, InterruptedException;

  /**
   * Push a docker container image.
   *
   * @param image The image to push.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void push(String image) throws DockerException, InterruptedException;

  /**
   * Push a docker container image, using a custom ProgressHandler
   *
   * @param image   The image to push.
   * @param handler The handler to use for processing each progress message received from Docker.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void push(String image, ProgressHandler handler) throws DockerException, InterruptedException;

  void push(final String image, final AuthConfig authconfig)
      throws DockerException, InterruptedException;

  void push(final String image, final ProgressHandler handler, final AuthConfig authConfig)
      throws DockerException, InterruptedException;

  /**
   * Tag a docker image.
   *
   * @param image The image to tag.
   * @param name  The new name that will be applied to the image.
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                            if one or more params were bad (400)
   * @throws com.spotify.docker.client.exceptions.ImageNotFoundException
   *                            if image was not found (404)
   * @throws com.spotify.docker.client.exceptions.ConflictException
   *                            conflict (409)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void tag(final String image, final String name) throws DockerException, InterruptedException;

  /**
   * Tag a docker image.
   *
   * @param image The image to tag.
   * @param name  The new name that will be applied to the image.
   * @param force Whether to force the tagging even if the tag is already assigned to another
   *              image.
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                            if one or more params were bad (400)
   * @throws com.spotify.docker.client.exceptions.ImageNotFoundException
   *                            if image was not found (404)
   * @throws com.spotify.docker.client.exceptions.ConflictException
   *                            conflict (409)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void tag(final String image, final String name, final boolean force)
      throws DockerException, InterruptedException;

  /**
   * Build a docker image.
   *
   * @param directory The directory containing the dockerfile.
   * @param params    Additional flags to use during build.
   * @return The id of the built image if successful, otherwise null.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws IOException          If some IO shit happened.
   */
  String build(final Path directory, final BuildParam... params)
      throws DockerException, InterruptedException, IOException;

  /**
   * Build a docker image.
   *
   * @param directory The directory containing the dockerfile.
   * @param name      The repository name and optional tag to apply to the built image.
   * @param params    Additional flags to use during build.
   * @return The id of the built image if successful, otherwise null.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws IOException          If some IO shit happened.
   */
  String build(final Path directory, final String name, final BuildParam... params)
      throws DockerException, InterruptedException, IOException;

  /**
   * Build a docker image.
   *
   * @param directory The directory containing the dockerfile.
   * @param handler   The handler to use for processing each progress message received from Docker.
   * @param params    Additional flags to use during build.
   * @return The id of the built image if successful, otherwise null.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws IOException          If some IO shit happened.
   */
  String build(final Path directory, final ProgressHandler handler, final BuildParam... params)
      throws DockerException, InterruptedException, IOException;

  /**
   * Build a docker image.
   *
   * @param directory The directory containing the dockerfile.
   * @param name      The repository name and optional tag to apply to the built image.
   * @param handler   The handler to use for processing each progress message received from Docker.
   * @param params    Additional flags to use during build.
   * @return The id of the built image if successful, otherwise null.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws IOException          If some IO shit happened.
   */
  String build(final Path directory, final String name, final ProgressHandler handler,
               final BuildParam... params)
      throws DockerException, InterruptedException, IOException;

  /**
   * Build a docker image.
   *
   * @param directory  The directory containing the dockerfile.
   * @param name       The repository name and optional tag to apply to the built image.
   * @param dockerfile The path within the build context to the Dockerfile
   * @param handler    The handler to use for processing each progress message received from
   *                   Docker.
   * @param params     Additional flags to use during build.
   * @return The id of the built image if successful, otherwise null.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws IOException          If some IO shit happened.
   */
  String build(final Path directory, final String name, final String dockerfile,
               final ProgressHandler handler, final BuildParam... params)
      throws DockerException, InterruptedException, IOException;

  /**
   * Flags which can be passed to the <code>build</code> method.
   */
  class BuildParam {

    private final String name;
    private final String value;

    /**
     * Parameter name.
     *
     * @return name of parameter
     */
    public String name() {
      return name;
    }

    /**
     * Parameter value.
     *
     * @return value of parameter
     */
    public String value() {
      return value;
    }

    public BuildParam(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Create a custom parameter.
     *
     * @param name  custom name
     * @param value custom value
     * @return BuildParam
     */
    public static BuildParam create(final String name, final String value) {
      return new BuildParam(name, value);
    }

    /**
     * Suppress verbose build output.
     *
     * @return BuildParam
     */
    public static BuildParam quiet() {
      return create("q", "true");
    }

    /**
     * Remove intermediate containers after a successful build.
     *
     * @return BuildParam
     */
    public static BuildParam rm() {
      return rm(true);
    }

    /**
     * Control whether to remove intermediate containers after a successful build.
     *
     * @param rm Whether to remove
     * @return BuildParam
     */
    public static BuildParam rm(final boolean rm) {
      return create("rm", String.valueOf(rm));
    }

    /**
     * Do not use the cache when building the image.
     *
     * @return BuildParam
     */
    public static BuildParam noCache() {
      return create("nocache", "true");
    }

    /**
     * Always remove intermediate containers.
     *
     * @return BuildParam
     */
    public static BuildParam forceRm() {
      return create("forcerm", "true");
    }

    /**
     * Always attempt to pull a newer version of the base image even if one exists locally.
     *
     * @return BuildParam
     */
    public static BuildParam pullNewerImage() {
      return create("pull", "true");
    }

    /**
     * Repository name (and optionally a tag) to be applied to the
     * resulting image in case of success.
     *
     * You could also pass the name explicitly to {@link #build(Path, String, BuildParam...)}
     * or one of the other build methods that takes an explicit name.
     * @param name A name to apply to the image
     * @return BuildParam
     */
    public static BuildParam name(final String name) {
      return create("t", name);
    }

    /**
     * path within the build context to the Dockerfile. This is ignored
     * if {@link #remote(URI)} is specified and points to an individual filename.
     *
     * You could also pass the dockerfile path explicitly to
     * {@link #build(Path, String, String, ProgressHandler, BuildParam...)}
     * or one of the other build methods that takes an explicit dockerfile path.
     * @param dockerfile Path to the dockerfile in the build context.
     * @return BuildParam
     */
    public static BuildParam dockerfile(final Path dockerfile) {
      return create("dockerfile", dockerfile.toString());
    }

    /**
     * A Git repository URI or HTTP/HTTPS URI build source. If the URI
     * specifies a filename, the file's contents are placed into a file called `Dockerfile`.
     *
     * @param remote A Git repository URI or HTTP/HTTPS URI build source.
     * @return BuildParam
     */
    public static BuildParam remote(final URI remote) {
      return create("remote", remote.toString());
    }

    /**
     * Set memory limit for build.
     * @param memory Memory limit for build, in bytes.
     * @return BuildParam
     */
    public static BuildParam memory(final Integer memory) {
      return create("memory", memory.toString());
    }

    /**
     * Total memory (memory + swap). Set to -1 to enable unlimited swap.
     * @param totalMemory Total memory (memory + swap) in bytes.
     * @return BuildParam
     */
    public static BuildParam totalMemory(final Integer totalMemory) {
      return create("memoryswap", totalMemory.toString());
    }

    /**
     * CPU shares (relative weight).
     * @param cpuShares CPU shares (relative weight).
     * @return BuildParam
     */
    public static BuildParam cpuShares(final Integer cpuShares) {
      return create("cpushares", cpuShares.toString());
    }

    /**
     * CPUs in which to allow execution, e.g. <code>0-3</code>, <code>0,1</code>.
     * @param cpusetCpus CPUs in which to allow execution
     * @return BuildParam
     */
    public static BuildParam cpusetCpus(final Integer cpusetCpus) {
      return create("cpusetcpus", cpusetCpus.toString());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final BuildParam that = (BuildParam) o;

      return Objects.equals(this.name, that.name) &&
          Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, value);
    }
  }

  /**
   * Return the history of the image.
   * @param image An image name or ID.
   * @return A List of {@link ImageHistory}
   * @throws DockerException  if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<ImageHistory> history(final String image) throws InterruptedException, DockerException;

  /**
   * Create a docker container.
   *
   * @param config The container configuration.
   * @return Container creation result with container id and eventual warnings from docker.
   * @throws com.spotify.docker.client.exceptions.ImageNotFoundException
   *                            if the requested parent image was not found (404)
   * @throws DockerException  if logs cannot be attached, because container is not running (406),
   *                              or if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ContainerCreation createContainer(ContainerConfig config)
      throws DockerException, InterruptedException;

  /**
   * Create a docker container.
   *
   * @param config The container configuration.
   * @param name   The container name.
   * @return Container creation result with container id and eventual warnings from docker.
   * @throws com.spotify.docker.client.exceptions.ImageNotFoundException
   *                            if the requested parent image was not found (404)
   * @throws DockerException   if logs cannot be attached, because container is not running (406),
   *                              or if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ContainerCreation createContainer(ContainerConfig config, String name)
      throws DockerException, InterruptedException;

  /**
   * Rename a docker container.
   *
   * @param containerId The id of the container to rename.
   * @param name        The new name the container will have
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container cannot be found (404)
   * @throws com.spotify.docker.client.exceptions.ContainerRenameConflictException
   *                              if name is already assigned (409)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void renameContainer(String containerId, String name)
      throws DockerException, InterruptedException;

  /**
   * Start a docker container.
   *
   * @param containerId The id of the container to start.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void startContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Stop a docker container by sending a SIGTERM, and following up with a SIGKILL if the container
   * doesn't exit gracefully and in a timely manner.
   *
   * @param containerId                The id of the container to stop.
   * @param secondsToWaitBeforeKilling Time to wait after SIGTERM before sending SIGKILL.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void stopContainer(String containerId, int secondsToWaitBeforeKilling)
      throws DockerException, InterruptedException;

  /**
   * Pause a docker container.
   *
   * @param containerId The id of the container to pause.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void pauseContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Unpause a docker container.
   *
   * @param containerId The id of the container to pause.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */

  void unpauseContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Restart a docker container. with a 10 second default wait
   *
   * @param containerId The id of the container to restart.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void restartContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Restart a docker container.
   *
   * @param containerId                The id of the container to restart.
   * @param secondsToWaitBeforeRestart number of seconds to wait before killing the container.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void restartContainer(String containerId, int secondsToWaitBeforeRestart)
      throws DockerException, InterruptedException;

  /**
   * Wait for a docker container to exit.
   *
   * @param containerId The id of the container to wait for.
   * @return Exit response with status code.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ContainerExit waitContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Kill a docker container.
   * Note: This implementation deviates from the Docker Remote API. The
   * latter accepts the kill signal as an argument. This implementation does
   * not accept the signal argument; instead, the default SIGKILL is sent.
   *
   * @param containerId The id of the container to kill.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void killContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Remove a docker container.
   *
   * @param containerId The id of the container to remove.
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                            if one or more params were bad (400)
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      If a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void removeContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Remove a docker container.
   *
   * @param containerId The id of the container to remove.
   * @param params      {@link RemoveContainerParam}
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                            if one or more params were bad (400)
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      If a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void removeContainer(String containerId, RemoveContainerParam... params)
      throws DockerException, InterruptedException;

  /**
   * Remove a docker container.
   *
   * @param containerId   The id of the container to remove.
   * @param removeVolumes Whether to remove volumes as well.
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                            if one or more params were bad (400)
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      If a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @deprecated Use {@link #removeContainer(String, RemoveContainerParam...)}
   */
  @Deprecated
  void removeContainer(String containerId, boolean removeVolumes)
      throws DockerException, InterruptedException;

  /**
   * Parameters for {@link #removeContainer(String)}
   */
  class RemoveContainerParam {

    private final String name;
    private final String value;

    public RemoveContainerParam(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Parameter name.
     *
     * @return name of parameter
     */
    public String name() {
      return name;
    }

    /**
     * Parameter value.
     *
     * @return value of parameter
     */
    public String value() {
      return value;
    }

    /**
     * Create a custom parameter.
     *
     * @param name  custom name
     * @param value custom value
     * @return BuildParam
     */

    public static RemoveContainerParam create(final String name, final String value) {
      return new RemoveContainerParam(name, value);
    }

    /**
     * Remove the volumes associated to the container. If not specified, defaults to false.
     *
     * @return RemoveContainerParam
     */
    public static RemoveContainerParam removeVolumes() {
      return removeVolumes(true);
    }

    /**
     * Remove the volumes associated to the container. If not specified, defaults to false.
     *
     * @param remove Whether to remove volumes
     * @return RemoveContainerParam
     */
    public static RemoveContainerParam removeVolumes(final boolean remove) {
      return create("v", remove ? "1" : "0");
    }

    /**
     * Kill then remove the container. If not specified, defaults to false.
     *
     * @return RemoveContainerParam
     */
    public static RemoveContainerParam forceKill() {
      return forceKill(true);
    }

    /**
     * Kill then remove the container. If not specified, defaults to false.
     *
     * @param force Whether to force kill before removing.
     * @return RemoveContainerParam
     */
    public static RemoveContainerParam forceKill(final boolean force) {
      return create("force", force ? "1" : "0");
    }
  }

  /**
   * Export a docker container as a tar archive.
   *
   * @param containerId The id of the container to export.
   * @return A stream in tar format that contains the contents of the container file system.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  InputStream exportContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Copies some files out of a container. (removed on API version 1.24)
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
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws com.spotify.docker.client.exceptions.UnsupportedApiVersionException
   *                              If client API is greater than or equal to 1.24
   * @deprecated Replaced by {@link #archiveContainer(String, String)} in API 1.20, removed in 1.24.
   */
  @Deprecated
  InputStream copyContainer(String containerId, String path)
      throws DockerException, InterruptedException;


  /**
   * Copies an archive out of a container. (API version 1.20+)
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
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @since 1.20
   */
  InputStream archiveContainer(String containerId, String path)
      throws DockerException, InterruptedException;

  /**
   * Copies some files from host to container. (API version 1.20+)
   *
   * @param directory   The path to sent to container
   * @param containerId The id of the container to sent files.
   * @param path        The path inside of the container to put files.
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                            if one or more params were bad (400)
   * @throws com.spotify.docker.client.exceptions.PermissionException
   *                      if the volume or container root file system is marked "read only"
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      If a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws IOException          If IOException
   * @since 1.20
   */
  void copyToContainer(final Path directory, String containerId, String path)
      throws DockerException, InterruptedException, IOException;


  /**
   * Inspect changes on a container's filesystem.
   *
   * @param containerId The id of the container.
   * @return A list of the changes to the container file system.
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<ContainerChange> inspectContainerChanges(String containerId)
      throws DockerException, InterruptedException;

  /**
   * Get docker container logs.
   *
   * @param containerId The id of the container to get logs for.
   * @param params      Params for controlling what streams to get and whether to tail or not.
   * @return A log message stream.
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                            if one or more params were bad (400)
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  LogStream logs(String containerId, LogsParam... params)
      throws DockerException, InterruptedException;

  /**
   * Watches the docker API for events.
   *
   * This method is broken for Docker 1.7.x because of a Docker bug.
   * See https://github.com/docker/docker/issues/14354.
   *
   * @param params The parameters to apply to the events request
   * @return An event stream
   * @throws DockerException      If a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  EventStream events(EventsParam... params)
      throws DockerException, InterruptedException;

  /**
   * Sets up an exec instance in a running container id.
   *
   * @param containerId The id of the container
   * @param cmd         shell command
   * @param params      Exec params
   * @return {@link ExecCreation}
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ExecCreation execCreate(String containerId, String[] cmd, ExecCreateParam... params)
      throws DockerException, InterruptedException;

  /**
   * Starts a previously set up exec instance id. If detach is true, this API returns after starting
   * the exec command. Otherwise, this API sets up an interactive session with the exec command.
   *
   * @param execId exec id
   * @param params Exec start params
   * @return exec output
   * @throws com.spotify.docker.client.exceptions.ExecNotFoundException
   *                              if exec instance is not found (404)
   * @throws com.spotify.docker.client.exceptions.ExecStartConflictException
   *                              if container is paused (409)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  LogStream execStart(String execId, ExecStartParameter... params)
      throws DockerException, InterruptedException;

  /**
   * Inspect the Swarm cluster. Only available in Docker API &gt;= 1.24.
   *
   * @return Info about a swarm
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  Swarm inspectSwarm() throws DockerException, InterruptedException;

  /**
   * Create a new service. Only available in Docker API &gt;= 1.24.
   *
   * @param spec the service spec
   * @param options the service creation parameters
   * @return Service creation result with service id.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ServiceCreateResponse createService(ServiceSpec spec, ServiceCreateOptions options)
          throws DockerException, InterruptedException;

  /**
   * Inspect an existing service. Only available in Docker API &gt;= 1.24.
   *
   * @param serviceId the id of the service to inspect
   * @return Info about the service
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  Service inspectService(String serviceId) throws DockerException, InterruptedException;

  /**
   * Update an existing service. Only available in Docker API &gt;= 1.24.
   *
   * @param serviceId the identifier of the service
   * @param version the version of the service
   * @param spec the new service spec
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void updateService(String serviceId, Long version, ServiceSpec spec)
          throws DockerException, InterruptedException;

  /**
   * List all services. Only available in Docker API &gt;= 1.24.
   *
   * @return A list of services.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<Service> listServices() throws DockerException, InterruptedException;

  /**
   * List services that match the given criteria. Only available in Docker API &gt;= 1.24.
   *
   * @param criteria Service listing and filtering options.
   * @return
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<Service> listServices(Service.Criteria criteria)
          throws DockerException, InterruptedException;

  /**
   * Remove an existing service. Only available in Docker API &gt;= 1.24.
   *
   * @param serviceId the id of the service to remove
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void removeService(String serviceId)
          throws DockerException, InterruptedException;

  /**
   * Inspect an existing task. Only available in Docker API &gt;= 1.24.
   *
   * @param taskId the id of the task to inspect
   * @return Info about the task
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  Task inspectTask(String taskId)
          throws DockerException, InterruptedException;

  /**
   * List all tasks. Only available in Docker API &gt;= 1.24.
   *
   * @return A list of tasks.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<Task> listTasks()
          throws DockerException, InterruptedException;

  /**
   * List tasks that match the given criteria. Only available in Docker API &gt;= 1.24.
   *
   * @param criteria
   * @return A list of tasks.
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<Task> listTasks(Task.Criteria criteria)
          throws DockerException, InterruptedException;

  /**
   * Supported parameters for {@link #execStart}
   */
  enum ExecStartParameter {
    DETACH("Detach"),
    TTY("Tty");

    private final String name;

    ExecStartParameter(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  /**
   * Resizes the tty session used by an exec command.
   * This API is valid only if <code>tty</code> was specified as part
   * of {@link #execCreate(String, String[], ExecCreateParam...) creating} and
   * {@link #execStart(String, ExecStartParameter...) starting} the exec command.
   * @param execId exec id
   * @param height height of tty session
   * @param width width of tty session
   *
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                              if both height and width are null or zero
   * @throws com.spotify.docker.client.exceptions.ExecNotFoundException
   *                              if exec instance is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void execResizeTty(String execId, Integer height, Integer width)
          throws DockerException, InterruptedException;

  /**
   * Inspects a running or previously run exec instance id.
   *
   * @param execId exec id
   * @return state of this exec instance.
   * @throws com.spotify.docker.client.exceptions.ExecNotFoundException
   *                              if exec instance is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ExecState execInspect(String execId) throws DockerException, InterruptedException;

  /**
   * Retrieves one-time stats (stream=0) for the container with the specified id.
   *
   * @param containerId The id of the container to retrieve stats for.
   * @return The container stats
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ContainerStats stats(String containerId) throws DockerException, InterruptedException;

  /**
   * Resize container TTY
   * This API is valid only if <code>tty</code> was specified as
   * part of {@link #createContainer(ContainerConfig) creating} the container.
   *
   * @param containerId The id of the container whose TTY will be resized.
   * @param height New height of TTY
   * @param width New width of TTY
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                              if both height and width are null or zero
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void resizeTty(String containerId, Integer height, Integer width)
      throws DockerException, InterruptedException;


  /**
   * List all networks
   *
   * @return networks
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<Network> listNetworks() throws DockerException, InterruptedException;

  /**
   * Inspect a specific network
   *
   * @param networkId The id of the network
   * @return network information
   * @throws com.spotify.docker.client.exceptions.NetworkNotFoundException
   *                              if network is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  Network inspectNetwork(final String networkId) throws DockerException, InterruptedException;

  /**
   * Create a new network
   *
   * @param networkConfig The network creation parameters
   * @return NetworkCreation
   * @throws com.spotify.docker.client.exceptions.NetworkNotFoundException
   *                              if network is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  NetworkCreation createNetwork(final NetworkConfig networkConfig)
      throws DockerException, InterruptedException;

  /**
   * Remove a docker network.
   *
   * @param networkId The id of the network to remove.
   * @throws com.spotify.docker.client.exceptions.NetworkNotFoundException
   *                              if network is not found (404)
   * @throws DockerException      if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void removeNetwork(String networkId) throws DockerException, InterruptedException;


  /**
   * Connects a docker container to a network.
   *
   * @param containerId The id of the container to connect.
   * @param networkId   The id of the network to connect.
   * @throws com.spotify.docker.client.exceptions.NotFoundException
   *                              if either container or network is not found (404)
   * @throws DockerException            if a server error occurred (500)
   * @throws InterruptedException       If the thread is interrupted
   */
  void connectToNetwork(String containerId, String networkId)
      throws DockerException, InterruptedException;


  /**
   * Disconnects a docker container to a network.
   *
   * @param containerId The id of the container to disconnect.
   * @param networkId   The id of the network to disconnect.
   * @throws com.spotify.docker.client.exceptions.NotFoundException
   *                              if either container or network is not found (404)
   * @throws DockerException            if a server error occurred (500)
   * @throws InterruptedException       If the thread is interrupted
   */
  void disconnectFromNetwork(String containerId, String networkId)
      throws DockerException, InterruptedException;

  /**
   * Closes any and all underlying connections to docker, and release resources.
   */
  @Override
  void close();

  /**
   * Parameters for {@link #execCreate(String, String[], ExecCreateParam...)}
   */
  class ExecCreateParam {

    private final String name;
    private final String value;

    private ExecCreateParam(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    public String name() {
      return name;
    }

    public String value() {
      return value;
    }

    private static ExecCreateParam create(final String name, final String value) {
      return new ExecCreateParam(name, value);
    }

    /**
     * Execute in detached mode
     *
     * @param detach Whether to detach.
     * @return ExecCreateParam
     */
    public static ExecCreateParam detach(final boolean detach) {
      return create("Detach", String.valueOf(detach));
    }

    /**
     * Execute in detached mode
     *
     * @return ExecCreateParam
     */
    public static ExecCreateParam detach() {
      return detach(true);
    }

    /**
     * Attach stdin
     *
     * @param attachStdin Whether to attach the standard input which allows user interaction.
     * @return ExecCreateParam
     */
    public static ExecCreateParam attachStdin(final boolean attachStdin) {
      return create("AttachStdin", String.valueOf(attachStdin));
    }

    /**
     * Attach standard input
     *
     * @return ExecCreateParam
     */
    public static ExecCreateParam attachStdin() {
      return attachStdin(true);
    }

    /**
     * Attach standard error
     *
     * @param attachStderr Whether to attach standout error
     * @return ExecCreateParam
     */
    public static ExecCreateParam attachStderr(final boolean attachStderr) {
      return create("AttachStderr", String.valueOf(attachStderr));
    }

    /**
     * Attach standard error
     *
     * @return ExecCreateParam
     */
    public static ExecCreateParam attachStderr() {
      return attachStderr(true);
    }

    /**
     * Attach standard ouput
     *
     * @param attachStdout Whether to attach standard output
     * @return ExecCreateParam
     */
    public static ExecCreateParam attachStdout(final boolean attachStdout) {
      return create("AttachStdout", String.valueOf(attachStdout));
    }

    /**
     * Attach standard ouput
     *
     * @return ExecCreateParam
     */
    public static ExecCreateParam attachStdout() {
      return attachStdout(true);
    }

    /**
     * Give extended privileges to the command
     *
     * @param privileged Whether to give extended privileges to the command
     * @return ExecCreateParam
     */
    public static ExecCreateParam privileged(final boolean privileged) {
      return create("Privileged", String.valueOf(privileged));
    }

    /**
     * Give extended privileges to the command
     *
     * @return ExecCreateParam
     */
    public static ExecCreateParam privileged() {
      return privileged(true);
    }

    /**
     * Attach standard streams to a tty.
     *
     * @param tty Whether to attach standard streams to a tty.
     * @return ExecCreateParam
     */
    public static ExecCreateParam tty(final boolean tty) {
      return create("Tty", String.valueOf(tty));
    }

    /**
     * Attach standard streams to a tty.
     *
     * @return ExecCreateParam
     */
    public static ExecCreateParam tty() {
      return tty(true);
    }

    /**
     * User that will run the command
     *
     * @param user user
     * @return ExecCreateParam
     */
    public static ExecCreateParam user(final String user) {
      return create("User", user);
    }
  }


  /**
   * Parameters for {@link #logs(String, LogsParam...)}
   */

  class LogsParam {

    private final String name;
    private final String value;

    public LogsParam(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Parameter name.
     *
     * @return name of parameter
     */
    public String name() {
      return name;
    }

    /**
     * Parameter value.
     *
     * @return value of parameter
     */
    public String value() {
      return value;
    }

    /**
     * Return stream.
     *
     * @return LogsParam
     */
    public static LogsParam follow() {
      return follow(true);
    }

    /**
     * Return stream. Default false.
     *
     * @param follow Whether to return stream.
     * @return LogsParam
     */
    public static LogsParam follow(final boolean follow) {
      return create("follow", String.valueOf(follow));
    }

    /**
     * Show stdout log.
     *
     * @return LogsParam
     */
    public static LogsParam stdout() {
      return stdout(true);
    }

    /**
     * Show stdout log. Default false.
     *
     * @param stdout Whether to show stdout log.
     * @return LogsParam
     */
    public static LogsParam stdout(final boolean stdout) {
      return create("stdout", String.valueOf(stdout));
    }

    /**
     * Show stderr log.
     *
     * @return LogsParam
     */
    public static LogsParam stderr() {
      return stderr(true);
    }

    /**
     * Show stderr log. Default false.
     *
     * @param stderr Whether to show stderr log.
     * @return LogsParam
     */
    public static LogsParam stderr(final boolean stderr) {
      return create("stderr", String.valueOf(stderr));
    }

    /**
     * Filter logs and only output entries since given Unix timestamp. Only available in Docker API
     * &gt;= 1.19.
     *
     * @param timestamp Only output entries since timestamp.
     * @return LogsParam
     */
    public static LogsParam since(final Integer timestamp) {
      return create("since", String.valueOf(timestamp));
    }

    /**
     * Print timestamp for every log line.
     *
     * @return LogsParam
     */
    public static LogsParam timestamps() {
      return timestamps(true);
    }

    /**
     * Print timestamp for every log line. Default false.
     *
     * @param timestamps Whether to print timestamp for every log line.
     * @return LogsParam
     */
    public static LogsParam timestamps(final boolean timestamps) {
      return create("timestamps", String.valueOf(timestamps));
    }

    /**
     * Output specified number of lines at the end of logs.
     *
     * @param lines Number of lines to output at the end of logs.
     * @return LogsParam
     */
    public static LogsParam tail(final Integer lines) {
      return create("tail", String.valueOf(lines));
    }

    /**
     * Create a custom parameter.
     *
     * @param name  custom name
     * @param value custom value
     * @return LogsParam
     */
    public static LogsParam create(final String name, final String value) {
      return new LogsParam(name, value);
    }
  }

  /**
   * Parameters for {@link #attachContainer(String, AttachParameter...)}
   */
  enum AttachParameter {
    LOGS,
    STREAM,
    STDIN,
    STDOUT,
    STDERR
  }

  /**
   * Attach to the container id
   *
   * @param containerId The id of the container to get logs for.
   * @param params      Params for controlling what streams to get and whether to tail or not.
   * @return A log message stream.
   * @throws com.spotify.docker.client.exceptions.BadParamException
   *                            if one or more params were bad (400)
   * @throws com.spotify.docker.client.exceptions.ContainerNotFoundException
   *                              if container is not found (404)
   * @throws DockerException            if a server error occurred (500)
   * @throws InterruptedException       If the thread is interrupted
   */
  LogStream attachContainer(String containerId, AttachParameter... params)
      throws DockerException, InterruptedException;

  /**
   * Get the Docker host address
   *
   * @return the docker host name or IP
   */
  String getHost();

  /**
   * Parameters for {@link #listContainers(ListContainersParam...)}
   */
  class ListContainersParam {

    private final String name;
    private final String value;

    public ListContainersParam(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Create a custom parameter.
     *
     * @param name  custom name
     * @param value custom value
     * @return ListContainersParam
     */
    public static ListContainersParam create(final String name, final String value) {
      return new ListContainersParam(name, value);
    }

    /**
     * Create a "filters" query param from a key/value pair
     *
     * @param key   Type of filter
     * @param value Value of filter
     * @return ListContainersParam
     */
    public static ListContainersParam filter(final String key, final String value) {
      return new ListContainersFilterParam(key, value);
    }

    /**
     * Parameter name.
     *
     * @return name of parameter
     */
    public String name() {
      return name;
    }

    /**
     * Parameter value.
     *
     * @return value of parameter
     */
    public String value() {
      return value;
    }

    /**
     * Show all containers. Only running containers are shown by default
     *
     * @return ListContainersParam
     */
    public static ListContainersParam allContainers() {
      return allContainers(true);
    }

    /**
     * Show all containers. Only running containers are shown by default
     *
     * @param all Whether to show all containers
     * @return ListContainersParam
     */
    public static ListContainersParam allContainers(final boolean all) {
      return create("all", all ? "1" : "0");
    }

    /**
     * Show <code>limit</code> last created containers, include non-running ones.
     *
     * @param limit Limit for number of containers to list
     * @return ListContainersParam
     */
    public static ListContainersParam limitContainers(final Integer limit) {
      return create("limit", String.valueOf(limit));
    }

    /**
     * Show only containers created since id, include non-running ones.
     *
     * @param id container ID
     * @return ListContainersParam
     */
    public static ListContainersParam containersCreatedSince(final String id) {
      return create("since", id);
    }

    /**
     * Show only containers created before id, include non-running ones.
     *
     * @param id container ID
     * @return ListContainersParam
     */
    public static ListContainersParam containersCreatedBefore(final String id) {
      return create("before", String.valueOf(id));
    }

    /**
     * Show the containers sizes.
     *
     * @param size Whether to show container sizes
     * @return ListContainersParam
     */
    public static ListContainersParam withContainerSizes(final Boolean size) {
      return create("size", String.valueOf(size));
    }

    /**
     * Show exited containers with given exit status.
     *
     * @param exitStatus Integer exit status
     * @return ListContainersParam
     */
    public static ListContainersParam withExitStatus(final int exitStatus) {
      return filter("exited", String.valueOf(exitStatus));
    }

    /**
     * Show created containers.
     *
     * @return ListContainersParam
     */
    public static ListContainersParam withStatusCreated() {
      return filter("status", "created");
    }

    /**
     * Show restarting containers.
     *
     * @return ListContainersParam
     */
    public static ListContainersParam withStatusRestarting() {
      return filter("status", "restarting");
    }

    /**
     * Show running containers.
     *
     * @return ListContainersParam
     */
    public static ListContainersParam withStatusRunning() {
      return filter("status", "running");
    }

    /**
     * Show paused containers.
     *
     * @return ListContainersParam
     */
    public static ListContainersParam withStatusPaused() {
      return filter("status", "paused");
    }

    /**
     * Show exited containers.
     *
     * @return ListContainersParam
     */
    public static ListContainersParam withStatusExited() {
      return filter("status", "exited");
    }

    /**
     * Show exited containers.
     *
     * @return ListContainersParam
     * @deprecated Replaced by {@link #withStatusExited()}
     */
    @Deprecated
    public static ListContainersParam exitedContainers() {
      return withStatusExited();
    }

    /**
     * Show containers with a label value.
     *
     * @param label The label to filter on
     * @param value The value of the label
     * @return ListContainersParam
     */
    public static ListContainersParam withLabel(final String label, final String value) {
      return isNullOrEmpty(value) ? filter("label", label) : filter("label", label + "=" + value);
    }

    /**
     * Show containers with a label.
     *
     * @param label The label to filter on
     * @return ListContainersParam
     */
    public static ListContainersParam withLabel(final String label) {
      return withLabel(label, null);
    }
  }

  class ListContainersFilterParam extends ListContainersParam {

    public ListContainersFilterParam(String name, String value) {
      super(name, value);
    }
  }

  /**
   * Parameters for {@link #listImages(ListImagesParam...)}.
   */
  class ListImagesParam {

    private final String name;
    private final String value;

    public ListImagesParam(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Create a custom parameter.
     *
     * @param name  of parameter
     * @param value of parameter
     * @return ListImagesParam
     */
    public static ListImagesParam create(final String name, final String value) {
      return new ListImagesParam(name, value);
    }

    /**
     * Create a custom filter.
     *
     * @param name  of filter
     * @param value of filter
     * @return ListImagesParam
     */
    public static ListImagesParam filter(final String name, final String value) {
      return new ListImagesFilterParam(name, value);
    }

    /**
     * Parameter name.
     *
     * @return name of parameter
     */
    public String name() {
      return name;
    }

    /**
     * Parameter value.
     *
     * @return value of parameter
     */
    public String value() {
      return value;
    }

    /**
     * Show all images. Only intermediate image layers are shown by default.
     *
     * @return ListImagesParam
     */
    public static ListImagesParam allImages() {
      return allImages(true);
    }

    /**
     * Show all images. Only intermediate image layers are shown by default.
     *
     * @param all Whether to list all images
     * @return ListImagesParam
     */
    public static ListImagesParam allImages(final boolean all) {
      return create("all", String.valueOf(all));
    }

    /**
     * Show digests.
     *
     * @return ListImagesParam
     */
    public static ListImagesParam digests() {
      return create("digests", "1");
    }

    /**
     * Show dangling images only. A dangling image is one which does not have a repository name. By
     * default both dangling and non-dangling will be shown.
     *
     * @return ListImagesParam
     */
    public static ListImagesParam danglingImages() {
      return danglingImages(true);
    }

    /**
     * Enable or disable dangling image filter.
     *
     * @param dangling Whether to list dangling images
     * @return ListImagesParam
     */
    public static ListImagesParam danglingImages(final boolean dangling) {
      return filter("dangling", String.valueOf(dangling));
    }

    /**
     * Show images with a label value.
     *
     * @param label The label to filter on
     * @param value The value of the label
     * @return ListImagesParam
     */
    public static ListImagesParam withLabel(final String label, final String value) {
      return isNullOrEmpty(value) ? filter("label", label) : filter("label", label + "=" + value);
    }

    /**
     * Show images with a label.
     *
     * @param label The label to filter on
     * @return ListImagesParam
     */
    public static ListImagesParam withLabel(final String label) {
      return withLabel(label, null);
    }

    /**
     * Show images by name. Can use RepoTags or RepoDigests as valid inputs.
     *
     * @param name Name of the image to filter on
     * @return ListImagesParam
     */
    public static ListImagesParam byName(final String name) {
      return create("filter", name);
    }
  }

  /**
   * Filter parameter for {@link #listImages(ListImagesParam...)}. This should be used by
   * ListImagesParam only.
   */
  class ListImagesFilterParam extends ListImagesParam {

    public ListImagesFilterParam(String name, String value) {
      super(name, value);
    }
  }

  /**
   * Parameters for {@link #events(EventsParam...)}
   */
  class EventsParam {

    private final String name;
    private final String value;

    private EventsParam(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Parameter name.
     *
     * @return The name
     */
    public String name() {
      return name;
    }

    /**
     * Parameter value.
     *
     * @return The value
     */
    public String value() {
      return value;
    }

    /**
     * Filter events until the given timestamp
     *
     * @param until Return events up until this Unix timestamp.
     * @return {@link EventsParam}
     */
    public static EventsParam until(Long until) {
      return new EventsParam("until", String.valueOf(until));
    }

    /**
     * Filter events since the given timestamp
     *
     * @param since Return events since this Unix timestamp.
     * @return {@link EventsParam}
     */
    public static EventsParam since(Long since) {
      return new EventsParam("since", String.valueOf(since));
    }

    /**
     * Apply filters to the returned events
     *
     * @param name  Name
     * @param value Value
     * @return {@link EventsParam}
     */
    private static EventsParam filter(String name, String value) {
      return new EventsFilterParam(name, value);
    }

    /**
     * Show only certain events. For example, "event=pull" for image pull events.
     * @param event Type of event to show
     * @return EventsParam
     */
    public static EventsParam event(final String event) {
      return filter("event", event);
    }

    /**
     * Show events for an image.
     * @param image An image tag or id
     * @return EventsParam
     */
    public static EventsParam image(final String image) {
      return filter("image", image);
    }

    /**
     * Show events for a container.
     * @param container A container name or id
     * @return EventsParam
     */
    public static EventsParam container(final String container) {
      return filter("container", container);
    }

    /**
     * Show events for a volume.
     * @param volume A volume name or id
     * @return EventsParam
     */
    public static EventsParam volume(final String volume) {
      return filter("volume", volume);
    }

    /**
     * Show events for a network.
     * @param network A network name or id
     * @return EventsParam
     */
    public static EventsParam network(final String network) {
      return filter("network", network);
    }

    /**
     * Show events for a daemon.
     * @param daemon A daemon name or id
     * @return EventsParam
     */
    public static EventsParam daemon(final String daemon) {
      return filter("daemon", daemon);
    }

    /**
     * Show events of a given type. For instance, "type=image" for all image events.
     * @param type A type of event. Possible values: container, image, volume, network, or daemon
     * @return EventsParam
     */
    public static EventsParam type(final String type) {
      return filter("type", type);
    }

    /**
     * Show events with a label value.
     *
     * @param label The label to filter on
     * @param value The value of the label
     * @return EventsParam
     */
    public static EventsParam label(final String label, final String value) {
      return isNullOrEmpty(value) ? filter("label", label) : filter("label", label + "=" + value);
    }

    /**
     * Show events with a label value.
     *
     * @param label The label to filter on
     * @return EventsParam
     */
    public static EventsParam label(final String label) {
      return label(label, null);
    }
  }

  /**
   * Filter parameter for {@link #events(EventsParam...)}. This should be used by EventsParam only.
   */
  class EventsFilterParam extends EventsParam {

    public EventsFilterParam(String name, String value) {
      super(name, value);
    }
  }

  Volume createVolume() throws DockerException, InterruptedException;

  Volume createVolume(Volume volume) throws DockerException, InterruptedException;

  Volume inspectVolume(String volumeName) throws DockerException, InterruptedException;

  void removeVolume(Volume volume) throws DockerException, InterruptedException;

  void removeVolume(String volumeName) throws DockerException, InterruptedException;

  VolumeList listVolumes(ListVolumesParam... params)
    throws DockerException, InterruptedException;

  /**
   * Parameters for {@link #listVolumes(ListVolumesParam...)}.
   * @since Docker 1.9, API version 1.21
   */
  class ListVolumesParam {
    private final String name;
    private final String value;

    private ListVolumesParam(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    /**
     * Parameter name.
     *
     * @return name of parameter
     * @since Docker 1.9, API version 1.21
     */
    public String name() {
      return name;
    }

    /**
     * Parameter value.
     *
     * @return value of parameter
     * @since Docker 1.9, API version 1.21
     */
    public String value() {
      return value;
    }

    /**
     * Create a custom filter.
     *
     * @param name  of filter
     * @param value of filter
     * @return ListVolumesParam
     * @since Docker 1.9, API version 1.21
     */
    public static ListVolumesParam filter(final String name, final String value) {
      return new ListVolumesFilterParam(name, value);
    }

    /**
     * Show dangling volumes only.
     * A dangling volume is one which is not referenced by any container.
     * By default both dangling and non-dangling will be shown.
     *
     * @return ListVolumesParam
     * @since Docker 1.9, API version 1.21
     */
    public static ListVolumesParam dangling() {
      return dangling(true);
    }

    /**
     * Enable or disable dangling volume filter.
     *
     * @param dangling Whether to list dangling images
     * @return ListVolumesParam
     */
    public static ListVolumesParam dangling(final Boolean dangling) {
      return filter("dangling", dangling.toString());
    }

    /**
     * Filter volumes by name.
     * @param name Matches all or part of a volume name.
     * @return ListVolumesParam
     * @since Docker 1.12, API version 1.24
     */
    public static ListVolumesParam name(final String name) {
      return filter("name", name);
    }

    /**
     * Filter volumes by volume driver.
     * @param driver Matches all or part of a volume driver name.
     * @return ListVolumesParam
     * @since Docker 1.12, API version 1.24
     */
    public static ListVolumesParam driver(final String driver) {
      return filter("driver", driver);
    }
  }

  /**
   * Filter parameter for {@link #listVolumes(ListVolumesParam...)}. This should be used by
   * ListVolumesParam only.
   * @since Docker 1.9, API version 1.21
   */
  class ListVolumesFilterParam extends ListVolumesParam {
    public ListVolumesFilterParam(String name, String value) {
      super(name, value);
    }
  }

}
