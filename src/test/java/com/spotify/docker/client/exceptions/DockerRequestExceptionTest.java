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

/*
 * Copyright (c) 2017
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

package com.spotify.docker.client.exceptions;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import org.junit.Test;

public class DockerRequestExceptionTest {

  @Test
  public void testExceptionMessageWithResponseBody() {
    final URI uri = URI.create("http://example.com");
    final String responseBody = "uh oh";
    final DockerRequestException ex =
        new DockerRequestException("GET", uri, 500, responseBody, new RuntimeException());

    assertEquals(ex.getMessage(), "Request error: GET http://example.com: 500, body: uh oh");
  }

  @Test
  public void testExceptionMessageWhenNoResponseBody() {
    final URI uri = URI.create("http://example.com");
    final String responseBody = null;
    final DockerRequestException ex =
        new DockerRequestException("GET", uri, 500, responseBody, new RuntimeException());

    assertEquals(ex.getMessage(), "Request error: GET http://example.com: 500");
  }
}
