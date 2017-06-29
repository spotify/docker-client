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

import static com.spotify.docker.client.ImageRef.parseRegistryUrl;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;
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
    assertThat(new ImageRef("index.docker.io/library/ubuntu"), hasRegistry("index.docker.io"));
    assertThat(new ImageRef("quay.io/library/ubuntu"), hasRegistry("quay.io"));
    assertThat(new ImageRef("gcr.io/library/ubuntu"), hasRegistry("gcr.io"));
    assertThat(new ImageRef("us.gcr.io/library/ubuntu"), hasRegistry("us.gcr.io"));
    assertThat(new ImageRef("gcr.kubernetes.io/library/ubuntu"),
        hasRegistry("gcr.kubernetes.io"));

    assertThat(new ImageRef("registry.example.net/foo/bar"),
        hasRegistry("registry.example.net"));

    assertThat(new ImageRef("registry.example.net/foo/bar:1.2.3"),
        hasRegistry("registry.example.net"));

    assertThat(new ImageRef("registry.example.net/foo/bar:latest"),
        hasRegistry("registry.example.net"));

    assertThat(new ImageRef("registry.example.net:5555/foo/bar:latest"),
        hasRegistry("registry.example.net:5555"));
  }

  @Test
  public void testRegistryUrl() throws Exception {
    final String defaultRegistry = "https://index.docker.io/v1/";
    assertThat(new ImageRef("ubuntu"), hasRegistryUrl(defaultRegistry));
    assertThat(new ImageRef("library/ubuntu"), hasRegistryUrl(defaultRegistry));
    assertThat(new ImageRef("docker.io/library/ubuntu"), hasRegistryUrl(defaultRegistry));
    assertThat(new ImageRef("index.docker.io/library/ubuntu"), hasRegistryUrl(defaultRegistry));
    assertThat(new ImageRef("quay.io/library/ubuntu"), hasRegistryUrl("quay.io"));
    assertThat(new ImageRef("gcr.io/library/ubuntu"), hasRegistryUrl("https://gcr.io"));
    assertThat(new ImageRef("us.gcr.io/library/ubuntu"), hasRegistryUrl("https://us.gcr.io"));
    assertThat(new ImageRef("gcr.kubernetes.io/library/ubuntu"),
        hasRegistryUrl("https://gcr.kubernetes.io"));

    assertThat(new ImageRef("registry.example.net/foo/bar"),
        hasRegistryUrl("https://registry.example.net"));

    assertThat(new ImageRef("registry.example.net/foo/bar:1.2.3"),
        hasRegistryUrl("https://registry.example.net"));

    assertThat(new ImageRef("registry.example.net/foo/bar:latest"),
        hasRegistryUrl("https://registry.example.net"));

    assertThat(new ImageRef("registry.example.net:5555/foo/bar:latest"),
        hasRegistryUrl("https://registry.example.net:5555"));
  }

  @Test
  public void testParseUrl() throws Exception {
    assertThat(parseRegistryUrl("docker.io"), equalTo("https://index.docker.io/v1/"));
    assertThat(parseRegistryUrl("index.docker.io"), equalTo("https://index.docker.io/v1/"));
    assertThat(parseRegistryUrl("registry.net"), equalTo("https://registry.net"));
    assertThat(parseRegistryUrl("registry.net:80"), equalTo("https://registry.net:80"));
  }

  private static Matcher<ImageRef> hasRegistry(final String expected) {
    return new FeatureMatcher<ImageRef, String>(equalTo(expected), "registryName", "registryName") {
      @Override
      protected String featureValueOf(final ImageRef actual) {
        return actual.getRegistryName();
      }
    };
  }

  private static Matcher<ImageRef> hasRegistryUrl(final String expected) {
    return new FeatureMatcher<ImageRef, String>(equalTo(expected),
        "registryNameUrl", "registryNameUrl") {
      @Override
      protected String featureValueOf(final ImageRef actual) {
        return actual.getRegistryUrl();
      }
    };
  }
}
