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

package com.spotify.docker.client;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

public class ImageRefTest {

  @Test
  public void testImageWithoutTag() {
    final ImageRef sut = new ImageRef("foobar");
    assertThat(sut.getImage(), equalTo("foobar"));
    assertThat(sut.getTag(), is(nullValue()));
  }

  @Test
  public void testImageWithTag() {
    final ImageRef sut = new ImageRef("foobar:12345");
    assertThat(sut.getImage(), equalTo("foobar"));
    assertThat(sut.getTag(), is("12345"));
  }

  @Test
  public void testImageWithTagAndRegistry() {
    final ImageRef sut = new ImageRef("registry:4711/foo/bar:12345");
    assertThat(sut.getImage(), equalTo("registry:4711/foo/bar"));
    assertThat(sut.getTag(), is("12345"));
  }

  @Test
  public void testImageWithDigest() {
    final ImageRef sut = new ImageRef("bar@sha256:12345");
    assertThat(sut.getImage(), equalTo("bar@sha256:12345"));
  }

  @Test
  public void testImageWithDigestAndRegistry() {
    final ImageRef sut = new ImageRef("registry:4711/foo/bar@sha256:12345");
    assertThat(sut.getImage(), equalTo("registry:4711/foo/bar@sha256:12345"));
  }

  @Test
  public void testRegistry() {
    final String defaultRegistry = "docker.io";
    assertThat(new ImageRef("ubuntu"), hasRegistry(defaultRegistry));
    assertThat(new ImageRef("library/ubuntu"), hasRegistry(defaultRegistry));
    assertThat(new ImageRef("docker.io/library/ubuntu"), hasRegistry(defaultRegistry));

    assertThat(new ImageRef("registry.example.net/foo/bar"),
        hasRegistry("registry.example.net"));

    assertThat(new ImageRef("registry.example.net/foo/bar:1.2.3"),
        hasRegistry("registry.example.net"));

    assertThat(new ImageRef("registry.example.net/foo/bar:latest"),
        hasRegistry("registry.example.net"));

    assertThat(new ImageRef("registry.example.net:5555/foo/bar:latest"),
        hasRegistry("registry.example.net:5555"));
  }

  private static Matcher<ImageRef> hasRegistry(final String expected) {
    return new FeatureMatcher<ImageRef, String>(equalTo(expected), "registryName", "registryName") {
      @Override
      protected String featureValueOf(final ImageRef actual) {
        return actual.getRegistryName();
      }
    };
  }

}
