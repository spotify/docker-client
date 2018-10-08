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

import com.spotify.docker.client.messages.DockerCredentialHelperAuth;
import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.Test;

public class SystemCredentialHelperDelegateTest {

  @Test
  public void testReadServerAuthDetails() throws Exception {
    final StringReader input = new StringReader(
        "{\"Username\": \"foo\", \"Secret\": \"bar\", \"ServerURL\": \"example.com\"}");
    final DockerCredentialHelperAuth auth = readServerAuthDetails(new BufferedReader(input));
    assertThat(auth, is(pojo(DockerCredentialHelperAuth.class)
        .where("username", is("foo"))
        .where("secret", is("bar"))
        .where("serverUrl", is("example.com"))
    ));
  }

  @Test
  public void readServerAuthDetailsFromMultipleLines() throws Exception {
    final StringReader input = new StringReader(
        "{\n"
        + "  \"Username\": \"foo\",\n"
        + "  \"Secret\": \"bar\",\n"
        + "  \"ServerURL\": \"example.com\""
        + "\n}");
    final DockerCredentialHelperAuth auth = readServerAuthDetails(new BufferedReader(input));
    assertThat(auth, is(pojo(DockerCredentialHelperAuth.class)
        .where("username", is("foo"))
        .where("secret", is("bar"))
        .where("serverUrl", is("example.com"))
    ));
  }

  @Test
  public void readServerAuthDetailsNoServerUrl() throws Exception {
    final StringReader input = new StringReader(
        "{\n"
        + "  \"Username\": \"foo\",\n"
        + "  \"Secret\": \"bar\"\n"
        + "\n}");
    final DockerCredentialHelperAuth auth = readServerAuthDetails(new BufferedReader(input));
    assertThat(auth, is(pojo(DockerCredentialHelperAuth.class)
        .where("username", is("foo"))
        .where("secret", is("bar"))
        .where("serverUrl", is(nullValue()))
    ));
  }
}
