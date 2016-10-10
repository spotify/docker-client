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

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.spotify.docker.client.ObjectMapperProvider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ContainerStateTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ObjectMapper objectMapper;

  @Before
  public void setUp() throws Exception {
    objectMapper = new ObjectMapperProvider().getContext(ContainerState.class);
  }

  @Test
  public void testLoadFromRandomFixture() throws Exception {
    final ContainerState containerState = objectMapper
        .readValue(fixture("fixtures/container-state-random.json"), ContainerState.class);
    assertThat(containerState.paused(), is(false));
    assertThat(containerState.restarting(), is(false));
    assertThat(containerState.running(), is(true));
    assertThat(containerState.exitCode(), is(0));
    assertThat(containerState.pid(), is(27629));
    assertThat(containerState.startedAt(), is(new Date(1412236798929L)));
    assertThat(containerState.finishedAt(), is(new Date(-62135769600000L)));
    assertThat(containerState.error(), is("this is an error"));
    assertThat(containerState.oomKilled(), is(false));
    assertThat(containerState.status(), is("running"));

  }

  @Test
  public void testLoadFromRandomFixtureMissingProperty() throws Exception {
    objectMapper.readValue(fixture("fixtures/container-state-missing-property.json"),
                           ContainerState.class);
  }

  @Test
  public void testLoadInvalidConatainerStateJson() throws Exception {
    expectedException.expect(JsonMappingException.class);
    objectMapper.readValue(fixture("fixtures/container-state-invalid.json"), ContainerState.class);

  }

  @Test
  public void testLoadInvalidJson() throws Exception {
    expectedException.expect(JsonParseException.class);
    objectMapper.readValue(fixture("fixtures/invalid.json"), ContainerState.class);

  }

  private static String fixture(String filename) throws IOException {
    return Resources.toString(Resources.getResource(filename), Charsets.UTF_8).trim();
  }
}
