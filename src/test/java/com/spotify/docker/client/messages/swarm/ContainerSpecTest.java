/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 - 2017 Spotify AB
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

package com.spotify.docker.client.messages.swarm;

import static com.spotify.docker.FixtureUtil.fixture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.ObjectMapperProvider;
import org.junit.Test;

public class ContainerSpecTest {

  private ObjectMapper objectMapper = ObjectMapperProvider.objectMapper();

  @Test
  public void test1_32_WithoutNullables() throws Exception {
    final ContainerSpec spec = objectMapper.readValue(fixture(
        "fixtures/1.32/containerSpecWithoutNullables.json"), ContainerSpec.class);
    assertThat(spec.labels().size(), equalTo(0));
    assertThat(spec.command(), is(nullValue()));
    assertThat(spec.secrets(), is(nullValue()));
    assertThat(spec.configs(), is(nullValue()));
  }

}