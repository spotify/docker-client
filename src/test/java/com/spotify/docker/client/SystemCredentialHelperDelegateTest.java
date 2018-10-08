/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 - 2018 Spotify AB
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

package com.spotify.docker.client;

import static com.spotify.docker.client.SystemCredentialHelperDelegate.readServerAuthDetails;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spotify.docker.client.messages.DockerCredentialHelperAuth;
import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.Before;
import org.junit.Test;

public class SystemCredentialHelperDelegateTest {

  private ObjectMapper objectMapper;

  @Before
  public void setup() {
    objectMapper = ObjectMapperProvider.objectMapper();
  }

  @Test
  public void testReadServerAuthDetails() throws Exception {
    final ObjectNode node = objectMapper.createObjectNode()
        .put("Username", "foo")
        .put("Secret", "bar")
        .put("ServerURL", "example.com");
    final StringReader input = new StringReader(objectMapper.writeValueAsString(node));
    final DockerCredentialHelperAuth auth = readServerAuthDetails(new BufferedReader(input));
    assertThat(auth, is(pojo(DockerCredentialHelperAuth.class)
        .where("username", is("foo"))
        .where("secret", is("bar"))
        .where("serverUrl", is("example.com"))
    ));
  }

  @Test
  public void readServerAuthDetailsFromMultipleLines() throws Exception {
    final ObjectNode node = objectMapper.createObjectNode()
        .put("Username", "foo")
        .put("Secret", "bar")
        .put("ServerURL", "example.com");
    final StringReader input =
        new StringReader(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
    final DockerCredentialHelperAuth auth = readServerAuthDetails(new BufferedReader(input));
    assertThat(auth, is(pojo(DockerCredentialHelperAuth.class)
        .where("username", is("foo"))
        .where("secret", is("bar"))
        .where("serverUrl", is("example.com"))
    ));
  }

  @Test
  public void readServerAuthDetailsNoServerUrl() throws Exception {
    final ObjectNode node = objectMapper.createObjectNode()
        .put("Username", "foo")
        .put("Secret", "bar");
    final StringReader input =
        new StringReader(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
    final DockerCredentialHelperAuth auth = readServerAuthDetails(new BufferedReader(input));
    assertThat(auth, is(pojo(DockerCredentialHelperAuth.class)
        .where("username", is("foo"))
        .where("secret", is("bar"))
        .where("serverUrl", is(nullValue()))
    ));
  }
}
