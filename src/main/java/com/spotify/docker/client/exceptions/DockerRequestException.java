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

package com.spotify.docker.client.exceptions;

import java.net.URI;

public class DockerRequestException extends DockerException {

  private final String method;
  private final URI uri;
  private final int status;
  private final String message;

  public DockerRequestException(final String method, final URI uri,
                                final int status, final String message,
                                final Throwable cause) {
    super("Request error: " + method + " " + uri + ": " + status
          + ((message != null) ? ", body: " + message : ""),
        cause);
    this.method = method;
    this.uri = uri;
    this.status = status;
    this.message = message;
  }

  public String method() {
    return method;
  }

  public URI uri() {
    return uri;
  }

  public int status() {
    return status;
  }

  public String message() {
    return message;
  }
}
