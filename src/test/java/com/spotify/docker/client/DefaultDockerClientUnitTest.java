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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.spotify.docker.client.messages.Info;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Future;

public class DefaultDockerClientUnitTest {

  @Mock
  Client clientMock;

  @Mock
  ClientBuilder clientBuilderMock;

  @Mock
  RSClientBuilderWrapper rsClientBuilderWrapperMock;

  @Mock
  Invocation.Builder builderMock;

  @Mock
  AsyncInvoker asyncInvoker;

  @Mock
  Future futureMock;

  @Mock
  WebTarget webTargetMock;

  DefaultDockerClient.Builder builder;

  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(clientBuilderMock.build()).thenReturn(clientMock);
    when(clientBuilderMock.withConfig(any(Configuration.class))).thenReturn(clientBuilderMock);
    when(clientBuilderMock.property(anyString(), any())).thenReturn(clientBuilderMock);

    when(rsClientBuilderWrapperMock.newBuilder()).thenReturn(clientBuilderMock);

    when(clientMock.target(any(URI.class))).thenReturn(webTargetMock);
    // return the same mock for any path.
    when(webTargetMock.path(anyString())).thenReturn(webTargetMock);

    when(webTargetMock.request(MediaType.APPLICATION_JSON_TYPE)).thenReturn(builderMock);

    when(builderMock.async()).thenReturn(asyncInvoker);

    when(asyncInvoker.method(anyString(), any(Class.class))).thenReturn(futureMock);

    Info info = new Info();
    when(futureMock.get()).thenReturn(info);

    builder = DefaultDockerClient.builder();
    builder.uri("https://perdu.com:2375");
  }

  @Test
  public void testHostForUnixSocket() {
    DefaultDockerClient client = DefaultDockerClient.builder()
        .uri(DefaultDockerClient.DEFAULT_UNIX_ENDPOINT).build();
    assertThat(client.getHost(), equalTo("localhost"));
  }
  
  @Test
  public void testHostForLocalHttps() {
    DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://localhost:2375").build();
    assertThat(client.getHost(), equalTo("localhost"));
  }
  
  @Test
  public void testHostForFQDNHttps() {
    DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://perdu.com:2375").build();
    assertThat(client.getHost(), equalTo("perdu.com"));
  }
  
  @Test
  public void testHostForIPHttps() {
    DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://192.168.53.103:2375").build();
    assertThat(client.getHost(), equalTo("192.168.53.103"));
  }

  @Test
  public void testNoHeaders() throws Exception {
    DefaultDockerClient dockerClient = new DefaultDockerClient(builder, rsClientBuilderWrapperMock);
    dockerClient.info();

    verify(builderMock, never()).header(anyString(), anyString());
  }

  @Test
  public void testOneHeader() throws Exception {

    builder.header("foo", 1);

    DefaultDockerClient dockerClient = new DefaultDockerClient(builder, rsClientBuilderWrapperMock);
    dockerClient.info();

    ArgumentCaptor<String> keyArgument = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> valueArgument = ArgumentCaptor.forClass(String.class);
    verify(builderMock, times(1)).header(keyArgument.capture(), valueArgument.capture());

    Assert.assertEquals("foo", keyArgument.getValue());
    Assert.assertEquals(1, valueArgument.getValue());
  }

  @Test
  public void testMultipleHeaders() throws Exception {
    Map<String, Object> headers = Maps.newHashMap();
    headers.put("int", 1);
    headers.put("string", "2");
    headers.put("list", Lists.newArrayList("a", "b", "c"));

    for (Map.Entry<String, Object> entry : headers.entrySet()) {
      builder.header(entry.getKey(), entry.getValue());
    }

    DefaultDockerClient dockerClient = new DefaultDockerClient(builder, rsClientBuilderWrapperMock);
    dockerClient.info();

    ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
    verify(builderMock, times(headers.size())).header(nameCaptor.capture(), valueCaptor.capture());

    int i = 0;
    for (Map.Entry<String, Object> entry : headers.entrySet()) {
      Assert.assertEquals(entry.getKey(), nameCaptor.getAllValues().get(i));
      Assert.assertEquals(entry.getValue(), valueCaptor.getAllValues().get(i));
      ++i;
    }
  }
}
