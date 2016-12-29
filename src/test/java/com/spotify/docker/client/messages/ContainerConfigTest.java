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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ContainerConfigTest {

  @Rule
  public final ExpectedException expectedException = ExpectedException.none();

  @Test
  public void testNoVolumes() throws Exception {
    final ContainerConfig containerConfig = ContainerConfig.builder().build();
    assertThat(containerConfig.volumes(), is(nullValue()));

    final ContainerConfig.Builder builder = containerConfig.toBuilder();
    assertThat(builder.volumes(), is(nullValue()));
  }

}