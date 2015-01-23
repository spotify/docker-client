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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
import com.spotify.docker.client.messages.Version;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import org.apache.http.conn.ConnectTimeoutException;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.spotify.docker.client.CompressedDirectory.delete;
import static com.spotify.docker.client.ObjectMapperProvider.objectMapper;
import static com.sun.jersey.api.client.config.ClientConfig.PROPERTY_READ_TIMEOUT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;

public class DefaultDockerClient implements DockerClient, Closeable {

  public static final long NO_TIMEOUT = 0;

  private static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = SECONDS.toMillis(5);
  private static final long DEFAULT_READ_TIMEOUT_MILLIS = SECONDS.toMillis(30);

  private static final String VERSION = "v1.12";
  private static final DefaultClientConfig CLIENT_CONFIG = new DefaultClientConfig(
      ObjectMapperProvider.class,
      LogsResponseReader.class,
      ProgressResponseReader.class);

  private static final Pattern CONTAINER_NAME_PATTERN = Pattern.compile("/?[a-zA-Z0-9_-]+");

  private static final GenericType<List<Container>> CONTAINER_LIST =
      new GenericType<List<Container>>() {};

  private static final GenericType<List<Image>> IMAGE_LIST =
      new GenericType<List<Image>>() {};

  private static final GenericType<List<RemovedImage>> REMOVED_IMAGE_LIST =
      new GenericType<List<RemovedImage>>() {};

  private static final AtomicInteger CLIENT_COUNTER = new AtomicInteger();

  private static final URI UNIX_SOCKET_URI = URI.create("unix://localhost");

  private final ExecutorService executor = MoreExecutors.getExitingExecutorService(
      (ThreadPoolExecutor) Executors.newCachedThreadPool(
          new ThreadFactoryBuilder()
              .setDaemon(true)
              .setNameFormat("docker-client-" + CLIENT_COUNTER.incrementAndGet() + "-%d")
              .build()));


  private final TerminatingClientHandler clientHandler;

  private final Client client;
  private final URI uri;

  /**
   * Create a new client with default configuration.
   * @param uri The docker rest api uri.
   */
  public DefaultDockerClient(final String uri) {
    this(URI.create(uri));
  }

  /**
   * Create a new client with default configuration.
   * @param uri The docker rest api uri.
   */
  public DefaultDockerClient(final URI uri) {
    final URI originalUri = checkNotNull(uri, "uri");
    this.clientHandler = new InterruptibleApacheClientHandler(originalUri, executor);
    this.client = new Client(clientHandler, CLIENT_CONFIG);
    this.client.setConnectTimeout((int) DEFAULT_CONNECT_TIMEOUT_MILLIS);
    this.client.setReadTimeout((int) DEFAULT_READ_TIMEOUT_MILLIS);

    if (originalUri.getScheme().equals("unix")) {
      this.uri = UNIX_SOCKET_URI;
    } else {
      this.uri = originalUri;
    }
  }

  /**
   * Create a new client using the configuration of the builder.
   */
  private DefaultDockerClient(final Builder builder) {
    final URI originalUri = checkNotNull(builder.uri, "uri");
    this.clientHandler = new InterruptibleApacheClientHandler(originalUri, executor);
    this.client = new Client(clientHandler, CLIENT_CONFIG);
    this.client.setConnectTimeout((int) builder.connectTimeoutMillis);
    this.client.setReadTimeout((int) builder.readTimeoutMillis);

    if (originalUri.getScheme().equals("unix")) {
      this.uri = UNIX_SOCKET_URI;
    } else {
      this.uri = originalUri;
    }
  }

  @Override
  public void close() {
    executor.shutdownNow();
    client.destroy();
  }

  @Override
  public String ping() throws DockerException, InterruptedException {
    final WebResource resource = client.resource(uri).path("_ping");
    return request(GET, String.class, resource, resource);
  }

  @Override
  public Version version() throws DockerException, InterruptedException {
    final WebResource resource = resource().path("version");
    return request(GET, Version.class, resource, resource.accept(APPLICATION_JSON_TYPE));
  }

  @Override
  public Info info() throws DockerException, InterruptedException {
    final WebResource resource = resource().path("info");
    return request(GET, Info.class, resource, resource.accept(APPLICATION_JSON_TYPE));
  }

