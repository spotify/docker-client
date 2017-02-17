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

package com.spotify.docker.client.messages;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.ObjectMapperProvider;

import java.util.Date;

import org.junit.Test;

public class EventTest {

  @Test
  public void serializationRoundTripTest() throws Exception {
    // Test serializing and deserializing the same Event instance works and preserves data
    final Event event = Event.create("create", "foo", "nginx", Event.Type.CONTAINER, "create",
        Event.Actor.create("bar", ImmutableMap.of("image", "nginx", "name", "docker-nginx")),
        new Date(1487356000), 100L);

    final ObjectMapper mapper = ObjectMapperProvider.objectMapper();

    final String json = mapper.writeValueAsString(event);

    final Event event2 = mapper.readValue(json, Event.class);
    assertThat(event, equalTo(event2));
  }
}