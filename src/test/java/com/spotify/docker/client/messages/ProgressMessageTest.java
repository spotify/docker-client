/*
 * Copyright (c) 2015 Spotify AB.
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

package com.spotify.docker.client.messages;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.spotify.docker.client.ObjectMapperProvider;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProgressMessageTest {

  private final String digest =
      "sha256:ebd39c3e3962f804787f6b0520f8f1e35fbd5a01ab778ac14c8d6c37978e8445";

  private ProgressMessage readMessage(String status) throws IOException {
    final ObjectMapper objectMapper = new ObjectMapperProvider().getContext(ProgressMessage.class);
    final String line = objectMapper.createObjectNode()
        .put("status", status)
        .toString();
    return objectMapper.readValue(line, ProgressMessage.class);
  }

  @Test
  public void testNotADigest() throws Exception {
    assertNull(readMessage("not-a-digest").digest());
  }

  @Test
  public void testDigest_Docker16() throws Exception {
    assertEquals(digest, readMessage("Digest: " + digest).digest());
  }

  @Test
  public void testDigest_Docker18() throws Exception {
    final String status = "some-image-tag: digest: " + digest + " size: 1234";
    assertEquals(digest, readMessage(status).digest());
  }
}