  @Override
  public List<Container> listContainers(final ListContainersParam... params)
      throws DockerException, InterruptedException {
    final Multimap<String, String> paramMap = ArrayListMultimap.create();
    for (ListContainersParam param : params) {
      paramMap.put(param.name(), param.value());
    }
    final WebResource resource = resource()
        .path("containers").path("json")
        .queryParams(multivaluedMap(paramMap));
    return request(GET, CONTAINER_LIST, resource, resource.accept(APPLICATION_JSON_TYPE));
  }

  @Override
  public List<Image> listImages(ListImagesParam... params)
      throws DockerException, InterruptedException {
    final MultivaluedMap<String, String> paramMap = new MultivaluedMapImpl();
    final Map<String, String> filters = newHashMap();
    for (ListImagesParam param : params) {
      if (param instanceof ListImagesFilterParam) {
        filters.put(param.name(), param.value());
      } else {
        paramMap.putSingle(param.name(), param.value());
      }
    }

    // If filters were specified, we must put them in a JSON object and pass them using the
    // 'filters' query param like this: filters={"dangling":["true"]}
    try {
      if (!filters.isEmpty()) {
        final StringWriter writer = new StringWriter();
        final JsonGenerator generator = objectMapper().getFactory().createGenerator(writer);
        generator.writeStartObject();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
          generator.writeArrayFieldStart(entry.getKey());
          generator.writeString(entry.getValue());
          generator.writeEndArray();
        }
        generator.writeEndObject();
        generator.close();
        // We must URL encode the string, otherwise Jersey chokes on the double-quotes in the json.
        final String encoded = URLEncoder.encode(writer.toString(), UTF_8.name());
        paramMap.putSingle("filters", encoded);
      }
    } catch (IOException e) {
      throw new DockerException(e);
    }

