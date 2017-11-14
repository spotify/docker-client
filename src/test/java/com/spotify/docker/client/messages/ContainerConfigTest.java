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

package com.spotify.docker.client.messages;

import static com.spotify.docker.FixtureUtil.fixture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.ObjectMapperProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ContainerConfigTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ObjectMapper objectMapper = ObjectMapperProvider.objectMapper();

  @Test
  public void test1_28() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.28/containerConfig.json"), ContainerConfig.class);
  }

  @Test
  public void test1_29() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.29/containerConfig.json"), ContainerConfig.class);
  }

  @Test
  public void test1_29_WithoutNullables() throws Exception {
    final ContainerConfig config = objectMapper.readValue(
        fixture("fixtures/1.29/containerConfigWithoutNullables.json"), ContainerConfig.class);
    assertThat(config.portSpecs(), is(nullValue()));
    assertThat(config.exposedPorts(), is(nullValue()));
    assertThat(config.env(), is(nullValue()));
    assertThat(config.cmd(), is(nullValue()));
    assertThat(config.entrypoint(), is(nullValue()));
    assertThat(config.onBuild(), is(nullValue()));
    assertThat(config.labels(), is(nullValue()));
    assertThat(config.healthcheck(), is(nullValue()));
  }

}