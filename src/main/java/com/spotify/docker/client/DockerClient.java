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
import com.spotify.docker.client.messages.RemovedImage;
import com.spotify.docker.client.messages.Version;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
   * @return String "OK"
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  String ping() throws DockerException, InterruptedException;

  /**
   * Get the docker version.
   *
   * @return docker version
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  Version version() throws DockerException, InterruptedException;

  /**
   * Check auth configuration.
   *
   * @param authConfig The authentication config needed to pull the image.
   * @return status code of auth request
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  int auth(final AuthConfig authConfig) throws DockerException, InterruptedException;

  /**
   * Get docker instance information.
   *
   * @return docker info
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  Info info() throws DockerException, InterruptedException;

  /**
   * List docker containers.
   *
   * @param params Container listing and filtering options.
   * @return A list of containers.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<Container> listContainers(ListContainersParam... params)
      throws DockerException, InterruptedException;

  /**
   * List docker images.
   *
   * @param params Image listing and filtering options.
   * @return A list of images.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<Image> listImages(ListImagesParam... params) throws DockerException, InterruptedException;

  /**
   * Inspect a docker container.
   *
   * @param containerId The id of the container to inspect.
   * @return Info about the container.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ContainerInfo inspectContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Create a new image from a container's changes.
   *
   * @param containerId The id of the container to commit.
   * @param comment commit message.
   * @param author image author.
   * @param tag image tag.
   * @param repo repository to commit to.
   * @param config ContainerConfig to commit.
   * @return ContainerCreation reply.
   * @throws DockerException if a server error occurred (500)
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
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ImageInfo inspectImage(String image) throws DockerException, InterruptedException;

  /**
   * Remove a docker image.
   *
   * @param image The image to remove.
   * @return A list describing each image which was removed.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<RemovedImage> removeImage(String image) throws DockerException, InterruptedException;


  /**
   * Remove a docker image.
   *
   * @param image The image to remove.
   * @param force Force image removal.
   * @param noPrune Do not delete untagged parents.
   * @return A list describing each image which was removed.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<RemovedImage> removeImage(String image, boolean force, boolean noPrune)
      throws DockerException, InterruptedException;

  /**
   * Search for images on Docker Hub
   * @param term the search term
   * @return a list of matches for the given search term
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  List<ImageSearchResult> searchImages(String term) throws DockerException, InterruptedException;
  
  /**
   * Pull a docker container image.
   *
   * @param image The image to pull.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void pull(String image) throws DockerException, InterruptedException;

  /**
   * Pull a docker container image, using a custom ProgressMessageHandler
   *
   * @param image The image to pull.
   * @param handler The handler to use for processing each progress message received from Docker.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void pull(String image, ProgressHandler handler) throws DockerException, InterruptedException;

   /**
   * Pull a private docker container image.
   *
   * @param image The image to pull.
   * @param authConfig The authentication config needed to pull the image.
    * @throws DockerException if a server error occurred (500)
    * @throws InterruptedException If the thread is interrupted
   */
   void pull(String image, AuthConfig authConfig) throws DockerException, InterruptedException;

  /**
   * Pull a private docker container image, using a custom ProgressMessageHandler.
   *
   * @param image The image to pull.
   * @param authConfig The authentication config needed to pull the image.
   * @param handler The handler to use for processing each progress message received from Docker.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void pull(String image, AuthConfig authConfig, ProgressHandler handler)
      throws DockerException, InterruptedException;

  /**
   * Push a docker container image.
   *
   * @param image The image to push.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void push(String image) throws DockerException, InterruptedException;

  /**
   * Push a docker container image, using a custom ProgressHandler
   *
   * @param image The image to push.
   * @param handler The handler to use for processing each progress message received from Docker.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void push(String image, ProgressHandler handler) throws DockerException, InterruptedException;

  /**
   * Tag a docker image.
   *
   * @param image The image to tag.
   * @param name The new name that will be applied to the image.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void tag(final String image, final String name) throws DockerException, InterruptedException;

  /**
   * Tag a docker image.
   *
   * @param image The image to tag.
   * @param name The new name that will be applied to the image.
   * @param force Whether to force the tagging even if the tag is already assigned to another image.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void tag(final String image, final String name, final boolean force)
      throws DockerException, InterruptedException;

  /**
   * Build a docker image.
   *
   * @param directory The directory containing the dockerfile.
   * @param params Additional flags to use during build.
   * @return The id of the built image if successful, otherwise null.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws IOException If some IO shit happened.
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
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws IOException If some IO shit happened.
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
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws IOException If some IO shit happened.
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
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   * @throws IOException If some IO shit happened.
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
   * @throws DockerException if a server error occurred (500)
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
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ContainerCreation createContainer(ContainerConfig config, String name)
      throws DockerException, InterruptedException;

  /**
   * Start a docker container.
   *
   * @param containerId The id of the container to start.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void startContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Start a docker container.
   *
   * @param containerId The id of the container to start.
   * @param hostConfig  The docker host configuration to use when starting the container.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void startContainer(String containerId, HostConfig hostConfig)
      throws DockerException, InterruptedException;

  /**
   * Stop a docker container by sending a SIGTERM, and following up with a SIGKILL if the
   * container doesn't exit gracefully and in a timely manner.
   *
   * @param containerId The id of the container to stop.
   * @param secondsToWaitBeforeKilling Time to wait after SIGTERM before sending SIGKILL.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void stopContainer(String containerId, int secondsToWaitBeforeKilling)
      throws DockerException, InterruptedException;

  /**
   * Pause a docker container.
   *
   * @param containerId The id of the container to pause.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void pauseContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Unpause a docker container.
   *
   * @param containerId The id of the container to pause.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */

  void unpauseContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Restart a docker container. with a 10 second default wait
   *
   * @param containerId The id of the container to restart.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void restartContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Restart a docker container.
   *
   * @param containerId                The id of the container to restart.
   * @param secondsToWaitBeforeRestart number of seconds to wait before killing the container.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void restartContainer(String containerId, int secondsToWaitBeforeRestart)
      throws DockerException, InterruptedException;

  /**
   * Wait for a docker container to exit.
   *
   * @param containerId The id of the container to wait for.
   * @return Exit response with status code.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ContainerExit waitContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Kill a docker container.
   *
   * @param containerId The id of the container to kill.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void killContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Remove a docker container.
   *
   * @param containerId The id of the container to remove.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void removeContainer(String containerId) throws DockerException, InterruptedException;

  /**
   * Remove a docker container.
   *
   * @param containerId   The id of the container to remove.
   * @param removeVolumes Whether to remove volumes as well.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  void removeContainer(String containerId, boolean removeVolumes)
      throws DockerException, InterruptedException;

  /**
   * Export a docker container as a tar archive.
   *
   * @param containerId The id of the container to export.
   * @return A stream in tar format that contains the contents of the container file system.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  InputStream exportContainer(String containerId) throws DockerException, InterruptedException;

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
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  InputStream copyContainer(String containerId, String path)
      throws DockerException, InterruptedException;

  /**
   * Get docker container logs.
   *
   * @param containerId The id of the container to get logs for.
   * @param params      Params for controlling what streams to get and whether to tail or not.
   * @return A log message stream.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  LogStream logs(String containerId, LogsParameter... params)
      throws DockerException, InterruptedException;


  /**
   * Sets up an exec instance in a running container id.
   *
   * @param containerId The id of the container
   * @param cmd shell command
   * @param params Exec params
   * @return exec id
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  String execCreate(String containerId, String[] cmd, ExecParameter... params)
      throws DockerException, InterruptedException;

  /**
   * Supported parameters for {@link #execCreate}
   */
  public static enum ExecParameter {
    STDOUT("AttachStdout"),
    STDERR("AttachStderr");

    private final String name;

    ExecParameter(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  /**
   * Starts a previously set up exec instance id.
   * If detach is true, this API returns after starting the exec command.
   * Otherwise, this API sets up an interactive session with the exec command.
   *
   * @param execId exec id
   * @param params Exec start params
   * @return exec output
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  LogStream execStart(String execId, ExecStartParameter... params)
      throws DockerException, InterruptedException;

  /**
   * Supported parameters for {@link #execStart}
   */
  public static enum ExecStartParameter {
    DETACH("Detach");

    private final String name;

    ExecStartParameter(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  /**
   * Inspects a running or previously run exec instance id.
   *
   * @param execId exec id
   * @return state of this exec instance.
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  ExecState execInspect(String execId) throws DockerException, InterruptedException;

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
   * Parameters for {@link #attachContainer(String, AttachParameter...)}
   */
  public static enum AttachParameter {
    LOGS,
    STREAM,
    STDIN,
    STDOUT,
    STDERR
  }

  /**
   * Attach to the container id
   * @param containerId The id of the container to get logs for.
   * @param params      Params for controlling what streams to get and whether to tail or not.
   * @return A log message stream.
   * @throws ContainerNotFoundException if the container was not found (404).
   * @throws DockerException if a server error occurred (500)
   * @throws InterruptedException If the thread is interrupted
   */
  LogStream attachContainer(String containerId, AttachParameter... params)
      throws DockerException, InterruptedException;

  /**
   * Get the Docker host address
   * @return the docker host name or IP
   */
  String getHost();
  
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
     * Show exited containers.
     *
     * @return ListContainersParam
     */
    public static ListContainersParam exitedContainers() throws UnsupportedEncodingException {
      return create("filters", URLEncoder.encode("{\"status\":[\"exited\"]}", "UTF-8"));
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
      return create("since", String.valueOf(id));
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
     * Create a custom parameter.
     *
     * @param name custom name
     * @param value custom value
     * @return ListContainersParam
     */
    public static ListContainersParam create(final String name, final String value) {
      return new ListContainersParam(name, value);
    }
  }

  /**
   * Parameters for {@link #listImages(ListImagesParam...)}
   */
  public static class ListImagesParam {

    private final String name;
    private final String value;

    public ListImagesParam(final String name, final String value) {
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
     * Show dangling images only. A dangling image is one which does not have a repository name.
     * By default both dangling and non-dangling will be shown.
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
     * Create a custom filter.
     *
     * @param name of filter
     * @param value of filter
     * @return ListImagesParam
     */
    public static ListImagesParam filter(final String name, final String value) {
      return new ListImagesFilterParam(name, value);
    }

    /**
     * Create a custom parameter.
     *
     * @param name of parameter
     * @param value of parameter
     * @return ListImagesParam
     */
    public static ListImagesParam create(final String name, final String value) {
      return new ListImagesParam(name, value);
    }
  }

  /**
   * Filter parameter for {@link #listImages(ListImagesParam...)}. This should be used by
   * ListImagesParam only.
   */
  static class ListImagesFilterParam extends ListImagesParam {
    public ListImagesFilterParam(String name, String value) {
      super(name, value);
    }
  }
}
