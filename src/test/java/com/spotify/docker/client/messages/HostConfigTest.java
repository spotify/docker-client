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

package com.spotify.docker.client.messages;

import com.spotify.docker.client.ObjectMapperProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class HostConfigTest {

  private ObjectMapper objectMapper;

  @Before
  public void setUp() throws Exception {
    objectMapper = new ObjectMapperProvider().getContext(HostConfig.class);
  }

  @Test
  public void testJsonAlways() throws Exception {
    final HostConfig hostConfig = objectMapper
        .readValue(fixture("fixtures/hostConfig/restartPolicyAlways.json"),
                   HostConfig.class);
    assertThat(hostConfig.restartPolicy(), is(HostConfig.RestartPolicy.always()));
  }

  @Test
  public void testJsonUnlessStopped() throws Exception {
    final HostConfig hostConfig = objectMapper
        .readValue(fixture("fixtures/hostConfig/restartPolicyUnlessStopped.json"),
                   HostConfig.class);
    assertThat(hostConfig.restartPolicy(), is(HostConfig.RestartPolicy.unlessStopped()));
  }

  @Test
  public void testJsonOnFailure() throws Exception {
    final HostConfig hostConfig = objectMapper
        .readValue(fixture("fixtures/hostConfig/restartPolicyOnFailure.json"),
                   HostConfig.class);
    assertThat(hostConfig.restartPolicy(), is(HostConfig.RestartPolicy.onFailure(5)));
  }

  private static String fixture(String filename) throws IOException {
    return Resources.toString(Resources.getResource(filename), Charsets.UTF_8).trim();
  }

  @Test
  public void testReplaceBinds() {
    final List<String> initialBinds = ImmutableList.of("/one:/one", "/two:/two");
    final HostConfig hostConfig = HostConfig.builder()
        .binds(initialBinds)
        .binds(initialBinds)
        .build();

    assertThat("Calling .binds() multiple times should replace the list each time",
               hostConfig.binds(), is(initialBinds));
  }

  @Test
  public void testAppendBinds() {
    final List<String> initialBinds = ImmutableList.of("/one:/one", "/two:/two");
    final HostConfig hostConfig = HostConfig.builder()
        .binds(initialBinds)
        .appendBinds("/three:/three")
        .appendBinds("/four:/four")
        .build();

    final List<String> expected = ImmutableList.<String>builder()
        .addAll(initialBinds)
        .add("/three:/three")
        .add("/four:/four")
        .build();

    assertThat("Calling .appendBinds should append to the list, not replace",
               hostConfig.binds(), is(expected));
  }

  @Test
  public void testPreventDuplicateBinds() {
    final HostConfig hostConfig = HostConfig.builder()
        .appendBinds("/one:/one")
        .appendBinds("/one:/one")
        .appendBinds("/one:/one")
        .build();

    assertThat(hostConfig.binds(), contains("/one:/one"));
  }
}
