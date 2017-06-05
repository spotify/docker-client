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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
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
    server.enqueue(new MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody(
            createObjectNode()
                .put("ApiVersion", "1.20")
                .put("Arch", "foobar")
                .put("GitCommit", "foobar")
                .put("GoVersion", "foobar")
                .put("KernelVersion", "foobar")
                .put("Os", "foobar")
                .put("Version", "1.20")
                .toString()
        )
    );

    // TODO (mbrown): what to return for build response?
    server.enqueue(new MockResponse()
        .setResponseCode(200)
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
}
