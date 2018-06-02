/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.docker.client;

import static com.spotify.docker.FixtureUtil.fixture;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Resources;
import com.spotify.docker.client.DockerClient.Signal;
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.ConflictException;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.NodeNotFoundException;
import com.spotify.docker.client.exceptions.NonSwarmNodeException;
import com.spotify.docker.client.exceptions.NotFoundException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.Distribution;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.HostConfig.Bind;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.Volume;
import com.spotify.docker.client.messages.swarm.Config;
import com.spotify.docker.client.messages.swarm.ConfigBind;
import com.spotify.docker.client.messages.swarm.ConfigCreateResponse;
import com.spotify.docker.client.messages.swarm.ConfigFile;
import com.spotify.docker.client.messages.swarm.ConfigSpec;
import com.spotify.docker.client.messages.swarm.ContainerSpec;
import com.spotify.docker.client.messages.swarm.EngineConfig;
import com.spotify.docker.client.messages.swarm.Node;
import com.spotify.docker.client.messages.swarm.NodeDescription;
import com.spotify.docker.client.messages.swarm.NodeInfo;
import com.spotify.docker.client.messages.swarm.NodeSpec;
import com.spotify.docker.client.messages.swarm.Placement;
import com.spotify.docker.client.messages.swarm.Preference;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.Spread;
import com.spotify.docker.client.messages.swarm.SwarmJoin;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;

import org.glassfish.jersey.client.RequestEntityProcessing;
import org.glassfish.jersey.internal.util.Base64;
import org.hamcrest.core.IsNot;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests DefaultDockerClient against a {@link okhttp3.mockwebserver.MockWebServer} instance, so
 * we can assert what the HTTP requests look like that DefaultDockerClient sends and test how
 * DefaltDockerClient behaves given certain responses from the Docker Remote API.
 * <p>
 * This test may not be a true "unit test", but using a MockWebServer where we can control the HTTP
 * responses sent by the server and capture the HTTP requests sent by the class-under-test is far
 * simpler that attempting to mock the {@link javax.ws.rs.client.Client} instance used by
 * DefaultDockerClient, since the Client has such a rich/fluent interface and many methods/classes
 * that would need to be mocked. Ultimately for testing DefaultDockerClient all we care about is
 * the HTTP requests it sends, rather than what HTTP client library it uses.</p>
 * <p>
 * When adding new functionality to DefaultDockerClient, please consider and prioritize adding unit
 * tests to cover the new functionality in this file rather than integration tests that require a
 * real docker daemon in {@link DefaultDockerClientTest}. While integration tests are valuable,
 * they are more brittle and harder to run than a simple unit test that captures/asserts HTTP
 * requests and responses.</p>
 *
 * @see <a href="https://github.com/square/okhttp/tree/master/mockwebserver">
 * https://github.com/square/okhttp/tree/master/mockwebserver</a>
 */
public class DefaultDockerClientUnitTest {

  private final MockWebServer server = new MockWebServer();

  private DefaultDockerClient.Builder builder;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void setup() throws Exception {
    server.start();

    builder = DefaultDockerClient.builder();
    builder.uri(server.url("/").uri());
  }

  @After
  public void tearDown() throws Exception {
    server.shutdown();
  }

  @Test
  public void testHostForUnixSocket() {
    final DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("unix:///var/run/docker.sock").build();
    assertThat(client.getHost(), equalTo("localhost"));
  }

  @Test
  public void testHostForLocalHttps() {
    final DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://localhost:2375").build();
    assertThat(client.getHost(), equalTo("localhost"));
  }

  @Test
  public void testHostForFqdnHttps() {
    final DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://perdu.com:2375").build();
    assertThat(client.getHost(), equalTo("perdu.com"));
  }

  @Test
  public void testHostForIpHttps() {
    final DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://192.168.53.103:2375").build();
    assertThat(client.getHost(), equalTo("192.168.53.103"));
  }

  @Test
  public void testHostWithProxy() {
    try {
      System.setProperty("http.proxyHost", "gmodules.com");
      System.setProperty("http.proxyPort", "80");
      final DefaultDockerClient client = DefaultDockerClient.builder()
              .uri("https://192.168.53.103:2375").build();
      assertThat((String) client.getClient().getConfiguration()
                      .getProperty("jersey.config.client.proxy.uri"),
              equalTo("http://gmodules.com:80"));
    } finally {
      System.clearProperty("http.proxyHost");
      System.clearProperty("http.proxyPort");
    }
  }

