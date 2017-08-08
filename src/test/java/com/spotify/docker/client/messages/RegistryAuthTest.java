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

import static com.spotify.docker.FixtureUtil.fixture;
import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.spotify.docker.client.ObjectMapperProvider;
import org.junit.Test;

public class RegistryAuthTest {

  private static final ObjectMapper objectMapper = ObjectMapperProvider.objectMapper();

  @Test
  public void testDeserializingFromJson() throws Exception {
    final RegistryAuth registryAuth =
        objectMapper.readValue(fixture("fixtures/registryAuth.json"), RegistryAuth.class);
    assertThat(registryAuth, is(
        pojo(RegistryAuth.class)
            .where("username", is("hannibal"))
            .where("password", is("xxxx"))
            .where("email", is("hannibal@a-team.com"))
            .where("serverAddress", is("https://index.docker.io/v1/"))
            .where("identityToken", is("foobar"))
    ));
  }

  @Test
  public void testForAuth() {
    final String username = "johndoe";
    final String password = "pass123";
    final String encoded = BaseEncoding.base64().encode((username + ":" + password).getBytes());

    final RegistryAuth registryAuth = RegistryAuth.forAuth(encoded).build();
    assertThat(registryAuth, is(
        pojo(RegistryAuth.class)
            .where("username", is(username))
            .where("password", is(password))
    ));
  }

  @Test
  public void testForAuth_PasswordContainsColon() {
    final String username = "johndoe";
    final String password = "foo:bar";
    final String encoded = BaseEncoding.base64().encode((username + ":" + password).getBytes());

    final RegistryAuth registryAuth = RegistryAuth.forAuth(encoded).build();
    assertThat(registryAuth, is(
        pojo(RegistryAuth.class)
            .where("username", is(username))
            .where("password", is(password))
    ));
  }
}
