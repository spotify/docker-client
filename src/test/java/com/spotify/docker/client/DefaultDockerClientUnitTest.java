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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Info;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Future;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Variant;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultDockerClientUnitTest {

  @Mock
  private Client clientMock;

  @Mock
  private ClientBuilder clientBuilderMock;

  @Mock
  private Invocation.Builder builderMock;

  @Mock
  private AsyncInvoker asyncInvoker;

  @Mock
  private WebTarget webTargetMock;

  private Supplier<ClientBuilder> clientBuilderSupplier;
  private DefaultDockerClient.Builder builder;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(clientBuilderMock.build()).thenReturn(clientMock);
    when(clientBuilderMock.withConfig(any(Configuration.class))).thenReturn(clientBuilderMock);
    when(clientBuilderMock.property(anyString(), any())).thenReturn(clientBuilderMock);

    clientBuilderSupplier = Suppliers.ofInstance(clientBuilderMock);

    when(clientMock.target(any(URI.class))).thenReturn(webTargetMock);
    // return the same mock for any path.
    when(webTargetMock.path(anyString())).thenReturn(webTargetMock);

    when(webTargetMock.request(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builderMock);

    when(builderMock.async()).thenReturn(asyncInvoker);

    final Future<Info> futureMock = Futures.immediateFuture(new Info());
    when(asyncInvoker.method(anyString(), any(Class.class))).thenReturn(futureMock);

    builder = DefaultDockerClient.builder();
    builder.uri("https://perdu.com:2375");
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
  public void testHostForFQDNHttps() {
    final DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://perdu.com:2375").build();
    assertThat(client.getHost(), equalTo("perdu.com"));
  }

  @Test
  public void testHostForIPHttps() {
    final DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://192.168.53.103:2375").build();
    assertThat(client.getHost(), equalTo("192.168.53.103"));
  }

  @Test
  public void testNoHeaders() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(
        builder, clientBuilderSupplier);
    dockerClient.info();

    verify(builderMock, never()).header(anyString(), anyString());
  }

  @Test
  public void testOneHeader() throws Exception {
    builder.header("foo", 1);

    final DefaultDockerClient dockerClient = new DefaultDockerClient(
        builder, clientBuilderSupplier);
    dockerClient.info();

    final ArgumentCaptor<String> keyArgument = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<Object> valueArgument = ArgumentCaptor.forClass(Object.class);
    verify(builderMock, times(1)).header(keyArgument.capture(), valueArgument.capture());

    Assert.assertEquals("foo", keyArgument.getValue());
    Assert.assertEquals(1, valueArgument.getValue());
  }

  @Test
  public void testMultipleHeaders() throws Exception {
    final Map<String, Object> headers = Maps.newHashMap();
    headers.put("int", 1);
    headers.put("string", "2");
    headers.put("list", Lists.newArrayList("a", "b", "c"));

    for (final Map.Entry<String, Object> entry : headers.entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }

    final DefaultDockerClient dockerClient = new DefaultDockerClient(
        builder, clientBuilderSupplier);
    dockerClient.info();

    final ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(builderMock, times(headers.size())).header(nameCaptor.capture(), valueCaptor.capture());

    int i = 0;
    for (final Map.Entry<String, Object> entry : headers.entrySet()) {
      Assert.assertEquals(entry.getKey(), nameCaptor.getAllValues().get(i));
      Assert.assertEquals(entry.getValue(), valueCaptor.getAllValues().get(i));
      ++i;
    }
  }

  @Test
  public void testCapAddAndDrop() throws Exception {
    final DefaultDockerClient dockerClient = new DefaultDockerClient(
        builder, clientBuilderSupplier);

    final HostConfig hostConfig = HostConfig.builder()
        .capAdd(ImmutableList.of("foo", "bar"))
        .capAdd(ImmutableList.of("baz", "qux"))
        .build();

    final ContainerConfig containerConfig = ContainerConfig.builder()
        .hostConfig(hostConfig)
        .build();

    //noinspection unchecked
    when(asyncInvoker.method(
        anyString(), any(Entity.class), any(Class.class)))
        .thenReturn(Futures.immediateFuture(new ContainerCreation()));

    dockerClient.createContainer(containerConfig);

    final ArgumentCaptor<String> methodArg = ArgumentCaptor.forClass(String.class);
    final ArgumentCaptor<Entity> entityArg = ArgumentCaptor.forClass(Entity.class);
    final ArgumentCaptor<Class> classArg = ArgumentCaptor.forClass(Class.class);

    //noinspection unchecked
    verify(asyncInvoker, times(1)).method(
        methodArg.capture(), entityArg.capture(), classArg.capture());

    final Entity expectedEntity = Entity.entity(
        containerConfig, new Variant(MediaType.valueOf(APPLICATION_JSON), (String) null, null));

    // Check that we've called the right method on the underlying AsyncInvoker with the right params
    assertThat(methodArg.getValue(), equalTo("POST"));
    assertThat(entityArg.getValue(), equalTo(expectedEntity));
    assertThat(classArg.getValue(), instanceOf(Class.class));
  }
}