  @Test
  public void testHostWithNonProxyHost() {
    try {
      System.setProperty("http.proxyHost", "gmodules.com");
      System.setProperty("http.proxyPort", "80");
      System.setProperty("http.nonProxyHosts", "127.0.0.1|localhost|192.168.*");
      final DefaultDockerClient client = DefaultDockerClient.builder()
              .uri("https://192.168.53.103:2375").build();
      assertThat((String) client.getClient().getConfiguration()
                      .getProperty("jersey.config.client.proxy.uri"),
              isEmptyOrNullString());
    } finally {
      System.clearProperty("http.proxyHost");
      System.clearProperty("http.proxyPort");
      System.clearProperty("http.nonProxyHosts");
    }
  }

  private RecordedRequest takeRequestImmediately() throws InterruptedException {
    return server.takeRequest(1, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testCustomHeaders() throws Exception {
    builder.header("int", 1);
    builder.header("string", "2");
    builder.header("list", Lists.newArrayList("a", "b", "c"));

    server.enqueue(new MockResponse());

    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);
    dockerClient.info();

    final RecordedRequest recordedRequest = takeRequestImmediately();
    assertThat(recordedRequest.getMethod(), is("GET"));
    assertThat(recordedRequest.getPath(), is("/info"));

    assertThat(recordedRequest.getHeader("int"), is("1"));
    assertThat(recordedRequest.getHeader("string"), is("2"));
    // TODO (mbrown): this seems like incorrect behavior - the client should send 3 headers with
    // name "list", not one header with a value of "[a, b, c]"
    assertThat(recordedRequest.getHeaders().values("list"), contains("[a, b, c]"));
  }

  private static JsonNode toJson(Buffer buffer) throws IOException {
    return ObjectMapperProvider.objectMapper().readTree(buffer.inputStream());
  }

  private static JsonNode toJson(byte[] bytes) throws IOException {
    return ObjectMapperProvider.objectMapper().readTree(bytes);
  }

  private static JsonNode toJson(Object object) throws IOException {
    return ObjectMapperProvider.objectMapper().valueToTree(object);
  }

  private static ObjectNode createObjectNode() {
    return ObjectMapperProvider.objectMapper().createObjectNode();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testCapAddAndDrop() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    final HostConfig hostConfig = HostConfig.builder()
        .capAdd(ImmutableList.of("foo", "bar"))
        .capAdd(ImmutableList.of("baz", "qux"))
        .build();

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .build();

    server.enqueue(new MockResponse());

    dockerClient.createContainer(containerConfig);

    final RecordedRequest recordedRequest = takeRequestImmediately();

    assertThat(recordedRequest.getMethod(), is("POST"));
    assertThat(recordedRequest.getPath(), is("/containers/create"));

    assertThat(recordedRequest.getHeader("Content-Type"), is("application/json"));

    // TODO (mbrown): use hamcrest-jackson for this, once we upgrade to Java 8
    final JsonNode requestJson = toJson(recordedRequest.getBody());

    final JsonNode capAddNode = requestJson.get("HostConfig").get("CapAdd");
    assertThat(capAddNode.isArray(), is(true));

    assertThat(childrenTextNodes((ArrayNode) capAddNode), containsInAnyOrder("baz", "qux"));
  }

  private static Set<String> childrenTextNodes(ArrayNode arrayNode) {
    final Set<String> texts = new HashSet<>();
    for (JsonNode child : arrayNode) {
      Preconditions.checkState(child.isTextual(),
          "ArrayNode must only contain text nodes, but found %s in %s",
          child.getNodeType(),
          arrayNode);
      texts.add(child.textValue());
    }
    return texts;
  }

  @Test
  @SuppressWarnings("deprecated")
  public void buildThrowsIfRegistryAuthandRegistryAuthSupplierAreBothSpecified()
      throws DockerCertificateException {
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("LOGIC ERROR");

    final RegistryAuthSupplier authSupplier = mock(RegistryAuthSupplier.class);

    //noinspection deprecation
    DefaultDockerClient.builder()
        .registryAuth(RegistryAuth.builder().identityToken("hello").build())
        .registryAuthSupplier(authSupplier)
        .build();
  }

  @Test
  public void testBuildPassesMultipleRegistryConfigs() throws Exception {
    final RegistryConfigs registryConfigs = RegistryConfigs.create(ImmutableMap.of(
        "server1", RegistryAuth.builder()
            .serverAddress("server1")
            .username("u1")
            .password("p1")
            .email("e1")
            .build(),

        "server2", RegistryAuth.builder()
            .serverAddress("server2")
            .username("u2")
            .password("p2")
            .email("e2")
            .build()
    ));

    final RegistryAuthSupplier authSupplier = mock(RegistryAuthSupplier.class);
    when(authSupplier.authForBuild()).thenReturn(registryConfigs);

    final DefaultDockerClient client = builder.registryAuthSupplier(authSupplier)
        .build();

    // build() calls /version to check what format of header to send
    enqueueServerApiVersion("1.20");

    server.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody(
                fixture("fixtures/1.22/build.json")
            )
    );

    final Path path = Paths.get(Resources.getResource("dockerDirectory").toURI());

    client.build(path);

    final RecordedRequest versionRequest = takeRequestImmediately();
    assertThat(versionRequest.getMethod(), is("GET"));
    assertThat(versionRequest.getPath(), is("/version"));

    final RecordedRequest buildRequest = takeRequestImmediately();
    assertThat(buildRequest.getMethod(), is("POST"));
    assertThat(buildRequest.getPath(), is("/build"));

    final String registryConfigHeader = buildRequest.getHeader("X-Registry-Config");
    assertThat(registryConfigHeader, is(not(nullValue())));

    // check that the JSON in the header is equivalent to what we mocked out above from
    // the registryAuthSupplier
    final JsonNode headerJsonNode = toJson(BaseEncoding.base64().decode(registryConfigHeader));
    assertThat(headerJsonNode, is(toJson(registryConfigs.configs())));
  }

