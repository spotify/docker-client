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

  @Test
  public void testDigest() throws IOException {
    final String digest = "sha256:ebd39c3e3962f804787f6b0520f8f1e35fbd5a01ab778ac14c8d6c37978e8445";
    final ObjectMapper objectMapper = new ObjectMapperProvider().getContext(ProgressMessage.class);

    assertEquals(
        digest,
        objectMapper.readValue("{\"status\":\"Digest: " + digest + "\"}", ProgressMessage.class)
            .digest());

    assertNull(
        objectMapper.readValue("{\"status\":\"not-a-digest\"}", ProgressMessage.class)
            .digest());
  }
}
