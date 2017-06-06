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

package com.spotify.docker.client.auth;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import org.junit.Test;

public class MultiRegistryAuthSupplierTest {

  private final RegistryAuthSupplier supplier1 = mock(RegistryAuthSupplier.class);
  private final RegistryAuthSupplier supplier2 = mock(RegistryAuthSupplier.class);

  private final RegistryAuthSupplier multiSupplier =
      new MultiRegistryAuthSupplier(ImmutableList.of(supplier1, supplier2));

  @Test
  public void testAuthFor() throws Exception {
    final String image1 = "foobar:latest";
    final RegistryAuth auth1 = RegistryAuth.builder().build();
    when(supplier1.authFor(image1)).thenReturn(auth1);

    assertThat(multiSupplier.authFor(image1), is(auth1));
    verify(supplier2, never()).authFor(image1);

    // test fallback
    final String image2 = "bizbat:1.2.3";
    final RegistryAuth auth2 = RegistryAuth.builder()
        .email("foo@biz.com")
        .build();
    when(supplier1.authFor(image2)).thenReturn(null);
    when(supplier2.authFor(image2)).thenReturn(auth2);

    assertThat(multiSupplier.authFor(image2), is(auth2));
  }

  @Test
  public void testAuthForSwarm() throws Exception {
    final RegistryAuth auth1 = RegistryAuth.builder()
        .email("a@b.com")
        .build();
    when(supplier1.authForSwarm()).thenReturn(auth1);

    assertThat(multiSupplier.authForSwarm(), is(auth1));
    verify(supplier2, never()).authForSwarm();

    // test fallback
    final RegistryAuth auth2 = RegistryAuth.builder()
        .email("foo@biz.com")
        .build();
    when(supplier1.authForSwarm()).thenReturn(null);
    when(supplier2.authForSwarm()).thenReturn(auth2);

    assertThat(multiSupplier.authForSwarm(), is(auth2));
  }

  @Test
  public void testAuthForBuild() throws Exception {

    final RegistryAuth auth1 = RegistryAuth.builder()
        .username("1")
        .serverAddress("a")
        .build();

    final RegistryAuth auth2 = RegistryAuth.builder()
        .username("2")
        .serverAddress("b")
        .build();

    final RegistryAuth auth3 = RegistryAuth.builder()
        .username("3")
        .serverAddress("b")
        .build();

    final RegistryAuth auth4 = RegistryAuth.builder()
        .username("4")
        .serverAddress("c")
        .build();

    when(supplier1.authForBuild()).thenReturn(RegistryConfigs.create(ImmutableMap.of(
        "a", auth1,
        "b", auth2
    )));

    when(supplier2.authForBuild()).thenReturn(RegistryConfigs.create(ImmutableMap.of(
        "b", auth3,
        "c", auth4
    )));

    // ensure that supplier1 had priority for server b
    assertThat(multiSupplier.authForBuild().configs(), allOf(
        hasEntry("a", auth1),
        hasEntry("b", auth2),
        hasEntry("c", auth4)
    ));
  }

  /**
   * Test what happens if one of the Suppliers returns null for authForBuild().
   */
  @Test
  public void testAuthForBuild_ReturnsNull() throws Exception {

    when(supplier1.authForBuild()).thenReturn(null);

    final RegistryConfigs registryConfigs = RegistryConfigs.create(ImmutableMap.of(
        "a",
        RegistryAuth.builder()
            .username("1")
            .serverAddress("a")
            .build(),
        "b",
        RegistryAuth.builder()
            .username("2")
            .serverAddress("b")
            .build()
    ));
    when(supplier2.authForBuild()).thenReturn(registryConfigs);

    assertThat(multiSupplier.authForBuild(), is(registryConfigs));
  }
}
