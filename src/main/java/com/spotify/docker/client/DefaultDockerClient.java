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
import com.spotify.docker.client.messages.TopResults;
import com.spotify.docker.client.messages.Version;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.spotify.docker.client.CompressedDirectory.delete;
import static com.spotify.docker.client.ObjectMapperProvider.objectMapper;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;

public class DefaultDockerClient implements DockerClient, Closeable {

  private static final String VERSION = "v1.12";
  private static final Logger log = LoggerFactory.getLogger(DefaultDockerClient.class);

  public static final long NO_TIMEOUT = 0;

  private static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = SECONDS.toMillis(5);
  private static final long DEFAULT_READ_TIMEOUT_MILLIS = SECONDS.toMillis(30);
  private static final ClientConfig DEFAULT_CONFIG = new ClientConfig(
      ObjectMapperProvider.class,
      JacksonFeature.class,
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

  private final ExecutorService executor = MoreExecutors.getExitingExecutorService(
      (ThreadPoolExecutor) Executors.newCachedThreadPool(
          new ThreadFactoryBuilder()
              .setDaemon(true)
              .setNameFormat("docker-client-" + CLIENT_COUNTER.incrementAndGet() + "-%d")
              .build()));


  private final Client client;

  private final URI uri;

  /**
   * Create a new client with default configuration.
   * @param uri The docker rest api uri.
   */
  public DefaultDockerClient(final String uri) {
    this(URI.create(uri.replaceAll("^unix:///", "unix://localhost/")));
  }

  /**
   * Create a new client with default configuration.
   * @param uri The docker rest api uri.
   */
  public DefaultDockerClient(final URI uri) {
    this(new Builder().uri(uri));
  }

  /**
   * Create a new client with default configuration.
   * @param uri The docker rest api uri.
   * @param dockerCertificates The certificates to use for HTTPS.
   */
  public DefaultDockerClient(final URI uri, final DockerCertificates dockerCertificates) {
    this(new Builder().uri(uri).dockerCertificates(dockerCertificates));
  }

  /**
   * Create a new client using the configuration of the builder.
   */
  private DefaultDockerClient(final Builder builder) {
    URI originalUri = checkNotNull(builder.uri, "uri");

    if ((builder.dockerCertificates != null) && !originalUri.getScheme().equals("https")) {
      throw new IllegalArgumentException("https URI must be provided to use certificates");
    }

    if (originalUri.getScheme().equals("unix")) {
      this.uri = UnixConnectionSocketFactory.sanitizeUri(originalUri);
    } else {
      this.uri = originalUri;
    }

    final ClientConfig config = DEFAULT_CONFIG
        .connectorProvider(new ApacheConnectorProvider())
        .property(ClientProperties.CONNECT_TIMEOUT, (int) builder.connectTimeoutMillis)
        .property(ClientProperties.READ_TIMEOUT, (int) builder.readTimeoutMillis)
        .property(ApacheClientProperties.CONNECTION_MANAGER,
                  new PoolingHttpClientConnectionManager(getSchemeRegistry(builder)));

    this.client = ClientBuilder.newClient(config);
  }

  private Registry<ConnectionSocketFactory> getSchemeRegistry(final Builder builder) {
    final SSLConnectionSocketFactory https;
    if (builder.dockerCertificates == null) {
      https = SSLConnectionSocketFactory.getSocketFactory();
    } else {
      https = new SSLConnectionSocketFactory(builder.dockerCertificates.sslContext(),
                                             builder.dockerCertificates.hostnameVerifier());
    }

    return RegistryBuilder
        .<ConnectionSocketFactory>create()
        .register("https", https)
        .register("http", PlainConnectionSocketFactory.getSocketFactory())
        .register("unix", new UnixConnectionSocketFactory(builder.uri))
        .build();
  }

  @Override
  public void close() {
    executor.shutdownNow();
    client.close();
  }

  @Override
  public String ping() throws DockerException, InterruptedException {
    final WebTarget resource = client.target(uri).path("_ping");
    return request(GET, String.class, resource, resource.request());
  }

  @Override
  public Version version() throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("version");
    return request(GET, Version.class, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public Info info() throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("info");
    return request(GET, Info.class, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public List<Container> listContainers(final ListContainersParam... params)
      throws DockerException, InterruptedException {
    WebTarget resource = resource()
        .path("containers").path("json");

    for (ListContainersParam param : params) {
      resource = resource.queryParam(param.name(), param.value());
    }

    return request(GET, CONTAINER_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public List<Image> listImages(ListImagesParam... params)
      throws DockerException, InterruptedException {
    WebTarget resource = resource()
        .path("images").path("json");

    final Map<String, String> filters = newHashMap();
    for (ListImagesParam param : params) {
      if (param instanceof ListImagesFilterParam) {
        filters.put(param.name(), param.value());
      } else {
        resource = resource.queryParam(param.name(), param.value());
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
        resource = resource.queryParam("filters", encoded);
      }
    } catch (IOException e) {
      throw new DockerException(e);
    }

    return request(GET, IMAGE_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public ContainerCreation createContainer(final ContainerConfig config)
      throws DockerException, InterruptedException {
    return createContainer(config, null);
  }

  @Override
  public ContainerCreation createContainer(final ContainerConfig config,
                                           final String name)
      throws DockerException, InterruptedException {
    WebTarget resource = resource()
        .path("containers").path("create");

    if (name != null) {
      checkArgument(CONTAINER_NAME_PATTERN.matcher(name).matches(),
                    "Invalid container name: \"%s\"", name);
      resource = resource.queryParam("name", name);
    }

    log.info("Creating container with ContainerConfig: {}", config);

    try {
      return request(POST, ContainerCreation.class, resource, resource
          .request(APPLICATION_JSON_TYPE), Entity.json(config));
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

    log.info("Starting container with HostConfig: {}", hostConfig);

    try {
      final WebTarget resource = resource()
          .path("containers").path(containerId).path("start");
      request(POST, resource, resource
                  .request(APPLICATION_JSON_TYPE)
                  .property(ClientProperties.REQUEST_ENTITY_PROCESSING,
                            RequestEntityProcessing.BUFFERED),
              Entity.json(hostConfig));
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
      final WebTarget resource = resource().path("containers").path(containerId)
          .path("restart")
          .queryParam("t", String.valueOf(secondsToWaitBeforeRestart));
      request(POST, resource, resource.request());
    } catch (WebApplicationException e) {
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
      final WebTarget resource = resource().path("containers").path(containerId).path("kill");
      request(POST, resource, resource.request());
    } catch (WebApplicationException e) {
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
      final WebTarget resource = resource().path("containers").path(containerId).path("stop")
          .queryParam("t", String.valueOf(secondsToWaitBeforeKilling));
      request(POST, resource, resource.request());
    } catch (WebApplicationException e) {
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
      final WebTarget resource = resource()
          .path("containers").path(containerId).path("wait");
      // Wait forever
      return request(POST, ContainerExit.class, resource,
                     resource.request(APPLICATION_JSON_TYPE)
                             .property(ClientProperties.READ_TIMEOUT, 0));
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
      final WebTarget resource = resource()
          .path("containers").path(containerId);
      request(DELETE, resource, resource
          .queryParam("v", String.valueOf(removeVolumes))
          .request(APPLICATION_JSON_TYPE));
    } catch (WebApplicationException e) {
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
    final WebTarget resource = resource()
        .path("containers").path(containerId).path("export");
    return request(GET, InputStream.class, resource,
                   resource.request(APPLICATION_OCTET_STREAM_TYPE));
  }

  @Override
  public InputStream copyContainer(String containerId, String path)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource()
        .path("containers").path(containerId).path("copy");

    // Internal JSON object; not worth it to create class for this
    JsonNodeFactory nf = JsonNodeFactory.instance;
    final JsonNode params = nf.objectNode().set("Resource", nf.textNode(path));

    return request(POST, InputStream.class, resource,
                   resource.request(APPLICATION_OCTET_STREAM_TYPE),
                   Entity.json(params));
  }

  @Override
  public ContainerInfo inspectContainer(final String containerId)
      throws DockerException, InterruptedException {
    try {
      final WebTarget resource = resource().path("containers").path(containerId).path("json");
      return request(GET, ContainerInfo.class, resource, resource.request(APPLICATION_JSON_TYPE));
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
  public TopResults topContainer(final String containerId)
      throws DockerException, InterruptedException {
    return topContainer(containerId, null);
  }

  @Override
  public TopResults topContainer(final String containerId, final String psArgs)
      throws DockerException, InterruptedException {
    try {
      WebTarget resource = resource().path("containers").path(containerId).path("top");
      if (!Strings.isNullOrEmpty(psArgs)) {
        resource = resource.queryParam("ps_args", psArgs);
      }
      return request(GET, TopResults.class, resource, resource.request(APPLICATION_JSON_TYPE));
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
  public void pull(final String image, final ProgressHandler handler)
      throws DockerException, InterruptedException {
    final ImageRef imageRef = new ImageRef(image);

    WebTarget resource = resource().path("images").path("create");

    resource = resource.queryParam("fromImage", imageRef.getImage());
    if (imageRef.getTag() != null) {
      resource = resource.queryParam("tag", imageRef.getTag());
    }

    try (ProgressStream pull = request(POST, ProgressStream.class, resource,
                                       resource.request(APPLICATION_JSON_TYPE))) {
      pull.tail(handler, POST, resource.getUri());
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

    WebTarget resource =
        resource().path("images").path(imageRef.getImage()).path("push");

    if (imageRef.getTag() != null) {
      resource = resource.queryParam("tag", imageRef.getTag());
    }

    // the docker daemon requires that the X-Registry-Auth header is specified
    // with a non-empty string even if your registry doesn't use authentication
    try (ProgressStream push =
             request(POST, ProgressStream.class, resource,
                     resource.request(APPLICATION_JSON_TYPE).header("X-Registry-Auth", "null"))) {
      push.tail(handler, POST, resource.getUri());
    }
  }

  @Override
  public void tag(final String image, final String name)
      throws DockerException, InterruptedException {
    final ImageRef imageRef = new ImageRef(name);

    WebTarget resource =
        resource().path("images").path(image).path("tag");

    resource = resource.queryParam("repo", imageRef.getImage());
    if (imageRef.getTag() != null) {
      resource = resource.queryParam("tag", imageRef.getTag());
    }

    try {
      request(POST, resource, resource.request());
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

    WebTarget resource = resource().path("build");

    for (final BuildParameter param : params) {
      resource = resource.queryParam(param.queryParam, String.valueOf(param.value));
    }
    if (name != null) {
     resource = resource.queryParam("t", name);
    }

    final File compressedDirectory = CompressedDirectory.create(directory);

    try (ProgressStream build = request(POST, ProgressStream.class, resource,
                                        resource.request(APPLICATION_JSON_TYPE),
                                        Entity.entity(compressedDirectory, "application/tar"))) {
      String imageId = null;
      while (build.hasNextMessage(POST, resource.getUri())) {
        final ProgressMessage message = build.nextMessage(POST, resource.getUri());
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
      final WebTarget resource = resource().path("images").path(image).path("json");
      return request(GET, ImageInfo.class, resource, resource.request(APPLICATION_JSON_TYPE));
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
      final WebTarget resource = resource().path("images").path(image)
          .queryParam("force", String.valueOf(force))
          .queryParam("noprune", String.valueOf(noPrune));
      return request(DELETE, REMOVED_IMAGE_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (WebApplicationException e) {
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
    WebTarget resource = resource()
        .path("containers").path(containerId).path("logs");

    for (final LogsParameter param : params) {
      resource = resource.queryParam(param.name().toLowerCase(Locale.ROOT), String.valueOf(true));
    }

    try {
      return request(GET, LogStream.class, resource,
                     resource.request("application/vnd.docker.raw-stream"));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ContainerNotFoundException(containerId);
        default:
          throw e;
      }
    }
  }

  private WebTarget resource() {
    return client.target(uri).path(VERSION);
  }

  private <T> T request(final String method, final GenericType<T> type,
                        final WebTarget resource, final Invocation.Builder request)
      throws DockerException, InterruptedException {
    try {
      return request.async().method(method, type).get();
    } catch (ExecutionException e) {
      throw propagate(method, resource, e);
    }
  }

  private <T> T request(final String method, final Class<T> clazz,
                        final WebTarget resource, final Invocation.Builder request)
      throws DockerException, InterruptedException {
    try {
      return request.async().method(method, clazz).get();
    } catch (ExecutionException e) {
      throw propagate(method, resource, e);
    }
  }

  private <T> T request(final String method, final Class<T> clazz,
                        final WebTarget resource, final Invocation.Builder request,
                        final Entity<?> entity)
      throws DockerException, InterruptedException {
    try {
      return request.async().method(method, entity, clazz).get();
    } catch (ExecutionException e) {
      throw propagate(method, resource, e);
    }
  }

  private void request(final String method,
                       final WebTarget resource,
                       final Invocation.Builder request)
      throws DockerException, InterruptedException {
    try {
      request.async().method(method).get();
    } catch (ExecutionException e) {
      throw propagate(method, resource, e);
    }
  }

  private void request(final String method,
                       final WebTarget resource,
                       final Invocation.Builder request,
                       final Entity<?> entity)
      throws DockerException, InterruptedException {
    try {
      request.async().method(method, entity).get();
    } catch (ExecutionException e) {
      throw propagate(method, resource, e);
    }
  }

  private RuntimeException propagate(final String method, final WebTarget resource,
                                     final Exception e)
      throws DockerException, InterruptedException {
    Throwable cause = e.getCause();

    Response response = null;
    if (cause instanceof ResponseProcessingException) {
      response = ((ResponseProcessingException) cause).getResponse();
    } else if (cause instanceof WebApplicationException) {
      response = ((WebApplicationException) cause).getResponse();
    } else if ((cause instanceof ProcessingException) && (cause.getCause() != null)) {
      // For a ProcessingException, The exception message or nested Throwable cause SHOULD contain
      // additional information about the reason of the processing failure.
      cause = cause.getCause();
    }

    if (response != null) {
      throw new DockerRequestException(method, resource.getUri(), response.getStatus(),
                                       message(response), cause);
    } else if ((cause instanceof SocketTimeoutException) ||
               (cause instanceof ConnectTimeoutException)) {
      throw new DockerTimeoutException(method, resource.getUri(), e);
    } else if (cause instanceof InterruptedIOException) {
      throw new InterruptedException("Interrupted: " + method + " " + resource);
    } else {
      throw new DockerException(e);
    }
  }

  private String message(final Response response) {
    final Readable reader = new InputStreamReader(response.readEntity(InputStream.class), UTF_8);
    try {
      return CharStreams.toString(reader);
    } catch (IOException ignore) {
      return null;
    }
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
    private DockerCertificates dockerCertificates;

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

    public DockerCertificates dockerCertificates() {
      return dockerCertificates;
    }

    public Builder dockerCertificates(final DockerCertificates dockerCertificates) {
      this.dockerCertificates = dockerCertificates;
      return this;
    }

    public DefaultDockerClient build() {
      return new DefaultDockerClient(this);
    }
  }
}
