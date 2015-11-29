/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.docker.client;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

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

}
