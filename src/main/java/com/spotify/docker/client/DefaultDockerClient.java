/*
 * Copyright (c) 2014 Spotify AB.
 * Copyright (c) 2014 Oleg Poleshuk.
 * Copyright (c) 2014 CyDesign Ltd.
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.net.HostAndPort;

import com.spotify.docker.client.exceptions.BadParamException;
import com.spotify.docker.client.exceptions.ConflictException;
import com.spotify.docker.client.exceptions.ContainerNotFoundException;
import com.spotify.docker.client.exceptions.ContainerRenameConflictException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.exceptions.ExecCreateConflictException;
import com.spotify.docker.client.exceptions.ExecNotFoundException;
import com.spotify.docker.client.exceptions.ExecStartConflictException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.exceptions.NetworkNotFoundException;
import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.exceptions.PermissionException;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import com.spotify.docker.client.exceptions.TaskNotFoundException;
import com.spotify.docker.client.exceptions.UnsupportedApiVersionException;
import com.spotify.docker.client.exceptions.VolumeNotFoundException;
import com.spotify.docker.client.messages.AuthConfig;
import com.spotify.docker.client.messages.AuthRegistryConfig;
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
import com.spotify.docker.client.messages.ProgressMessage;
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

import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.internal.util.Base64;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static com.spotify.docker.client.ObjectMapperProvider.objectMapper;
import static com.spotify.docker.client.VersionCompare.compareVersion;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;


public class DefaultDockerClient implements DockerClient, Closeable {

  /**
   * Hack: this {@link ProgressHandler} is meant to capture the image ID (or image digest in Docker
   * 1.10+) of an image being loaded.
   * Weirdly enough, Docker returns the ID or digest of a newly created image in the status of a
   * progress message. <p> The image ID/digest is required to tag the just loaded image since,
   * also weirdly enough, the pull operation with the <code>fromSrc</code> parameter does not
   * support the <code>tag</code> parameter. By retrieving the ID/digest, the image can be tagged
   * with its image name, given its ID/digest.
   */
  private static class LoadProgressHandler implements ProgressHandler {

    // The length of the image hash
    private static final int EXPECTED_CHARACTER_NUM1 = 64;
    // The length of the image digest
    private static final int EXPECTED_CHARACTER_NUM2 = 71;

    private final ProgressHandler delegate;

    private String imageId;

    private LoadProgressHandler(ProgressHandler delegate) {
      this.delegate = delegate;
    }

    private String getImageId() {
      Preconditions.checkState(imageId != null,
                               "Could not acquire image ID or digest following load");
      return imageId;
    }

    @Override
    public void progress(ProgressMessage message) throws DockerException {
      delegate.progress(message);
      final String status = message.status();
      if (status != null &&
          (status.length() == EXPECTED_CHARACTER_NUM1 ||
           status.length() == EXPECTED_CHARACTER_NUM2)) {
        imageId = message.status();
      }
    }

  }

  // ==========================================================================

  private static final String UNIX_SCHEME = "unix";

  private static final Logger log = LoggerFactory.getLogger(DefaultDockerClient.class);

  static final long NO_TIMEOUT = 0;

  private static final long DEFAULT_CONNECT_TIMEOUT_MILLIS = SECONDS.toMillis(5);
  private static final long DEFAULT_READ_TIMEOUT_MILLIS = SECONDS.toMillis(30);
  private static final int DEFAULT_CONNECTION_POOL_SIZE = 100;

  private static final ClientConfig DEFAULT_CONFIG = new ClientConfig(
      ObjectMapperProvider.class,
      JacksonFeature.class,
      LogsResponseReader.class,
      ProgressResponseReader.class);

  private static final Pattern CONTAINER_NAME_PATTERN = Pattern.compile("/?[a-zA-Z0-9_-]+");

  private static final GenericType<List<Container>> CONTAINER_LIST =
      new GenericType<List<Container>>() {
      };

  private static final GenericType<List<ContainerChange>> CONTAINER_CHANGE_LIST =
      new GenericType<List<ContainerChange>>() {
      };

  private static final GenericType<List<Image>> IMAGE_LIST =
      new GenericType<List<Image>>() {
      };

  private static final GenericType<List<Network>> NETWORK_LIST =
      new GenericType<List<Network>>() {
      };

  private static final GenericType<List<ImageSearchResult>> IMAGES_SEARCH_RESULT_LIST =
      new GenericType<List<ImageSearchResult>>() {
      };

  private static final GenericType<List<RemovedImage>> REMOVED_IMAGE_LIST =
      new GenericType<List<RemovedImage>>() {
      };

  private static final GenericType<List<ImageHistory>> IMAGE_HISTORY_LIST =
      new GenericType<List<ImageHistory>>() {
      };

  private static final Supplier<ClientBuilder> DEFAULT_BUILDER_SUPPLIER =
      new Supplier<ClientBuilder>() {
        @Override
        public ClientBuilder get() {
          return ClientBuilder.newBuilder();
        }
      };

  private static final GenericType<List<Service>> SERVICE_LIST =
      new GenericType<List<Service>>() {
      };

  private static final GenericType<List<Task>> TASK_LIST = new GenericType<List<Task>>() {
  };

  private final Client client;
  private final Client noTimeoutClient;

  private final URI uri;
  private final String apiVersion;
  private final AuthConfig authConfig;

  private final Map<String, Object> headers;

  Client getClient() {
    return client;
  }

  Client getNoTimeoutClient() {
    return noTimeoutClient;
  }

  /**
   * Create a new client with default configuration.
   *
   * @param uri The docker rest api uri.
   */
  public DefaultDockerClient(final String uri) {
    this(URI.create(uri.replaceAll("^unix:///", "unix://localhost/")));
  }

  /**
   * Create a new client with default configuration.
   *
   * @param uri The docker rest api uri.
   */
  public DefaultDockerClient(final URI uri) {
    this(new Builder().uri(uri));
  }

  /**
   * Create a new client with default configuration.
   *
   * @param uri                The docker rest api uri.
   * @param dockerCertificates The certificates to use for HTTPS.
   */
  public DefaultDockerClient(final URI uri, final DockerCertificates dockerCertificates) {
    this(new Builder().uri(uri).dockerCertificates(dockerCertificates));
  }

  /**
   * Create a new client using the configuration of the builder.
   *
   * @param builder DefaultDockerClient builder
   */
  protected DefaultDockerClient(final Builder builder) {
    this(builder, DEFAULT_BUILDER_SUPPLIER);
  }

  @VisibleForTesting
  DefaultDockerClient(final Builder builder, Supplier<ClientBuilder> clientBuilderSupplier) {
    final URI originalUri = checkNotNull(builder.uri, "uri");
    this.apiVersion = builder.apiVersion();

    if ((builder.dockerCertificates != null) && !originalUri.getScheme().equals("https")) {
      throw new IllegalArgumentException(
          "An HTTPS URI for DOCKER_HOST must be provided to use Docker client certificates");
    }

    if (originalUri.getScheme().equals(UNIX_SCHEME)) {
      this.uri = UnixConnectionSocketFactory.sanitizeUri(originalUri);
    } else {
      this.uri = originalUri;
    }

    final PoolingHttpClientConnectionManager cm = getConnectionManager(builder);
    final PoolingHttpClientConnectionManager noTimeoutCm = getConnectionManager(builder);

    final RequestConfig requestConfig = RequestConfig.custom()
        .setConnectionRequestTimeout((int) builder.connectTimeoutMillis)
        .setConnectTimeout((int) builder.connectTimeoutMillis)
        .setSocketTimeout((int) builder.readTimeoutMillis)
        .build();

    final ClientConfig config = DEFAULT_CONFIG
        .connectorProvider(new ApacheConnectorProvider())
        .property(ApacheClientProperties.CONNECTION_MANAGER, cm)
        .property(ApacheClientProperties.REQUEST_CONFIG, requestConfig);

    this.authConfig = builder.authConfig;

    this.client = clientBuilderSupplier.get().withConfig(config).build();

    // ApacheConnector doesn't respect per-request timeout settings.
    // Workaround: instead create a client with infinite read timeout,
    // and use it for waitContainer, stopContainer, attachContainer, logs, and build
    final RequestConfig noReadTimeoutRequestConfig = RequestConfig.copy(requestConfig)
        .setSocketTimeout((int) NO_TIMEOUT)
        .build();
    this.noTimeoutClient = clientBuilderSupplier.get()
        .withConfig(config)
        .property(ApacheClientProperties.CONNECTION_MANAGER, noTimeoutCm)
        .property(ApacheClientProperties.REQUEST_CONFIG, noReadTimeoutRequestConfig)
        .build();

    this.headers = new HashMap<>(builder.headers());
  }

  public String getHost() {
    return fromNullable(uri.getHost()).or("localhost");
  }

  private PoolingHttpClientConnectionManager getConnectionManager(Builder builder) {
    final PoolingHttpClientConnectionManager cm =
        new PoolingHttpClientConnectionManager(getSchemeRegistry(builder));

    // Use all available connections instead of artificially limiting ourselves to 2 per server.
    cm.setMaxTotal(builder.connectionPoolSize);
    cm.setDefaultMaxPerRoute(cm.getMaxTotal());

    return cm;
  }

  private Registry<ConnectionSocketFactory> getSchemeRegistry(final Builder builder) {
    final SSLConnectionSocketFactory https;
    if (builder.dockerCertificates == null) {
      https = SSLConnectionSocketFactory.getSocketFactory();
    } else {
      https = new SSLConnectionSocketFactory(builder.dockerCertificates.sslContext(),
                                             builder.dockerCertificates.hostnameVerifier());
    }

    final RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder
        .<ConnectionSocketFactory>create()
        .register("https", https)
        .register("http", PlainConnectionSocketFactory.getSocketFactory());

    if (builder.uri.getScheme().equals(UNIX_SCHEME)) {
      registryBuilder.register(UNIX_SCHEME, new UnixConnectionSocketFactory(builder.uri));
    }

    return registryBuilder.build();
  }

  @Override
  public void close() {
    client.close();
    noTimeoutClient.close();
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
  public int auth(final AuthConfig authConfig) throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("auth");
    final Response response =
        request(POST, Response.class, resource, resource.request(APPLICATION_JSON_TYPE),
                Entity.json(authConfig));
    return response.getStatus();
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

    final Map<String, List<String>> filters = newHashMap();
    for (final ListContainersParam param : params) {
      if (param instanceof ListContainersFilterParam) {
        List<String> filterValueList;
        if (filters.containsKey(param.name())) {
          filterValueList = filters.get(param.name());
        } else {
          filterValueList = Lists.newArrayList();
        }
        filterValueList.add(param.value());
        filters.put(param.name(), filterValueList);
      } else {
        resource = resource.queryParam(urlEncode(param.name()), urlEncode(param.value()));
      }
    }

    if (!filters.isEmpty()) {
      // If filters were specified, we must put them in a JSON object and pass them using the
      // 'filters' query param like this: filters={"dangling":["true"]}. If filters is an empty map,
      // urlEncodeFilters will return null and queryParam() will remove that query parameter.
      resource = resource.queryParam("filters", urlEncodeFilters(filters));
    }

    try {
      return request(GET, CONTAINER_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 400:
          throw new BadParamException(getQueryParamMap(resource), e);
        default:
          throw e;
      }
    }
  }

  private Map<String, String> getQueryParamMap(final WebTarget resource) {
    final String queryParams = resource.getUri().getQuery();
    final Map<String, String> paramsMap = Maps.newHashMap();
    for (final String queryParam : queryParams.split("&")) {
      final String[] kv = queryParam.split("=");
      paramsMap.put(kv[0], kv[1]);
    }
    return paramsMap;
  }

  /**
   * URL-encodes a string
   *
   * @param unencoded A string that may contain characters not allowed in URLs
   * @return URL-encoded String
   * @throws DockerException if there's an UnsupportedEncodingException
   */
  private String urlEncode(final String unencoded) throws DockerException {
    try {
      return URLEncoder.encode(unencoded, UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      throw new DockerException(e);
    }
  }

  /**
   * Takes a map of filters and URL-encodes them. If the map is empty or an exception occurs, return
   * null.
   *
   * @param filters A map of filters.
   * @return String
   * @throws DockerException if there's an IOException
   */
  private String urlEncodeFilters(final Map<String, List<String>> filters) throws DockerException {
    try {
      final String unencodedFilters = objectMapper().writeValueAsString(filters);
      if (!unencodedFilters.isEmpty()) {
        return urlEncode(unencodedFilters);
      }
    } catch (IOException e) {
      throw new DockerException(e);
    }
    return null;
  }

  @Override
  public List<Image> listImages(final ListImagesParam... params)
      throws DockerException, InterruptedException {
    WebTarget resource = resource()
        .path("images").path("json");

    final Map<String, List<String>> filters = newHashMap();
    for (final ListImagesParam param : params) {
      if (param instanceof ListImagesFilterParam) {
        final List<String> filterValueList;
        if (filters.containsKey(param.name())) {
          filterValueList = filters.get(param.name());
        } else {
          filterValueList = Lists.newArrayList();
        }
        filterValueList.add(param.value());
        filters.put(param.name(), filterValueList);
      } else {
        resource = resource.queryParam(urlEncode(param.name()), urlEncode(param.value()));
      }
    }

    if (!filters.isEmpty()) {
      // If filters were specified, we must put them in a JSON object and pass them using the
      // 'filters' query param like this: filters={"dangling":["true"]}. If filters is an empty map,
      // urlEncodeFilters will return null and queryParam() will remove that query parameter.
      resource = resource.queryParam("filters", urlEncodeFilters(filters));
    }

    return request(GET, IMAGE_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public ContainerCreation createContainer(final ContainerConfig config)
      throws DockerException, InterruptedException {
    return createContainer(config, null);
  }

  @Override
  public ContainerCreation createContainer(final ContainerConfig config, final String name)
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
        case 406:
          throw new DockerException("Impossible to attach. Container not running.", e);
        default:
          throw e;
      }
    }
  }

  @Override
  public void startContainer(final String containerId)
      throws DockerException, InterruptedException {
    checkNotNull(containerId, "containerId");

    log.info("Starting container with Id: {}", containerId);

    containerAction(containerId, "start");
  }

  private void containerAction(final String containerId, final String action)
      throws DockerException, InterruptedException {
    try {
      final WebTarget resource = resource()
          .path("containers").path(containerId).path(action);
      request(POST, resource, resource.request());
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
  public void pauseContainer(final String containerId)
      throws DockerException, InterruptedException {
    checkNotNull(containerId, "containerId");
    containerAction(containerId, "pause");
  }

  @Override
  public void unpauseContainer(final String containerId)
      throws DockerException, InterruptedException {
    checkNotNull(containerId, "containerId");
    containerAction(containerId, "unpause");
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
  public void killContainer(final String containerId) throws DockerException, InterruptedException {
    containerAction(containerId, "kill");
  }

  @Override
  public void stopContainer(final String containerId, final int secondsToWaitBeforeKilling)
      throws DockerException, InterruptedException {
    try {
      final WebTarget resource = noTimeoutResource()
          .path("containers").path(containerId).path("stop")
          .queryParam("t", String.valueOf(secondsToWaitBeforeKilling));
      request(POST, resource, resource.request());
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 304: // already stopped, so we're cool
          return;
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public ContainerExit waitContainer(final String containerId)
      throws DockerException, InterruptedException {
    try {
      final WebTarget resource = noTimeoutResource()
          .path("containers").path(containerId).path("wait");
      // Wait forever
      return request(POST, ContainerExit.class, resource,
                     resource.request(APPLICATION_JSON_TYPE));
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
    removeContainer(containerId, new RemoveContainerParam[0]);
  }

  @Deprecated
  @Override
  public void removeContainer(final String containerId, final boolean removeVolumes)
      throws DockerException, InterruptedException {
    removeContainer(containerId, RemoveContainerParam.removeVolumes(removeVolumes));
  }

  @Override
  public void removeContainer(final String containerId, final RemoveContainerParam... params)
      throws DockerException, InterruptedException {
    try {
      WebTarget resource = resource().path("containers").path(containerId);

      for (final RemoveContainerParam param : params) {
        resource = resource.queryParam(param.name(), param.value());
      }

      request(DELETE, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 400:
          throw new BadParamException(getQueryParamMap(resource()), e);
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public InputStream exportContainer(String containerId)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource()
        .path("containers").path(containerId).path("export");
    try {
      return request(GET, InputStream.class, resource,
                     resource.request(APPLICATION_OCTET_STREAM_TYPE));
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
  @Deprecated
  public InputStream copyContainer(String containerId, String path)
      throws DockerException, InterruptedException {
    final String apiVersion = version().apiVersion();
    final int versionComparison = compareVersion(apiVersion, "1.24");

    // Version above 1.24
    if (versionComparison >= 0) {
      throw new UnsupportedApiVersionException(apiVersion);
    }

    final WebTarget resource = resource()
        .path("containers").path(containerId).path("copy");

    // Internal JSON object; not worth it to create class for this
    final JsonNodeFactory nf = JsonNodeFactory.instance;
    final JsonNode params = nf.objectNode().set("Resource", nf.textNode(path));

    try {
      return request(POST, InputStream.class, resource,
                     resource.request(APPLICATION_OCTET_STREAM_TYPE),
                     Entity.json(params));
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
  public InputStream archiveContainer(String containerId, String path)
      throws DockerException, InterruptedException {
    final String apiVersion = version().apiVersion();
    final int versionComparison = compareVersion(apiVersion, "1.20");

    // Version below 1.20
    if (versionComparison < 0) {
      throw new UnsupportedApiVersionException(apiVersion);
    }

    final WebTarget resource = resource()
        .path("containers").path(containerId).path("archive")
        .queryParam("path", path);

    try {
      return request(GET, InputStream.class, resource,
                     resource.request(APPLICATION_OCTET_STREAM_TYPE));
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
  public void copyToContainer(final Path directory, String containerId, String path)
      throws DockerException, InterruptedException, IOException {
    final WebTarget resource = resource()
        .path("containers")
        .path(containerId)
        .path("archive")
        .queryParam("noOverwriteDirNonDir", true)
        .queryParam("path", path);

    final CompressedDirectory compressedDirectory = CompressedDirectory.create(directory);

    final InputStream fileStream = Files.newInputStream(compressedDirectory.file());

    try {
      request(PUT, String.class, resource,
              resource.request(APPLICATION_OCTET_STREAM_TYPE),
              Entity.entity(fileStream, "application/tar"));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 400:
          throw new BadParamException(getQueryParamMap(resource), e);
        case 403:
          throw new PermissionException("Volume or container rootfs is marked as read-only.", e);
        case 404:
          throw new NotFoundException(
              String.format("Either container %s or path %s not found.", containerId, path), e);
        default:
          throw e;
      }
    }
  }

  @Override
  public List<ContainerChange> inspectContainerChanges(final String containerId)
      throws DockerException, InterruptedException {
    try {
      final WebTarget resource = resource().path("containers").path(containerId).path("changes");
      return request(GET, CONTAINER_CHANGE_LIST, resource,
                     resource.request(APPLICATION_JSON_TYPE));
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
  public ContainerCreation commitContainer(final String containerId,
                                           final String repo,
                                           final String tag,
                                           final ContainerConfig config,
                                           final String comment,
                                           final String author)
      throws DockerException, InterruptedException {

    checkNotNull(containerId, "containerId");
    checkNotNull(repo, "repo");
    checkNotNull(config, "containerConfig");

    WebTarget resource = resource()
        .path("commit")
        .queryParam("container", containerId)
        .queryParam("repo", repo);

    if (!isNullOrEmpty(author)) {
      resource = resource.queryParam("author", author);
    }
    if (!isNullOrEmpty(comment)) {
      resource = resource.queryParam("comment", comment);
    }
    if (!isNullOrEmpty(tag)) {
      resource = resource.queryParam("tag", tag);
    }

    log.info("Committing container id: {} to repository: {} with ContainerConfig: {}", containerId,
             repo, config);

    try {
      return request(POST, ContainerCreation.class, resource, resource
          .request(APPLICATION_JSON_TYPE), Entity.json(config));
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
  public void renameContainer(final String containerId, final String name)
      throws DockerException, InterruptedException {
    WebTarget resource = resource()
        .path("containers").path(containerId).path("rename");

    if (name == null) {
      throw new IllegalArgumentException("Cannot rename container to null");
    }

    checkArgument(CONTAINER_NAME_PATTERN.matcher(name).matches(),
                  "Invalid container name: \"%s\"", name);
    resource = resource.queryParam("name", name);

    log.info("Renaming container with id {}. New name {}.", containerId, name);

    try {
      request(POST, resource, resource.request());
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        case 409:
          throw new ContainerRenameConflictException(containerId, name, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public List<ImageSearchResult> searchImages(final String term)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("images").path("search").queryParam("term", term);
    return request(GET, IMAGES_SEARCH_RESULT_LIST, resource,
                   resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  @Deprecated
  public void load(final String image, final InputStream imagePayload)
      throws DockerException, InterruptedException {
    create(image, imagePayload);
  }

  @Override
  @Deprecated
  public void load(final String image, final InputStream imagePayload,
                   final AuthConfig authConfig)
      throws DockerException, InterruptedException {
    create(image, imagePayload);
  }

  @Override
  @Deprecated
  public void load(final String image, final InputStream imagePayload,
                   final ProgressHandler handler)
      throws DockerException, InterruptedException {
    create(image, imagePayload, handler);
  }

  @Override
  @Deprecated
  public void load(final String image, final InputStream imagePayload,
                   final AuthConfig authConfig, final ProgressHandler handler)
      throws DockerException, InterruptedException {
    create(image, imagePayload, handler);
  }

  @Override
  public void create(final String image, final InputStream imagePayload)
      throws DockerException, InterruptedException {
    create(image, imagePayload, new LoggingPullHandler("image stream"));
  }

  @Override
  public void create(final String image, final InputStream imagePayload,
                     final ProgressHandler handler)
      throws DockerException, InterruptedException {
    WebTarget resource = resource().path("images").path("create");

    resource = resource
        .queryParam("fromSrc", "-")
        .queryParam("tag", image);

    final LoadProgressHandler loadProgressHandler = new LoadProgressHandler(handler);
    final Entity<InputStream> entity = Entity.entity(imagePayload,
                                                     APPLICATION_OCTET_STREAM);
    try (final ProgressStream load =
             request(POST, ProgressStream.class, resource,
                     resource.request(APPLICATION_JSON_TYPE), entity)) {
      load.tail(loadProgressHandler, POST, resource.getUri());
      tag(loadProgressHandler.getImageId(), image, true);
    } catch (IOException e) {
      throw new DockerException(e);
    } finally {
      IOUtils.closeQuietly(imagePayload);
    }
  }

  @Override
  public void load(final InputStream imagePayload)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("images").path("load");

    final Entity<InputStream> entity = Entity.entity(imagePayload, APPLICATION_OCTET_STREAM);
    try {
      request(POST, ProgressStream.class, resource,
              resource.request(APPLICATION_JSON_TYPE), entity);
    } finally {
      IOUtils.closeQuietly(imagePayload);
    }
  }

  @Override
  public InputStream save(final String... images)
      throws DockerException, IOException, InterruptedException {
    WebTarget resource;
    if (images.length == 1) {
      resource = resource().path("images").path(images[0]).path("get");
    } else {
      resource = resource().path("images").path("get");
      if (images.length > 1) {
        for (final String image : images) {
          if (!isNullOrEmpty(image)) {
            resource = resource.queryParam("names", urlEncode(image));
          }
        }
      }
    }

    return request(
        GET,
        InputStream.class,
        resource,
        resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  @Deprecated
  public InputStream save(final String image, final AuthConfig authConfig)
      throws DockerException, IOException, InterruptedException {
    return save(image);
  }

  @Override
  public InputStream saveMultiple(final String... images)
      throws DockerException, IOException, InterruptedException {

    final WebTarget resource = resource().path("images").path("get");
    if (images != null) {
      for (final String image : images) {
        resource.queryParam("names", urlEncode(image));
      }
    }

    return request(
        GET,
        InputStream.class,
        resource,
        resource.request(APPLICATION_JSON_TYPE).header("X-Registry-Auth", authHeader(authConfig))
    );
  }

  @Override
  public void pull(final String image) throws DockerException, InterruptedException {
    pull(image, new LoggingPullHandler(image));
  }

  @Override
  public void pull(final String image, final ProgressHandler handler)
      throws DockerException, InterruptedException {
    pull(image, authConfig, handler);
  }

  @Override
  public void pull(final String image, final AuthConfig authConfig)
      throws DockerException, InterruptedException {
    pull(image, authConfig, new LoggingPullHandler(image));
  }

  @Override
  public void pull(final String image, final AuthConfig authConfig, final ProgressHandler handler)
      throws DockerException, InterruptedException {
    final ImageRef imageRef = new ImageRef(image);

    WebTarget resource = resource().path("images").path("create");

    resource = resource.queryParam("fromImage", imageRef.getImage());
    if (imageRef.getTag() != null) {
      resource = resource.queryParam("tag", imageRef.getTag());
    }

    try (ProgressStream pull =
             request(POST, ProgressStream.class, resource,
                     resource
                         .request(APPLICATION_JSON_TYPE)
                         .header("X-Registry-Auth", authHeader(authConfig)))) {
      pull.tail(handler, POST, resource.getUri());
    } catch (IOException e) {
      throw new DockerException(e);
    }
  }

  @Override
  public void push(final String image) throws DockerException, InterruptedException {
    push(image, new LoggingPushHandler(image));
  }

  @Override
  public void push(final String image, final AuthConfig authconfig)
      throws DockerException, InterruptedException {
    push(image, new LoggingPushHandler(image), authconfig);
  }

  @Override
  public void push(final String image, final ProgressHandler handler)
      throws DockerException, InterruptedException {
    push(image, handler, authConfig);
  }

  @Override
  public void push(final String image, final ProgressHandler handler, final AuthConfig authConfig)
      throws DockerException, InterruptedException {
    final ImageRef imageRef = new ImageRef(image);

    WebTarget resource = resource().path("images").path(imageRef.getImage()).path("push");

    if (imageRef.getTag() != null) {
      resource = resource.queryParam("tag", imageRef.getTag());
    }

    // the docker daemon requires that the X-Registry-Auth header is specified
    // with a non-empty string even if your registry doesn't use authentication
    try (ProgressStream push =
             request(POST, ProgressStream.class, resource,
                     resource.request(APPLICATION_JSON_TYPE)
                         .header("X-Registry-Auth", authHeader(authConfig)))) {
      push.tail(handler, POST, resource.getUri());
    } catch (IOException e) {
      throw new DockerException(e);
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ImageNotFoundException(image, e);
      }
    }
  }

  @Override
  public void tag(final String image, final String name)
      throws DockerException, InterruptedException {
    tag(image, name, false);
  }

  @Override
  public void tag(final String image, final String name, final boolean force)
      throws DockerException, InterruptedException {
    final ImageRef imageRef = new ImageRef(name);

    WebTarget resource = resource().path("images").path(image).path("tag");

    resource = resource.queryParam("repo", imageRef.getImage());
    if (imageRef.getTag() != null) {
      resource = resource.queryParam("tag", imageRef.getTag());
    }

    if (force) {
      resource = resource.queryParam("force", true);
    }

    try {
      request(POST, resource, resource.request());
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 400:
          throw new BadParamException(getQueryParamMap(resource), e);
        case 404:
          throw new ImageNotFoundException(image, e);
        case 409:
          throw new ConflictException(e);
        default:
          throw e;
      }
    }
  }

  @Override
  public String build(final Path directory, final BuildParam... params)
      throws DockerException, InterruptedException, IOException {
    return build(directory, null, new LoggingBuildHandler(), params);
  }

  @Override
  public String build(final Path directory, final String name, final BuildParam... params)
      throws DockerException, InterruptedException, IOException {
    return build(directory, name, new LoggingBuildHandler(), params);
  }

  @Override
  public String build(final Path directory, final ProgressHandler handler,
                      final BuildParam... params)
      throws DockerException, InterruptedException, IOException {
    return build(directory, null, handler, params);
  }

  @Override
  public String build(final Path directory, final String name, final ProgressHandler handler,
                      final BuildParam... params)
      throws DockerException, InterruptedException, IOException {
    return build(directory, name, null, handler, params);
  }

  @Override
  public String build(final Path directory, final String name, final String dockerfile,
                      final ProgressHandler handler, final BuildParam... params)
      throws DockerException, InterruptedException, IOException {
    checkNotNull(handler, "handler");

    WebTarget resource = noTimeoutResource().path("build");

    for (final BuildParam param : params) {
      resource = resource.queryParam(param.name(), param.value());
    }
    if (name != null) {
      resource = resource.queryParam("t", name);
    }
    if (dockerfile != null) {
      resource = resource.queryParam("dockerfile", dockerfile);
    }

    log.debug("Auth Config {}", authConfig);

    // Convert auth to X-Registry-Config format
    AuthRegistryConfig authRegistryConfig;
    if (authConfig == null) {
      authRegistryConfig = AuthRegistryConfig.EMPTY;
    } else {
      authRegistryConfig = new AuthRegistryConfig(authConfig.serverAddress(),
                                                  authConfig.username(),
                                                  authConfig.password(),
                                                  authConfig.email(),
                                                  authConfig.serverAddress());
    }

    try (final CompressedDirectory compressedDirectory = CompressedDirectory.create(directory);
         final InputStream fileStream = Files.newInputStream(compressedDirectory.file());
         final ProgressStream build =
             request(POST, ProgressStream.class, resource,
                     resource.request(APPLICATION_JSON_TYPE)
                         .header("X-Registry-Config",
                                 authRegistryHeader(authRegistryConfig)),
                     Entity.entity(fileStream, "application/tar"))) {

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
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ImageNotFoundException(image, e);
        case 409:
          throw new ConflictException(e);
        default:
          throw e;
      }
    }
  }

  @Override
  public List<ImageHistory> history(final String image)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource()
        .path("images")
        .path(image)
        .path("history");
    try {
      return request(GET, IMAGE_HISTORY_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
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
  public LogStream logs(final String containerId, final LogsParam... params)
      throws DockerException, InterruptedException {
    WebTarget resource = noTimeoutResource()
        .path("containers").path(containerId)
        .path("logs");

    for (final LogsParam param : params) {
      resource = resource.queryParam(param.name(), param.value());
    }

    return getLogStream(GET, resource, containerId);
  }

  @Override
  public EventStream events(EventsParam... params)
      throws DockerException, InterruptedException {
    WebTarget resource = noTimeoutResource().path("events");
    final Map<String, String> filters = newHashMap();
    for (final EventsParam param : params) {
      if (param instanceof EventsFilterParam) {
        filters.put(param.name(), param.value());
      } else {
        resource = resource.queryParam(param.name(), param.value());
      }
    }

    try {
      if (!filters.isEmpty()) {
        final StringWriter writer = new StringWriter();
        final JsonGenerator generator = objectMapper().getFactory().createGenerator(writer);
        generator.writeStartObject();
        for (final Map.Entry<String, String> entry : filters.entrySet()) {
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
    } catch (IOException exception) {
      throw new DockerException(exception);
    }

    try {
      final CloseableHttpClient client = (CloseableHttpClient) ApacheConnectorProvider
          .getHttpClient(noTimeoutClient);
      final CloseableHttpResponse response = client.execute(new HttpGet(resource.getUri()));
      return new EventStream(response, objectMapper());
    } catch (IOException exception) {
      throw new DockerException(exception);
    }
  }

  @Override
  public LogStream attachContainer(final String containerId,
                                   final AttachParameter... params) throws DockerException,
                                                                           InterruptedException {
    WebTarget resource = noTimeoutResource().path("containers").path(containerId).path("attach");

    for (final AttachParameter param : params) {
      resource = resource.queryParam(param.name().toLowerCase(Locale.ROOT), String.valueOf(true));
    }

    return getLogStream(POST, resource, containerId);
  }

  private LogStream getLogStream(final String method, final WebTarget resource,
                                 final String containerId)
      throws DockerException, InterruptedException {
    try {
      final Invocation.Builder request = resource.request("application/vnd.docker.raw-stream");
      return request(method, LogStream.class, resource, request);
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 400:
          throw new BadParamException(getQueryParamMap(resource), e);
        case 404:
          throw new ContainerNotFoundException(containerId);
        default:
          throw e;
      }
    }
  }

  @Override
  public ExecCreation execCreate(final String containerId,
                                 final String[] cmd,
                                 final ExecCreateParam... params)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("containers").path(containerId).path("exec");

    final StringWriter writer = new StringWriter();
    try {
      final JsonGenerator generator = objectMapper().getFactory().createGenerator(writer);
      generator.writeStartObject();

      for (final ExecCreateParam param : params) {
        if (param.value().equals("true") || param.value().equals("false")) {
          generator.writeBooleanField(param.name(), Boolean.valueOf(param.value()));
        } else {
          generator.writeStringField(param.name(), param.value());
        }
      }

      generator.writeArrayFieldStart("Cmd");
      for (final String s : cmd) {
        generator.writeString(s);
      }
      generator.writeEndArray();

      generator.writeEndObject();
      generator.close();
    } catch (IOException e) {
      throw new DockerException(e);
    }

    try {
      return request(POST, ExecCreation.class, resource, resource.request(APPLICATION_JSON_TYPE),
                     Entity.json(writer.toString()));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        case 409:
          throw new ExecCreateConflictException(containerId, e);
        default:
          throw e;
      }
    }
  }


  @Override
  public LogStream execStart(final String execId, final ExecStartParameter... params)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("exec").path(execId).path("start");

    final StringWriter writer = new StringWriter();
    try {
      final JsonGenerator generator = objectMapper().getFactory().createGenerator(writer);
      generator.writeStartObject();

      for (final ExecStartParameter param : params) {
        generator.writeBooleanField(param.getName(), true);
      }

      generator.writeEndObject();
      generator.close();
    } catch (IOException e) {
      throw new DockerException(e);
    }

    try {
      return request(POST, LogStream.class, resource,
                     resource.request("application/vnd.docker.raw-stream"),
                     Entity.json(writer.toString()));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ExecNotFoundException(execId, e);
        case 409:
          throw new ExecStartConflictException(execId, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public Swarm inspectSwarm() throws DockerException, InterruptedException {
    assertAPIVersionIsAbove("1.24");

    final WebTarget resource = resource().path("swarm");
    return request(GET, Swarm.class, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public ServiceCreateResponse createService(final ServiceSpec spec,
                                             final ServiceCreateOptions options)
      throws DockerException, InterruptedException {
    assertAPIVersionIsAbove("1.24");
    final WebTarget resource = resource().path("services").path("create");

    try {
      return request(POST, ServiceCreateResponse.class, resource,
                     resource.request(APPLICATION_JSON_TYPE), Entity.json(spec));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 406:
          throw new DockerException("Server error or node is not part of swarm.", e);
        case 409:
          throw new DockerException("Name conflicts with an existing object.", e);
        default:
          throw e;
      }
    }
  }

  @Override
  public Service inspectService(final String serviceId)
      throws DockerException, InterruptedException {
    assertAPIVersionIsAbove("1.24");
    try {
      final WebTarget resource = resource().path("services").path(serviceId);
      return request(GET, Service.class, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ServiceNotFoundException(serviceId);
        default:
          throw e;
      }
    }
  }

  @Override
  public void updateService(final String serviceId, final Long version, final ServiceSpec spec)
      throws DockerException, InterruptedException {
    assertAPIVersionIsAbove("1.24");
    try {
      WebTarget resource = resource().path("services").path(serviceId).path("update");
      resource = resource.queryParam("version", version);
      request(POST, String.class, resource, resource.request(APPLICATION_JSON_TYPE),
              Entity.json(spec));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ServiceNotFoundException(serviceId);
        default:
          throw e;
      }
    }
  }

  @Override
  public List<Service> listServices() throws DockerException, InterruptedException {
    assertAPIVersionIsAbove("1.24");
    final WebTarget resource = resource().path("services");
    return request(GET, SERVICE_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public List<Service> listServices(final Service.Criteria criteria)
      throws DockerException, InterruptedException {
    assertAPIVersionIsAbove("1.24");
    WebTarget resource = resource().path("services");
    final Map<String, List<String>> filters = new HashMap<>();

    if (criteria.getServiceId() != null) {
      filters.put("id", Collections.singletonList(criteria.getServiceId()));
    }
    if (criteria.getServiceName() != null) {
      filters.put("name", Collections.singletonList(criteria.getServiceName()));
    }

    resource = resource.queryParam("filters", urlEncodeFilters(filters));
    return request(GET, SERVICE_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public void removeService(final String serviceId) throws DockerException, InterruptedException {
    assertAPIVersionIsAbove("1.24");
    try {
      final WebTarget resource = resource().path("services").path(serviceId);
      request(DELETE, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ServiceNotFoundException(serviceId);
        default:
          throw e;
      }
    }
  }

  @Override
  public Task inspectTask(final String taskId) throws DockerException, InterruptedException {
    assertAPIVersionIsAbove("1.24");
    try {
      final WebTarget resource = resource().path("tasks").path(taskId);
      return request(GET, Task.class, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new TaskNotFoundException(taskId);
        default:
          throw e;
      }
    }
  }

  @Override
  public List<Task> listTasks() throws DockerException, InterruptedException {
    assertAPIVersionIsAbove("1.24");
    final WebTarget resource = resource().path("tasks");
    return request(GET, TASK_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public List<Task> listTasks(final Task.Criteria criteria)
      throws DockerException, InterruptedException {
    assertAPIVersionIsAbove("1.24");
    WebTarget resource = resource().path("tasks");
    final Map<String, List<String>> filters = new HashMap<String, List<String>>();

    if (criteria.getTaskId() != null) {
      filters.put("id", Collections.singletonList(criteria.getTaskId()));
    }
    if (criteria.getTaskName() != null) {
      filters.put("name", Collections.singletonList(criteria.getTaskName()));
    }
    if (criteria.getServiceName() != null) {
      filters.put("service", Collections.singletonList(criteria.getServiceName()));
    }
    if (criteria.getNodeId() != null) {
      filters.put("node", Collections.singletonList(criteria.getNodeId()));
    }
    if (criteria.getLabel() != null) {
      filters.put("label", Collections.singletonList(criteria.getLabel()));
    }
    if (criteria.getDesiredState() != null) {
      filters.put("desired-state", Collections.singletonList(criteria.getDesiredState()));
    }

    resource = resource.queryParam("filters", urlEncodeFilters(filters));
    return request(GET, TASK_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public void execResizeTty(final String execId,
                            final Integer height,
                            final Integer width)
      throws DockerException, InterruptedException {
    checkTtyParams(height, width);

    WebTarget resource = resource().path("exec").path(execId).path("resize");
    if (height != null && height > 0) {
      resource = resource.queryParam("h", height);
    }
    if (width != null && width > 0) {
      resource = resource.queryParam("w", width);
    }

    try {
      request(POST, resource, resource.request(TEXT_PLAIN_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ExecNotFoundException(execId, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public ExecState execInspect(final String execId) throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("exec").path(execId).path("json");

    try {
      return request(GET, ExecState.class, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ExecNotFoundException(execId, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public ContainerStats stats(final String containerId)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("containers").path(containerId).path("stats")
        .queryParam("stream", "0");

    try {
      return request(GET, ContainerStats.class, resource, resource.request(APPLICATION_JSON_TYPE));
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
  public void resizeTty(final String containerId, final Integer height, final Integer width)
      throws DockerException, InterruptedException {
    checkTtyParams(height, width);

    WebTarget resource = resource().path("containers").path(containerId).path("resize");
    if (height != null && height > 0) {
      resource = resource.queryParam("h", height);
    }
    if (width != null && width > 0) {
      resource = resource.queryParam("w", width);
    }

    try {
      request(POST, resource, resource.request(TEXT_PLAIN_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new ContainerNotFoundException(containerId, e);
        default:
          throw e;
      }
    }
  }

  private void checkTtyParams(final Integer height, final Integer width) throws BadParamException {
    if ((height == null && width == null) || (height != null && height == 0) ||
        (width != null && width == 0)) {
      final Map<String, String> paramMap = Maps.newHashMap();
      paramMap.put("h", height == null ? null : height.toString());
      paramMap.put("w", width == null ? null : width.toString());
      throw new BadParamException(paramMap, "Either width or height must be non-null and > 0");
    }
  }

  @Override
  public List<Network> listNetworks() throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("networks");
    return request(GET, NETWORK_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  @Override
  public Network inspectNetwork(String networkId) throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("networks").path(networkId);
    try {
      return request(GET, Network.class, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new NetworkNotFoundException(networkId, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public NetworkCreation createNetwork(NetworkConfig networkConfig)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("networks").path("create");

    try {
      return request(POST, NetworkCreation.class, resource, resource.request(APPLICATION_JSON_TYPE),
                     Entity.json(networkConfig));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new NotFoundException("Plugin not found", e);
        default:
          throw e;
      }
    }
  }

  @Override
  public void removeNetwork(String networkId) throws DockerException, InterruptedException {
    try {
      final WebTarget resource = resource().path("networks").path(networkId);
      request(DELETE, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new NetworkNotFoundException(networkId, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public void connectToNetwork(String containerId, String networkId)
      throws DockerException, InterruptedException {
    manageNetworkConnection(containerId, "connect", networkId);
  }

  @Override
  public void disconnectFromNetwork(String containerId, String networkId)
      throws DockerException, InterruptedException {
    manageNetworkConnection(containerId, "disconnect", networkId);
  }

  private void manageNetworkConnection(String containerId, String methodname, String networkId)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("networks").path(networkId).path(methodname);

    final Map<String, String> request = new HashMap<>();
    request.put("Container", containerId);

    try {
      request(POST, Response.class, resource, resource.request(APPLICATION_JSON_TYPE),
              Entity.json(request));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          final String message = String.format("Container %s or network %s not found.",
                                               containerId, networkId);
          throw new NotFoundException(message, e);
        case 500:
          throw e;
      }
    }
  }

  @Override
  public Volume createVolume() throws DockerException, InterruptedException {
    return createVolume(Volume.builder().build());
  }

  @Override
  public Volume createVolume(final Volume volume) throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("volumes").path("create");

    return request(POST, Volume.class, resource,
                   resource.request(APPLICATION_JSON_TYPE),
                   Entity.json(volume));
  }

  @Override
  public Volume inspectVolume(final String volumeName)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("volumes").path(volumeName);
    try {
      return request(GET, Volume.class, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new VolumeNotFoundException(volumeName, e);
        default:
          throw e;
      }
    }
  }

  @Override
  public void removeVolume(final Volume volume)
      throws DockerException, InterruptedException {
    removeVolume(volume.name());
  }

  @Override
  public void removeVolume(final String volumeName)
      throws DockerException, InterruptedException {
    final WebTarget resource = resource().path("volumes").path(volumeName);
    try {
      request(DELETE, resource, resource.request(APPLICATION_JSON_TYPE));
    } catch (DockerRequestException e) {
      switch (e.status()) {
        case 404:
          throw new VolumeNotFoundException(volumeName, e);
        case 409:
          throw new ConflictException("Volume is in use and cannot be removed", e);
        default:
          throw e;
      }
    }
  }

  @Override
  public VolumeList listVolumes(ListVolumesParam... params)
      throws DockerException, InterruptedException {
    WebTarget resource = resource().path("volumes");

    final Map<String, List<String>> filters = newHashMap();
    for (final ListVolumesParam param : params) {
      if (param instanceof ListVolumesFilterParam) {
        List<String> filterValueList;
        if (filters.containsKey(param.name())) {
          filterValueList = filters.get(param.name());
        } else {
          filterValueList = Lists.newArrayList();
        }
        filterValueList.add(param.value());
        filters.put(param.name(), filterValueList);
      } else {
        resource = resource.queryParam(urlEncode(param.name()), urlEncode(param.value()));
      }
    }

    if (!filters.isEmpty()) {
      // If filters were specified, we must put them in a JSON object and pass them using the
      // 'filters' query param like this: filters={"dangling":["true"]}. If filters is an empty map,
      // urlEncodeFilters will return null and queryParam() will remove that query parameter.
      resource = resource.queryParam("filters", urlEncodeFilters(filters));
    }

    return request(GET, VolumeList.class, resource, resource.request(APPLICATION_JSON_TYPE));
  }

  private WebTarget resource() {
    final WebTarget target = client.target(uri);
    if (!isNullOrEmpty(apiVersion)) {
      return target.path(apiVersion);
    }
    return target;
  }

  private WebTarget noTimeoutResource() {
    final WebTarget target = noTimeoutClient.target(uri);
    if (!isNullOrEmpty(apiVersion)) {
      return target.path(apiVersion);
    }
    return target;
  }

  private <T> T request(final String method, final GenericType<T> type,
                        final WebTarget resource, final Invocation.Builder request)
      throws DockerException, InterruptedException {
    try {
      return headers(request).async().method(method, type).get();
    } catch (ExecutionException | MultiException e) {
      throw propagate(method, resource, e);
    }
  }

  private <T> T request(final String method, final Class<T> clazz,
                        final WebTarget resource, final Invocation.Builder request)
      throws DockerException, InterruptedException {
    try {
      return headers(request).async().method(method, clazz).get();
    } catch (ExecutionException | MultiException e) {
      throw propagate(method, resource, e);
    }
  }

  private <T> T request(final String method, final Class<T> clazz,
                        final WebTarget resource, final Invocation.Builder request,
                        final Entity<?> entity)
      throws DockerException, InterruptedException {
    try {
      return headers(request).async().method(method, entity, clazz).get();
    } catch (ExecutionException | MultiException e) {
      throw propagate(method, resource, e);
    }
  }

  private void request(final String method,
                       final WebTarget resource,
                       final Invocation.Builder request)
      throws DockerException, InterruptedException {
    try {
      headers(request).async().method(method, String.class).get();
    } catch (ExecutionException | MultiException e) {
      throw propagate(method, resource, e);
    }
  }

  private Invocation.Builder headers(final Invocation.Builder request) {
    final Set<Map.Entry<String, Object>> entries = headers.entrySet();

    for (final Map.Entry<String, Object> entry : entries) {
      request.header(entry.getKey(), entry.getValue());
    }

    return request;
  }

  private RuntimeException propagate(final String method, final WebTarget resource,
                                     final Exception e)
      throws DockerException, InterruptedException {
    Throwable cause = e.getCause();

    // Sometimes e is a org.glassfish.hk2.api.MultiException
    // which contains the cause we're actually interested in.
    // So we unpack it here.
    if (e instanceof MultiException) {
      cause = cause.getCause();
    }

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
    } else if ((cause instanceof InterruptedIOException)
               || (cause instanceof InterruptedException)) {
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

  private String authHeader(final AuthConfig authConfig) throws DockerException {
    if (authConfig == null) {
      return "null";
    }
    try {
      return Base64.encodeAsString(ObjectMapperProvider
                                       .objectMapper()
                                       .writeValueAsString(authConfig));
    } catch (JsonProcessingException ex) {
      throw new DockerException("Could not encode X-Registry-Auth header", ex);
    }
  }

  private String authRegistryHeader(final AuthRegistryConfig authRegistryConfig)
      throws DockerException {
    if (authRegistryConfig == null) {
      return "null";
    }
    try {
      String authRegistryJson =
          ObjectMapperProvider.objectMapper().writeValueAsString(authRegistryConfig);

      final String apiVersion = version().apiVersion();
      final int versionComparison = compareVersion(apiVersion, "1.19");

      // Version below 1.19
      if (versionComparison < 0) {
        authRegistryJson = "{\"configs\":" + authRegistryJson + "}";
      } else if (versionComparison == 0) {
        // Version equal 1.19
        authRegistryJson = "{\"auths\":" + authRegistryJson + "}";
      }

      log.debug("Registry Config Json {}", authRegistryJson);
      final String authRegistryEncoded = Base64.encodeAsString(authRegistryJson);
      log.debug("Registry Config Encoded {}", authRegistryEncoded);
      return authRegistryEncoded;
    } catch (JsonProcessingException | InterruptedException ex) {
      throw new DockerException("Could not encode X-Registry-Config header", ex);
    }
  }

  private void assertAPIVersionIsAbove(String minimumVersion)
      throws DockerException, InterruptedException {
    final String apiVersion = version().apiVersion();
    final int versionComparison = compareVersion(apiVersion, minimumVersion);

    // Version above minimumVersion
    if (versionComparison < 0) {
      throw new UnsupportedApiVersionException(apiVersion);
    }
  }

  /**
   * Create a new {@link DefaultDockerClient} builder.
   *
   * @return Returns a builder that can be used to further customize and then build the client.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Create a new {@link DefaultDockerClient} builder prepopulated with values loaded from the
   * DOCKER_HOST and DOCKER_CERT_PATH environment variables.
   *
   * @return Returns a builder that can be used to further customize and then build the client.
   * @throws DockerCertificateException if we could not build a DockerCertificates object
   */
  public static Builder fromEnv() throws DockerCertificateException {
    final String endpoint = DockerHost.endpointFromEnv();
    final Path dockerCertPath = Paths.get(firstNonNull(DockerHost.certPathFromEnv(),
                                                       DockerHost.defaultCertPath()));

    final Builder builder = new Builder();

    final Optional<DockerCertificates> certs = DockerCertificates.builder()
        .dockerCertPath(dockerCertPath).build();

    if (endpoint.startsWith(UNIX_SCHEME + "://")) {
      builder.uri(endpoint);
    } else {
      final String stripped = endpoint.replaceAll(".*://", "");
      final HostAndPort hostAndPort = HostAndPort.fromString(stripped);
      final String hostText = hostAndPort.getHostText();
      final String scheme = certs.isPresent() ? "https" : "http";

      final int port = hostAndPort.getPortOrDefault(DockerHost.defaultPort());
      final String address = isNullOrEmpty(hostText) ? DockerHost.defaultAddress() : hostText;

      builder.uri(scheme + "://" + address + ":" + port);
    }

    if (certs.isPresent()) {
      builder.dockerCertificates(certs.get());
    }

    return builder;
  }

  public static class Builder {

    private URI uri;
    private String apiVersion;
    private long connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT_MILLIS;
    private long readTimeoutMillis = DEFAULT_READ_TIMEOUT_MILLIS;
    private int connectionPoolSize = DEFAULT_CONNECTION_POOL_SIZE;
    private DockerCertificates dockerCertificates;
    private boolean dockerAuth;
    private AuthConfig authConfig;
    private Map<String, Object> headers = new HashMap<>();

    public URI uri() {
      return uri;
    }

    public Builder uri(final URI uri) {
      this.uri = uri;
      return this;
    }

    /**
     * Set the URI for connections to Docker.
     *
     * @param uri URI String for connections to Docker
     * @return Builder
     */
    public Builder uri(final String uri) {
      return uri(URI.create(uri));
    }

    /**
     * Set the Docker API version that will be used in the HTTP requests to Docker daemon.
     *
     * @param apiVersion String for Docker API version
     * @return Builder
     */
    public Builder apiVersion(final String apiVersion) {
      this.apiVersion = apiVersion;
      return this;
    }

    public String apiVersion() {
      return apiVersion;
    }

    public long connectTimeoutMillis() {
      return connectTimeoutMillis;
    }

    /**
     * Set the timeout in milliseconds until a connection to Docker is established. A timeout value
     * of zero is interpreted as an infinite timeout.
     *
     * @param connectTimeoutMillis connection timeout to Docker daemon in milliseconds
     * @return Builder
     */
    public Builder connectTimeoutMillis(final long connectTimeoutMillis) {
      this.connectTimeoutMillis = connectTimeoutMillis;
      return this;
    }

    public long readTimeoutMillis() {
      return readTimeoutMillis;
    }

    /**
     * Set the SO_TIMEOUT in milliseconds. This is the maximum period of inactivity between
     * receiving two consecutive data packets from Docker.
     *
     * @param readTimeoutMillis read timeout to Docker daemon in milliseconds
     * @return Builder
     */
    public Builder readTimeoutMillis(final long readTimeoutMillis) {
      this.readTimeoutMillis = readTimeoutMillis;
      return this;
    }

    public DockerCertificates dockerCertificates() {
      return dockerCertificates;
    }

    /**
     * Provide certificates to secure the connection to Docker.
     *
     * @param dockerCertificates DockerCertificates object
     * @return Builder
     */
    public Builder dockerCertificates(final DockerCertificates dockerCertificates) {
      this.dockerCertificates = dockerCertificates;
      return this;
    }

    public int connectionPoolSize() {
      return connectionPoolSize;
    }

    /**
     * Set the size of the connection pool for connections to Docker. Note that due to a known
     * issue, DefaultDockerClient maintains two separate connection pools, each of which is capped
     * at this size. Therefore, the maximum number of concurrent connections to Docker may be up to
     * 2 * connectionPoolSize.
     *
     * @param connectionPoolSize connection pool size
     * @return Builder
     */
    public Builder connectionPoolSize(final int connectionPoolSize) {
      this.connectionPoolSize = connectionPoolSize;
      return this;
    }

    public boolean dockerAuth() {
      return dockerAuth;
    }

    /**
     * Allows reusing Docker auth info
     *
     * @param dockerAuth tells if Docker auth info should be used
     * @return Builder
     */
    public Builder dockerAuth(final boolean dockerAuth) {
      this.dockerAuth = dockerAuth;
      return this;
    }

    public AuthConfig authConfig() {
      return authConfig;
    }

    /**
     * Set the auth parameters for pull/push requests from/to private repositories.
     *
     * @param authConfig AuthConfig object
     * @return Builder
     */
    public Builder authConfig(final AuthConfig authConfig) {
      this.authConfig = authConfig;
      return this;
    }

    public DefaultDockerClient build() {
      if (dockerAuth) {
        try {
          this.authConfig = AuthConfig.fromDockerConfig().build();
        } catch (IOException e) {
          log.warn("Unable to use Docker auth info", e);
        }
      }
      return new DefaultDockerClient(this);
    }

    public Builder header(String name, Object value) {
      headers.put(name, value);
      return this;
    }

    public Map<String, Object> headers() {
      return headers;
    }
  }

}
