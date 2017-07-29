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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.ObjectMapperProvider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class InfoTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ObjectMapper objectMapper = ObjectMapperProvider.objectMapper();

  @Test
  public void test1_24_SwarmInactive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.24/infoSwarmInactive.json"), Info.class);
  }

  @Test
  public void test1_24_SwarmActive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.24/infoSwarmActive.json"), Info.class);
  }

  @Test
  public void test1_25_SwarmInactive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.25/infoSwarmInactive.json"), Info.class);
  }

  @Test
  public void test1_25_SwarmActive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.25/infoSwarmActive.json"), Info.class);
  }

  @Test
  public void test1_26_SwarmInactive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.26/infoSwarmInactive.json"), Info.class);
  }

  @Test
  public void test1_26_SwarmActive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.26/infoSwarmActive.json"), Info.class);
  }

  @Test
  public void test1_27_SwarmInactive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.27/infoSwarmInactive.json"), Info.class);
  }

  @Test
  public void test1_27_SwarmActive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.27/infoSwarmActive.json"), Info.class);
  }

  @Test
  public void test1_28_SwarmActive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.28/infoSwarmActive.json"), Info.class);
  }

  @Test
  public void test1_29_SwarmInactive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.29/infoSwarmInactive.json"), Info.class);
  }

  @Test
  public void test1_29_SwarmActive() throws Exception {
    objectMapper.readValue(fixture("fixtures/1.29/infoSwarmActive.json"), Info.class);
  }

}