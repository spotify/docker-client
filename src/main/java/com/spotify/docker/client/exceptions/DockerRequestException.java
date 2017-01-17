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
import javax.annotation.Nullable;

public class DockerRequestException extends DockerException {

  private final String method;
  private final URI uri;
  private final int status;
  private final String responseBody;

  public DockerRequestException(final String method, final URI uri,
                                final int status, final String responseBody,
                                final Throwable cause) {
    super("Request error: " + method + " " + uri + ": " + status
          + ((responseBody != null) ? ", body: " + responseBody : ""),
        cause);
    this.method = method;
    this.uri = uri;
    this.status = status;
    this.responseBody = responseBody;
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

  /**
   * The response body from the HTTP response containing an error, if any.
   *
   * @deprecated use {@link #getResponseBody()} instead to avoid confusion with {@link
   * Throwable#getMessage()}.
   */
  @Deprecated
  @Nullable
  public String message() {
    return responseBody;
  }

  /**
   * The response body from the HTTP response containing an error, if any.
   *
   * @return response body or null.
   */
  @Nullable
  public String getResponseBody() {
    return responseBody;
  }
}