  @Test
  public void testNanoCpus() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    final HostConfig hostConfig = HostConfig.builder()
        .nanoCpus(2_000_000_000L)
        .build();

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .build();

    server.enqueue(new MockResponse());

    dockerClient.createContainer(containerConfig);

    final RecordedRequest recordedRequest = takeRequestImmediately();

    final JsonNode requestJson = toJson(recordedRequest.getBody());
    final JsonNode nanoCpus = requestJson.get("HostConfig").get("NanoCpus");

    assertThat(hostConfig.nanoCpus(), is(nanoCpus.longValue()));
  }

  @Test
  public void testInspectNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    // build() calls /version to check what format of header to send
    enqueueServerApiVersion("1.28");
    enqueueServerApiResponse(200, "fixtures/1.28/nodeInfo.json");

    final NodeInfo nodeInfo = dockerClient.inspectNode("24ifsmvkjbyhk");

    assertThat(nodeInfo, notNullValue());
    assertThat(nodeInfo.id(), is("24ifsmvkjbyhk"));
    assertThat(nodeInfo.status(), notNullValue());
    assertThat(nodeInfo.status().addr(), is("172.17.0.2"));
    assertThat(nodeInfo.managerStatus(), notNullValue());
    assertThat(nodeInfo.managerStatus().addr(), is("172.17.0.2:2377"));
    assertThat(nodeInfo.managerStatus().leader(), is(true));
    assertThat(nodeInfo.managerStatus().reachability(), is("reachable"));
  }

  @Test
  public void testInspectNonLeaderNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.27");

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            fixture("fixtures/1.27/nodeInfoNonLeader.json")
        )
    );

    NodeInfo nodeInfo = dockerClient.inspectNode("24ifsmvkjbyhk");
    assertThat(nodeInfo, notNullValue());
    assertThat(nodeInfo.id(), is("24ifsmvkjbyhk"));
    assertThat(nodeInfo.status(), notNullValue());
    assertThat(nodeInfo.status().addr(), is("172.17.0.2"));
    assertThat(nodeInfo.managerStatus(), notNullValue());
    assertThat(nodeInfo.managerStatus().addr(), is("172.17.0.2:2377"));
    assertThat(nodeInfo.managerStatus().leader(), nullValue());
    assertThat(nodeInfo.managerStatus().reachability(), is("reachable"));
  }

  @Test
  public void testInspectNodeNonManager() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.27");

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            fixture("fixtures/1.27/nodeInfoNonManager.json")
        )
    );

    NodeInfo nodeInfo = dockerClient.inspectNode("24ifsmvkjbyhk");
    assertThat(nodeInfo, notNullValue());
    assertThat(nodeInfo.id(), is("24ifsmvkjbyhk"));
    assertThat(nodeInfo.status(), notNullValue());
    assertThat(nodeInfo.status().addr(), is("172.17.0.2"));
    assertThat(nodeInfo.managerStatus(), nullValue());
  }

  @Test(expected = NodeNotFoundException.class)
  public void testInspectMissingNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    // build() calls /version to check what format of header to send
    enqueueServerApiVersion("1.28");
    enqueueServerApiEmptyResponse(404);

    dockerClient.inspectNode("24ifsmvkjbyhk");
  }

  @Test(expected = NonSwarmNodeException.class)
  public void testInspectNonSwarmNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    // build() calls /version to check what format of header to send
    enqueueServerApiVersion("1.28");
    enqueueServerApiEmptyResponse(503);

    dockerClient.inspectNode("24ifsmvkjbyhk");
  }

  @Test
  public void testUpdateNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.28");
    enqueueServerApiResponse(200, "fixtures/1.28/listNodes.json");

    final List<Node> nodes = dockerClient.listNodes();

    assertThat(nodes.size(), is(1));

    final Node node = nodes.get(0);

    assertThat(node.id(), equalTo("24ifsmvkjbyhk"));
    assertThat(node.version().index(), equalTo(8L));
    assertThat(node.spec().name(), equalTo("my-node"));
    assertThat(node.spec().role(), equalTo("manager"));
    assertThat(node.spec().availability(), equalTo("active"));
    assertThat(node.spec().labels(), hasKey(equalTo("foo")));

    final NodeSpec updatedNodeSpec = NodeSpec.builder(node.spec())
        .addLabel("foobar", "foobar")
        .build();

    enqueueServerApiVersion("1.28");
    enqueueServerApiEmptyResponse(200);

    dockerClient.updateNode(node.id(), node.version().index(), updatedNodeSpec);
  }

  @Test(expected = DockerException.class)
  public void testUpdateNodeWithInvalidVersion() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.28");

    final ObjectNode errorMessage = createObjectNode()
        .put("message", "invalid node version: '7'");

    enqueueServerApiResponse(500, errorMessage);

    final NodeSpec nodeSpec = NodeSpec.builder()
        .addLabel("foo", "baz")
        .name("foobar")
        .availability("active")
        .role("manager")
        .build();

    dockerClient.updateNode("24ifsmvkjbyhk", 7L, nodeSpec);
  }

  @Test(expected = NodeNotFoundException.class)
  public void testUpdateMissingNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.28");
    enqueueServerApiError(404, "Error updating node: '24ifsmvkjbyhk'");

    final NodeSpec nodeSpec = NodeSpec.builder()
        .addLabel("foo", "baz")
        .name("foobar")
        .availability("active")
        .role("manager")
        .build();

    dockerClient.updateNode("24ifsmvkjbyhk", 8L, nodeSpec);
  }

  @Test(expected = NonSwarmNodeException.class)
  public void testUpdateNonSwarmNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.28");
    enqueueServerApiError(503, "Error updating node: '24ifsmvkjbyhk'");

    final NodeSpec nodeSpec = NodeSpec.builder()
        .name("foobar")
        .addLabel("foo", "baz")
        .availability("active")
        .role("manager")
        .build();

    dockerClient.updateNode("24ifsmvkjbyhk", 8L, nodeSpec);
  }

  @Test
  public void testJoinSwarm() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.24");
    enqueueServerApiEmptyResponse(200);

    SwarmJoin swarmJoin = SwarmJoin.builder()
            .joinToken("token_foo")
            .listenAddr("0.0.0.0:2377")
            .remoteAddrs(Arrays.asList("10.0.0.10:2377"))
            .build();

    dockerClient.joinSwarm(swarmJoin);
  }

  private void enqueueServerApiError(final int statusCode, final String message)
      throws IOException {
    final ObjectNode errorMessage = createObjectNode()
        .put("message", message);

    enqueueServerApiResponse(statusCode, errorMessage);
  }

  @Test
  public void testDeleteNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.24");
    enqueueServerApiEmptyResponse(200);

    dockerClient.deleteNode("node-1234");
  }

  @Test(expected = NodeNotFoundException.class)
  public void testDeleteNode_NodeNotFound() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.24");
    enqueueServerApiEmptyResponse(404);

    dockerClient.deleteNode("node-1234");
  }

  @Test(expected = NonSwarmNodeException.class)
  public void testDeleteNode_NodeNotPartOfSwarm() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.24");
    enqueueServerApiEmptyResponse(503);

    dockerClient.deleteNode("node-1234");
  }

  @Test
  public void testCreateServiceWithWarnings() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    // build() calls /version to check what format of header to send
    enqueueServerApiVersion("1.25");
    enqueueServerApiResponse(201, "fixtures/1.25/createServiceResponse.json");

    final TaskSpec taskSpec = TaskSpec.builder()
        .containerSpec(ContainerSpec.builder()
            .image("this_image_is_not_found_in_the_registry")
            .build())
        .build();

    final ServiceSpec spec = ServiceSpec.builder()
        .name("test")
        .taskTemplate(taskSpec)
        .build();

    final ServiceCreateResponse response = dockerClient.createService(spec);
    assertThat(response.id(), is(notNullValue()));
    assertThat(response.warnings(), is(hasSize(1)));
    assertThat(response.warnings(),
        contains("unable to pin image this_image_is_not_found_in_the_registry to digest"));
  }

  @Test
  public void testServiceLogs() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.25");

    server.enqueue(new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "text/plain; charset=utf-8")
            .setBody(
                fixture("fixtures/1.25/serviceLogs.txt")
            )
    );

    final LogStream stream = dockerClient.serviceLogs("serviceId", DockerClient.LogsParam.stderr());
    assertThat(stream.readFully(), is("Log Statement"));
  }

  private void enqueueServerApiEmptyResponse(final int statusCode) {
    server.enqueue(new MockResponse()
        .setResponseCode(statusCode)
        .addHeader("Content-Type", "application/json")
    );
  }

  @Test
  public void testCreateServiceWithPlacementPreference()
      throws IOException, DockerException, InterruptedException {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    final ImmutableList<Preference> prefs = ImmutableList.of(
        Preference.create(
            Spread.create(
                "test"
            )
        )
    );

    final TaskSpec taskSpec = TaskSpec.builder()
        .placement(Placement.create(null, prefs))
        .containerSpec(ContainerSpec.builder()
            .image("this_image_is_found_in_the_registry")
            .build())
        .build();

    final ServiceSpec spec = ServiceSpec.builder()
        .name("test")
        .taskTemplate(taskSpec)
        .build();


    enqueueServerApiVersion("1.30");
    enqueueServerApiResponse(201, "fixtures/1.30/createServiceResponse.json");

    final ServiceCreateResponse response = dockerClient.createService(spec);
    assertThat(response.id(), equalTo("ak7w3gjqoa3kuz8xcpnyy0pvl"));

    enqueueServerApiVersion("1.30");
    enqueueServerApiResponse(200, "fixtures/1.30/inspectCreateResponseWithPlacementPrefs.json");

    final Service service = dockerClient.inspectService("ak7w3gjqoa3kuz8xcpnyy0pvl");
    assertThat(service.spec().taskTemplate().placement(), equalTo(taskSpec.placement()));
  }

  @Test
  public void testCreateServiceWithConfig() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    // build() calls /version to check what format of header to send
    enqueueServerApiVersion("1.30");
    enqueueServerApiResponse(201, "fixtures/1.30/configCreateResponse.json");

    final ConfigSpec configSpec = ConfigSpec
        .builder()
        .data(Base64.encodeAsString("foobar"))
        .name("foo.yaml")
        .build();

    final ConfigCreateResponse configCreateResponse = dockerClient.createConfig(configSpec);
    assertThat(configCreateResponse.id(), equalTo("ktnbjxoalbkvbvedmg1urrz8h"));

    final ConfigBind configBind = ConfigBind.builder()
        .configName(configSpec.name())
        .configId(configCreateResponse.id())
        .file(ConfigFile.builder()
          .gid("1000")
          .uid("1000")
          .mode(600L)
          .name(configSpec.name())
          .build()
        ).build();

    final TaskSpec taskSpec = TaskSpec.builder()
        .containerSpec(ContainerSpec.builder()
            .image("this_image_is_found_in_the_registry")
            .configs(ImmutableList.of(configBind))
            .build())
        .build();

    final ServiceSpec spec = ServiceSpec.builder()
        .name("test")
        .taskTemplate(taskSpec)
        .build();

    enqueueServerApiVersion("1.30");
    enqueueServerApiResponse(201, "fixtures/1.30/createServiceResponse.json");

    final ServiceCreateResponse response = dockerClient.createService(spec);
    assertThat(response.id(), equalTo("ak7w3gjqoa3kuz8xcpnyy0pvl"));
  }

  @Test
  public void testListConfigs() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            fixture("fixtures/1.30/listConfigs.json")
        )
    );

    final List<Config> configs = dockerClient.listConfigs();
    assertThat(configs.size(), equalTo(1));

    final Config config = configs.get(0);

    assertThat(config, notNullValue());
    assertThat(config.id(), equalTo("ktnbjxoalbkvbvedmg1urrz8h"));
    assertThat(config.version().index(), equalTo(11L));

    final ConfigSpec configSpec = config.configSpec();
    assertThat(configSpec.name(), equalTo("server.conf"));
  }

  @Test
  public void testCreateConfig() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(201)
        .addHeader("Content-Type", "application/json")
        .setBody(
            fixture("fixtures/1.30/inspectConfig.json")
        )
    );

    final ConfigSpec configSpec = ConfigSpec
        .builder()
        .data(Base64.encodeAsString("foobar"))
        .name("foo.yaml")
        .build();

    final ConfigCreateResponse configCreateResponse = dockerClient.createConfig(configSpec);

    assertThat(configCreateResponse.id(), equalTo("ktnbjxoalbkvbvedmg1urrz8h"));
  }

  @Test(expected = ConflictException.class)
  public void testCreateConfig_ConflictingName() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(409)
        .addHeader("Content-Type", "application/json")
    );

    final ConfigSpec configSpec = ConfigSpec
        .builder()
        .data(Base64.encodeAsString("foobar"))
        .name("foo.yaml")
        .build();

    dockerClient.createConfig(configSpec);
  }

  @Test(expected = NonSwarmNodeException.class)
  public void testCreateConfig_NonSwarmNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(503)
        .addHeader("Content-Type", "application/json")
    );

    final ConfigSpec configSpec = ConfigSpec
        .builder()
        .data(Base64.encodeAsString("foobar"))
        .name("foo.yaml")
        .build();

    dockerClient.createConfig(configSpec);
  }

  @Test
  public void testInspectConfig() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            fixture("fixtures/1.30/inspectConfig.json")
        )
    );

    final Config config = dockerClient.inspectConfig("ktnbjxoalbkvbvedmg1urrz8h");

    assertThat(config, notNullValue());
    assertThat(config.id(), equalTo("ktnbjxoalbkvbvedmg1urrz8h"));
    assertThat(config.version().index(), equalTo(11L));

    final ConfigSpec configSpec = config.configSpec();
    assertThat(configSpec.name(), equalTo("app-dev.crt"));
  }

  @Test(expected = NotFoundException.class)
  public void testInspectConfig_NotFound() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(404)
        .addHeader("Content-Type", "application/json")
    );

    dockerClient.inspectConfig("ktnbjxoalbkvbvedmg1urrz8h");
  }

  @Test(expected = NonSwarmNodeException.class)
  public void testInspectConfig_NonSwarmNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(503)
        .addHeader("Content-Type", "application/json")
    );

    dockerClient.inspectConfig("ktnbjxoalbkvbvedmg1urrz8h");
  }

  @Test
  public void testDeleteConfig() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(204)
        .addHeader("Content-Type", "application/json")
    );

    dockerClient.deleteConfig("ktnbjxoalbkvbvedmg1urrz8h");
  }

  @Test(expected = NotFoundException.class)
  public void testDeleteConfig_NotFound() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(404)
        .addHeader("Content-Type", "application/json")
    );

    dockerClient.deleteConfig("ktnbjxoalbkvbvedmg1urrz8h");
  }

  @Test(expected = NonSwarmNodeException.class)
  public void testDeleteConfig_NonSwarmNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(503)
        .addHeader("Content-Type", "application/json")
    );

    dockerClient.deleteConfig("ktnbjxoalbkvbvedmg1urrz8h");
  }

  @Test(expected = NotFoundException.class)
  public void testUpdateConfig_NotFound() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(404)
        .addHeader("Content-Type", "application/json")
    );

    final ConfigSpec configSpec = ConfigSpec
        .builder()
        .data(Base64.encodeAsString("foobar"))
        .name("foo.yaml")
        .build();

    dockerClient.updateConfig("ktnbjxoalbkvbvedmg1urrz8h", 11L, configSpec);
  }

  @Test(expected = NonSwarmNodeException.class)
  public void testUpdateConfig_NonSwarmNode() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.30");

    server.enqueue(new MockResponse()
        .setResponseCode(503)
        .addHeader("Content-Type", "application/json")
    );

    final ConfigSpec configSpec = ConfigSpec
        .builder()
        .data(Base64.encodeAsString("foobar"))
        .name("foo.yaml")
        .build();

    dockerClient.updateConfig("ktnbjxoalbkvbvedmg1urrz8h", 11L, configSpec);
  }

  @Test
  public void testListNodes() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.28");

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            fixture("fixtures/1.28/listNodes.json")
        )
    );

    final List<Node> nodes = dockerClient.listNodes();
    assertThat(nodes.size(), equalTo(1));

    final Node node = nodes.get(0);

    assertThat(node, notNullValue());
    assertThat(node.id(), is("24ifsmvkjbyhk"));
    assertThat(node.version().index(), is(8L));

    final NodeSpec nodeSpec = node.spec();
    assertThat(nodeSpec.name(), is("my-node"));
    assertThat(nodeSpec.role(), is("manager"));
    assertThat(nodeSpec.availability(), is("active"));
    assertThat(nodeSpec.labels().keySet(), contains("foo"));

    final NodeDescription desc = node.description();
    assertThat(desc.hostname(), is("bf3067039e47"));
    assertThat(desc.platform().architecture(), is("x86_64"));
    assertThat(desc.platform().os(), is("linux"));
    assertThat(desc.resources().memoryBytes(), is(8272408576L));
    assertThat(desc.resources().nanoCpus(), is(4000000000L));

    final EngineConfig engine = desc.engine();
    assertThat(engine.engineVersion(), is("17.04.0"));
    assertThat(engine.labels().keySet(), contains("foo"));
    assertThat(engine.plugins().size(), equalTo(4));

    assertThat(node.status(), notNullValue());
    assertThat(node.status().addr(), is("172.17.0.2"));
    assertThat(node.managerStatus(), notNullValue());
    assertThat(node.managerStatus().addr(), is("172.17.0.2:2377"));
    assertThat(node.managerStatus().leader(), is(true));
    assertThat(node.managerStatus().reachability(), is("reachable"));
  }

  @Test(expected = DockerException.class)
  public void testListNodesWithServerError() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    enqueueServerApiVersion("1.28");

    server.enqueue(new MockResponse()
        .setResponseCode(500)
        .addHeader("Content-Type", "application/json")
    );

    dockerClient.listNodes();
  }
  
  @Test
  public void testBindBuilderSelinuxLabeling() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    final Bind bindNoSelinuxLabel = HostConfig.Bind.builder()
        .from("noselinux")
        .to("noselinux")
        .build();

    final Bind bindSharedSelinuxContent = HostConfig.Bind.builder()
        .from("shared")
        .to("shared")
        .selinuxLabeling(true)
        .build();

    final Bind bindPrivateSelinuxContent = HostConfig.Bind.builder()
        .from("private")
        .to("private")
        .selinuxLabeling(false)
        .build();

    final HostConfig hostConfig = HostConfig.builder()
        .binds(bindNoSelinuxLabel, bindSharedSelinuxContent, bindPrivateSelinuxContent)
        .build();

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .build();

    server.enqueue(new MockResponse());

    dockerClient.createContainer(containerConfig);

    final RecordedRequest recordedRequest = takeRequestImmediately();

    final JsonNode requestJson = toJson(recordedRequest.getBody());

    final JsonNode binds = requestJson.get("HostConfig").get("Binds");

    assertThat(binds.isArray(), is(true));

    Set<String> bindSet = childrenTextNodes((ArrayNode) binds);
    assertThat(bindSet, hasSize(3));

    assertThat(bindSet, hasItem(allOf(containsString("noselinux"),
        not(containsString("z")), not(containsString("Z")))));

    assertThat(bindSet, hasItem(allOf(containsString("shared"), containsString("z"))));
    assertThat(bindSet, hasItem(allOf(containsString("private"), containsString("Z"))));
  }
  
  @Test
  public void testKillContainer() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    server.enqueue(new MockResponse());

    final Signal signal = Signal.SIGHUP;
    dockerClient.killContainer("1234", signal);

    final RecordedRequest recordedRequest = takeRequestImmediately();

    final HttpUrl requestUrl = recordedRequest.getRequestUrl();
    assertThat(requestUrl.queryParameter("signal"), equalTo(signal.toString()));
  }

  @Test
  public void testInspectVolume() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            fixture("fixtures/1.33/inspectVolume.json")
        )
    );

    final Volume volume = dockerClient.inspectVolume("my-volume");

    assertThat(volume.name(), is("tardis"));
    assertThat(volume.driver(), is("custom"));
    assertThat(volume.mountpoint(), is("/var/lib/docker/volumes/tardis"));
    assertThat(volume.status(), is(ImmutableMap.of("hello", "world")));
    assertThat(volume.labels(), is(ImmutableMap.of(
        "com.example.some-label", "some-value",
        "com.example.some-other-label", "some-other-value"
    )));
    assertThat(volume.scope(), is("local"));
    assertThat(volume.options(), is(ImmutableMap.of(
        "foo", "bar",
        "baz", "qux"
    )));
  }
  
  @Test
  public void testBufferedRequestEntityProcessing() throws Exception {
    builder.useRequestEntityProcessing(RequestEntityProcessing.BUFFERED);
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);
    
    final HostConfig hostConfig = HostConfig.builder().build();

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .build();

    server.enqueue(new MockResponse());

    dockerClient.createContainer(containerConfig);

    final RecordedRequest recordedRequest = takeRequestImmediately();

    assertThat(recordedRequest.getHeader("Content-Length"), notNullValue());
    assertThat(recordedRequest.getHeader("Transfer-Encoding"), nullValue());
  }
  
  @Test
  public void testChunkedRequestEntityProcessing() throws Exception {
    builder.useRequestEntityProcessing(RequestEntityProcessing.CHUNKED);
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);
    
    final HostConfig hostConfig = HostConfig.builder().build();

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .build();

    server.enqueue(new MockResponse());

    dockerClient.createContainer(containerConfig);

    final RecordedRequest recordedRequest = takeRequestImmediately();

    assertThat(recordedRequest.getHeader("Content-Length"), nullValue());
    assertThat(recordedRequest.getHeader("Transfer-Encoding"), is("chunked"));
  }

  @Test
  public void testGetDistribution() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(builder);

    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
          fixture("fixtures/1.30/distribution.json")
        )
    );
    final Distribution distribution = dockerClient.getDistribution("my-image");
    assertThat(distribution, notNullValue());
    assertThat(distribution.platforms().size(), is(1));
    assertThat(distribution.platforms().get(0).architecture() , is("amd64"));
    assertThat(distribution.platforms().get(0).os() , is("linux"));
    assertThat(distribution.platforms().get(0).osVersion() , is(""));
    assertThat(distribution.platforms().get(0).variant() , is(""));
    assertThat(distribution.descriptor().size() , is(Long.valueOf(3987495)));
    assertThat(distribution.descriptor().digest() , is(
        "sha256:c0537ff6a5218ef531ece93d4984efc99bbf3f7497c0a7726c88e2bb7584dc96"));
    assertThat(distribution.descriptor().mediaType() , is(
        "application/vnd.docker.distribution.manifest.v2+json"
    ));
    assertThat(distribution.platforms().get(0).osFeatures() , is(ImmutableList.of(
        "feature1", "feature2"
    )));
    assertThat(distribution.platforms().get(0).features() , is(ImmutableList.of(
        "feature1", "feature2"
    )));
    assertThat(distribution.descriptor().urls() , is(ImmutableList.of(
        "url1", "url2"
    )));
  }

  private void enqueueServerApiResponse(final int statusCode, final String fileName)
      throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(statusCode)
        .addHeader("Content-Type", "application/json")
        .setBody(
            fixture(fileName)
        )
    );
  }

  private void enqueueServerApiResponse(final int statusCode, final ObjectNode objectResponse)
      throws IOException {
    server.enqueue(new MockResponse()
        .setResponseCode(statusCode)
        .addHeader("Content-Type", "application/json")
        .setBody(
            objectResponse.toString()
        )
    );
  }

  private void enqueueServerApiVersion(final String apiVersion) throws IOException {
    enqueueServerApiResponse(200,
        createObjectNode()
            .put("ApiVersion", apiVersion)
            .put("Arch", "foobar")
            .put("GitCommit", "foobar")
            .put("GoVersion", "foobar")
            .put("KernelVersion", "foobar")
            .put("Os", "foobar")
            .put("Version", "1.20")
    );
  }
}