    final WebResource resource = resource()
        .path("images").path("json")
        .queryParams(paramMap);
    return request(GET, IMAGE_LIST, resource, resource.accept(APPLICATION_JSON_TYPE));
  }

  @Override
  public ContainerCreation createContainer(final ContainerConfig config)
      throws DockerException, InterruptedException {
    return createContainer(config, null);
  }

  public static interface ExceptionPropagator {

    void propagate(UniformInterfaceException e) throws DockerException;
  }

  @Override
  public ContainerCreation createContainer(final ContainerConfig config,
                                           final String name)
      throws DockerException, InterruptedException {

    final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
    if (name != null) {
      checkArgument(CONTAINER_NAME_PATTERN.matcher(name).matches(),
                    "Invalid container name: \"%s\"", name);
      params.add("name", name);
    }

    try {
      final WebResource resource = resource()
          .path("containers").path("create")
          .queryParams(params);
      return request(POST, ContainerCreation.class, resource, resource
          .entity(config)
          .type(APPLICATION_JSON_TYPE)
          .accept(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ImageNotFoundException(config.image(), e);
        default:
          throw e;
      }
    }
  }

  @Override
  public void startContainer(final String containerId)
      throws DockerException, InterruptedException {
    startContainer(containerId, HostConfig.builder().build());
  }

  @Override
  public void startContainer(final String containerId, final HostConfig hostConfig)
      throws DockerException, InterruptedException {
    checkNotNull(containerId, "containerId");
    checkNotNull(hostConfig, "hostConfig");
    try {
      final WebResource resource = resource()
          .path("containers").path(containerId).path("start");
      request(POST, resource, resource
          .type(APPLICATION_JSON_TYPE)
          .accept(APPLICATION_JSON_TYPE)
          .entity(hostConfig));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public void restartContainer(String containerId) throws DockerException, InterruptedException {
    restartContainer(containerId, 10);
  }

  @Override
  public void restartContainer(String containerId, int secondsToWaitBeforeRestart)
      throws DockerException, InterruptedException {
    checkNotNull(containerId, "containerId");
    checkNotNull(secondsToWaitBeforeRestart, "secondsToWait");
    try {
      final WebResource resource = resource().path("containers").path(containerId)
          .path("restart");
      request(POST, resource, resource
          .queryParam("t", String.valueOf(secondsToWaitBeforeRestart)));
    } catch (UniformInterfaceException e) {
      switch (e.getResponse().getStatus()) {
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        default:
          throw new DockerException(e);
      }
    }
  }


  @Override
  public void killContainer(final String containerId) throws DockerException, InterruptedException {
    try {
      final WebResource resource = resource().path("containers").path(containerId).path("kill");
      request(POST, resource, resource);
    } catch (UniformInterfaceException e) {
      switch (e.getResponse().getStatus()) {
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        default:
          throw new DockerException(e);
      }
    }
  }

  @Override
  public void stopContainer(final String containerId, final int secondsToWaitBeforeKilling)
      throws DockerException, InterruptedException {
    try {
      final WebResource resource = resource().path("containers").path(containerId).path("stop");
      request(POST, resource, resource
          .queryParam("t", String.valueOf(secondsToWaitBeforeKilling)));
    } catch (UniformInterfaceException e) {
      switch (e.getResponse().getStatus()) {
        case 304: // already stopped, so we're cool
          return;
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        default:
          throw new DockerException(e);
      }
    }
  }

  @Override
  public ContainerExit waitContainer(final String containerId)
      throws DockerException, InterruptedException {
    try {
      final WebResource resource = resource()
          .path("containers").path(containerId).path("wait");
      // Wait forever
      resource.setProperty(PROPERTY_READ_TIMEOUT, 0);
      return request(POST, ContainerExit.class, resource, resource.accept(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public void removeContainer(final String containerId)
      throws DockerException, InterruptedException {
    removeContainer(containerId, false);
  }

  @Override
  public void removeContainer(final String containerId, final boolean removeVolumes)
      throws DockerException, InterruptedException {
    try {
      final WebResource resource = resource()
          .path("containers").path(containerId);
      request(DELETE, resource, resource
          .queryParam("v", String.valueOf(removeVolumes))
          .accept(APPLICATION_JSON_TYPE));
    } catch (UniformInterfaceException e) {
      switch (e.getResponse().getStatus()) {
        case 404:
          throw new ContainerNotFoundException(containerId);
        default:
          throw new DockerException(e);
      }
    }
  }

  @Override
  public InputStream exportContainer(String containerId)
      throws DockerException, InterruptedException {
    final WebResource resource = resource()
        .path("containers").path(containerId).path("export");
    return request(GET, InputStream.class, resource,
                   resource.accept(APPLICATION_OCTET_STREAM_TYPE));
  }

  @Override
  public InputStream copyContainer(String containerId, String path)
      throws DockerException, InterruptedException {
    final WebResource resource = resource()
        .path("containers").path(containerId).path("copy");

    // Internal JSON object; not worth it to create class for this
    JsonNodeFactory nf = JsonNodeFactory.instance;
    final JsonNode params = nf.objectNode().set("Resource", nf.textNode(path));

    return request(POST, InputStream.class, resource,
                   resource.accept(APPLICATION_OCTET_STREAM_TYPE)
                       .entity(params, APPLICATION_JSON_TYPE));
  }

  @Override
  public ContainerInfo inspectContainer(final String containerId)
      throws DockerException, InterruptedException {
    try {
      final WebResource resource = resource().path("containers").path(containerId).path("json");
      return request(GET, ContainerInfo.class, resource, resource.accept(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public void pull(final String image) throws DockerException, InterruptedException {
    pull(image, new LoggingPullHandler(image));
  }
  
  @Override
  public void pull(String image, AuthConfig authConfig)
      throws DockerException, InterruptedException {
	  pull(image, authConfig, new LoggingPullHandler(image));
  }

  @Override
  public void pull(String image, AuthConfig authConfig, ProgressHandler handler)
	      throws DockerException, InterruptedException {
	  final ImageRef imageRef = new ImageRef(image);

	  final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
	  params.add("fromImage", imageRef.getImage());
	  if (imageRef.getTag() != null) {
		params.add("tag", imageRef.getTag());
	  }
	
	  final WebResource resource = resource().path("images").path("create").queryParams(params);
	  
	  try (ProgressStream pull = request(POST,ProgressStream.class, resource, 
			  resource.accept(APPLICATION_JSON_TYPE).header("X-Registry-Auth", authConfig.toHeaderValue()))) {
		pull.tail(handler, POST, resource.getURI());
	  }
  }
  
  @Override
  public void pull(final String image, final ProgressHandler handler)
      throws DockerException, InterruptedException {
    final ImageRef imageRef = new ImageRef(image);

    final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
    params.add("fromImage", imageRef.getImage());
    if (imageRef.getTag() != null) {
      params.add("tag", imageRef.getTag());
    }

    final WebResource resource = resource().path("images").path("create").queryParams(params);

    try (ProgressStream pull = request(POST, ProgressStream.class, resource,
                                       resource.accept(APPLICATION_JSON_TYPE))) {
      pull.tail(handler, POST, resource.getURI());
    }
  }

  @Override
  public void push(final String image) throws DockerException, InterruptedException {
    push(image, new LoggingPushHandler(image));
  }

  @Override
  public void push(final String image, final ProgressHandler handler)
      throws DockerException, InterruptedException {
    final ImageRef imageRef = new ImageRef(image);

    final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
    if (imageRef.getTag() != null) {
      params.add("tag", imageRef.getTag());
    }

    final WebResource resource =
        resource().path("images").path(imageRef.getImage()).path("push").queryParams(params);

    // the docker daemon requires that the X-Registry-Auth header is specified
    // with a non-empty string even if your registry doesn't use authentication
    try (ProgressStream push =
             request(POST, ProgressStream.class, resource,
                     resource.accept(APPLICATION_JSON_TYPE).header("X-Registry-Auth", "null"))) {
      push.tail(handler, POST, resource.getURI());
    }
  }

  @Override
  public void tag(final String image, final String name)
      throws DockerException, InterruptedException {
    final ImageRef imageRef = new ImageRef(name);

    final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
    params.add("repo", imageRef.getImage());
    if (imageRef.getTag() != null) {
      params.add("tag", imageRef.getTag());
    }

    final WebResource resource =
        resource().path("images").path(image).path("tag").queryParams(params);

    try {
      request(POST, resource, resource);
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ImageNotFoundException(image, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public String build(final Path directory, final BuildParameter... params)
      throws DockerException, InterruptedException, IOException {
    return build(directory, null, new LoggingBuildHandler(), params);
  }

  @Override
  public String build(final Path directory, final String name, final BuildParameter... params)
      throws DockerException, InterruptedException, IOException {
    return build(directory, name, new LoggingBuildHandler(), params);
  }

  @Override
  public String build(final Path directory, final ProgressHandler handler,
                      final BuildParameter... params)
      throws DockerException, InterruptedException, IOException {
    return build(directory, null, handler, params);
  }

  @Override
  public String build(final Path directory, final String name, final ProgressHandler handler,
                      final BuildParameter... params)
      throws DockerException, InterruptedException, IOException {
    checkNotNull(handler, "handler");

    final Multimap<String, String> paramMap = ArrayListMultimap.create();
    for (final BuildParameter param : params) {
      paramMap.put(param.queryParam, String.valueOf(param.value));
    }
    if (name != null) {
      paramMap.put("t", name);
    }

    final WebResource resource = resource().path("build").queryParams(multivaluedMap(paramMap));
    final File compressedDirectory = CompressedDirectory.create(directory);

    try (ProgressStream build = request(POST, ProgressStream.class, resource,
                                        resource.accept(APPLICATION_JSON_TYPE)
                                            .entity(compressedDirectory, "application/tar"))) {
      String imageId = null;
      while (build.hasNextMessage(POST, resource.getURI())) {
        final ProgressMessage message = build.nextMessage(POST, resource.getURI());
        final String id = message.buildImageId();
        if (id != null) {
          imageId = id;
        }
        handler.progress(message);
      }
      return imageId;
    } finally {
      delete(compressedDirectory);
    }
  }

  @Override
  public ImageInfo inspectImage(final String image) throws DockerException, InterruptedException {
    try {
      final WebResource resource = resource().path("images").path(image).path("json");
      return request(GET, ImageInfo.class, resource, resource.accept(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ImageNotFoundException(image, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public List<RemovedImage> removeImage(String image)
      throws DockerException, InterruptedException {
    return removeImage(image, false, false);
  }

  @Override
  public List<RemovedImage> removeImage(String image, boolean force, boolean noPrune)
      throws DockerException, InterruptedException {
    try {
      final WebResource resource = resource().path("images").path(image)
          .queryParam("force", String.valueOf(force))
          .queryParam("noprune", String.valueOf(noPrune));
      return request(DELETE, REMOVED_IMAGE_LIST, resource, resource.accept(APPLICATION_JSON_TYPE));
    } catch (UniformInterfaceException e) {
      switch (e.getResponse().getStatus()) {
        case 404:
          throw new ImageNotFoundException(image);
        default:
          throw new DockerException(e);
      }
    }
  }

  @Override
  public LogStream logs(final String containerId, final LogsParameter... params)
      throws DockerException, InterruptedException {
    final Multimap<String, String> paramMap = ArrayListMultimap.create();
    for (final LogsParameter param : params) {
      paramMap.put(param.name().toLowerCase(Locale.ROOT), String.valueOf(true));
    }
    final WebResource resource = resource()
        .path("containers").path(containerId).path("logs")
        .queryParams(multivaluedMap(paramMap));
    try {
      return request(GET, LogStream.class, resource,
                     resource.accept("application/vnd.docker.raw-stream"));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ContainerNotFoundException(containerId);
        default:
          throw e;
      }
    }
  }

  private WebResource resource() {
    return client.resource(uri).path(VERSION);
  }

  private <T> T request(final String method, final GenericType<T> type,
                        final WebResource resource, final WebResource.Builder request)
      throws DockerException, InterruptedException {
    try {
      return request.method(method, type);
    } catch (ClientHandlerException e) {
      throw propagate(method, resource, e);
    } catch (UniformInterfaceException e) {
      throw propagate(method, resource, e);
    }
  }

  private <T> T request(final String method, final Class<T> clazz,
                        final WebResource resource, final UniformInterface request)
      throws DockerException, InterruptedException {
    try {
      return request.method(method, clazz);
    } catch (ClientHandlerException e) {
      throw propagate(method, resource, e);
    } catch (UniformInterfaceException e) {
      throw propagate(method, resource, e);
    }
  }

  private void request(final String method,
                       final WebResource resource,
                       final UniformInterface request) throws DockerException,
                                                              InterruptedException {
    try {
      request.method(method);
    } catch (ClientHandlerException e) {
      throw propagate(method, resource, e);
    } catch (UniformInterfaceException e) {
      throw propagate(method, resource, e);
    }
  }

  private DockerRequestException propagate(final String method, final WebResource resource,
                                           final UniformInterfaceException e) {
    return new DockerRequestException(method, resource.getURI(),
                                      e.getResponse().getStatus(), message(e.getResponse()),
                                      e);
  }

  private RuntimeException propagate(final String method, final WebResource resource,
                                     final ClientHandlerException e)
      throws DockerException, InterruptedException {
    final Throwable cause = e.getCause();
    if ((cause instanceof SocketTimeoutException) || (cause instanceof ConnectTimeoutException)) {
      throw new DockerTimeoutException(method, resource.getURI(), e);
    } else if (cause instanceof InterruptedIOException) {
      throw new InterruptedException("Interrupted: " + method + " " + resource);
    } else {
      throw new DockerException(e);
    }
  }

  private String message(final ClientResponse response) {
    final Readable reader = new InputStreamReader(response.getEntityInputStream(), UTF_8);
    try {
      return CharStreams.toString(reader);
    } catch (IOException ignore) {
      return null;
    }
  }

  private MultivaluedMap<String, String> multivaluedMap(final Multimap<String, String> map) {
    final MultivaluedMap<String, String> multivaluedMap = new MultivaluedMapImpl();
    for (Map.Entry<String, String> e : map.entries()) {
      final String value = e.getValue();
      if (value != null) {
        multivaluedMap.add(e.getKey(), value);
      }
    }
    return multivaluedMap;
  }

  /**
   * Create a new {@link DefaultDockerClient} builder.
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private URI uri;
    private long connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;
    private long readTimeoutMillis = DEFAULT_READ_TIMEOUT_MILLIS;

    public URI uri() {
      return uri;
    }

    public Builder uri(final URI uri) {
      this.uri = uri;
      return this;
    }

    public Builder uri(final String uri) {
      return uri(URI.create(uri));
    }

    public long connectTimeoutMillis() {
      return connectTimeoutMillis;
    }

    public Builder connectTimeoutMillis(final long connectTimeoutMillis) {
      this.connectTimeoutMillis = connectTimeoutMillis;
      return this;
    }

    public long readTimeoutMillis() {
      return readTimeoutMillis;
    }

    public Builder readTimeoutMillis(final long readTimeoutMillis) {
      this.readTimeoutMillis = readTimeoutMillis;
      return this;
    }

    public DefaultDockerClient build() {
      return new DefaultDockerClient(this);
    }
  }
}
