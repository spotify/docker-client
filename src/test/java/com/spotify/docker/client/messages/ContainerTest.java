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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.ObjectMapperProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ContainerTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ObjectMapper objectMapper = ObjectMapperProvider.objectMapper();

  @Test
  public void testLoadFromFixture() throws Exception {
    final Container container = objectMapper
        .readValue(fixture("fixtures/container-ports-as-string.json"), Container.class);
    assertThat(container.portsAsString(), is("0.0.0.0:80->88/tcp"));
  }


  @Test
  public void testLoadFromFixtureMissingPorts() throws Exception {
    final Container container = objectMapper
            .readValue(fixture("fixtures/container-no-ports-or-names.json"), Container.class);
    assertThat(container.id(), is("1009"));
  }

}
