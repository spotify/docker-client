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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DockerConfigReader;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ConfigFileRegistryAuthSupplierTest {

  private final DockerConfigReader reader = mock(DockerConfigReader.class);

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private File configFile;

  private ConfigFileRegistryAuthSupplier supplier;

  @Before
  public void setUp() throws Exception {
    configFile = tempFolder.newFile();

    supplier = new ConfigFileRegistryAuthSupplier(reader, configFile.toPath());
  }

  @Test
  public void testAuthFor_ConfigFileDoesNotExist() throws Exception {
    assertTrue("unable to delete file", configFile.delete());

    // sanity check
    assertFalse("cannot continue this test if the file " + configFile + " actually exists",
        configFile.exists());

    assertThat(supplier.authFor("foo.example.net/bar:1.2.3"), is(nullValue()));
  }

  @Test
  public void testAuthFor_ConfigFileEmptyFile() throws Exception {
    assertEquals("file is not empty", 0, Files.readAllBytes(configFile.toPath()).length);

    assertThat(supplier.authFor("foo.example.net/bar:1.2.3"), is(nullValue()));
  }

  @Test
  public void testAuthFor_Success() throws Exception {
    final RegistryAuth auth = RegistryAuth.builder()
        .username("abc123")
        .build();

    when(reader.authForRegistry(configFile.toPath(), "foo.example.net")).thenReturn(auth);

    assertThat(supplier.authFor("foo.example.net/bar:1.2.3"), is(equalTo(auth)));
  }

  @Test
  public void testAuthForSwarm_Unimplemented() throws Exception {
    assertThat(supplier.authForSwarm(), is(nullValue()));

    // force future implementors of this method to write a test
    verify(reader, never()).authForAllRegistries(any(Path.class));
  }

  @Test
  public void testAuthForBuild_ConfigFileDoesNotExist() throws Exception {
    assertTrue("unable to delete file", configFile.delete());

    // sanity check
    assertFalse("cannot continue this test if the file " + configFile + " actually exists",
        configFile.exists());

    assertThat(supplier.authForBuild(), is(nullValue()));
  }

  @Test
  public void testAuthForBuild_ConfigFileEmptyFile() throws Exception {
    assertEquals("file is not empty", 0, Files.readAllBytes(configFile.toPath()).length);

    assertThat(supplier.authForBuild(), is(nullValue()));
  }

  @Test
  public void testAuthForBuild_Success() throws Exception {
    final RegistryConfigs configs = RegistryConfigs.create(ImmutableMap.of(
        "server1",
        RegistryAuth.builder()
            .serverAddress("server1")
            .username("user1")
            .password("pass1")
            .build()
    ));

    when(reader.authForAllRegistries(configFile.toPath())).thenReturn(configs);

    assertThat(supplier.authForBuild(), is(equalTo(configs)));
  }
}
