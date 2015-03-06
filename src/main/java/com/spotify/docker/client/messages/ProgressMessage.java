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

package com.spotify.docker.client.messages;

import com.google.common.base.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProgressMessage {

  @JsonProperty private String id;
  @JsonProperty private String status;
  @JsonProperty private String stream;
  @JsonProperty private String error;
  @JsonProperty private String progress;
  @JsonProperty private ProgressDetail progressDetail;

  public String id() {
    return id;
  }

  public ProgressMessage id(final String id) {
    this.id = id;
    return this;
  }

  public String status() {
    return status;
  }

  public ProgressMessage status(final String status) {
    this.status = status;
    return this;
  }

  public String stream() {
    return stream;
  }

  public ProgressMessage stream(final String stream) {
    this.stream = stream;
    return this;
  }

  public String error() {
    return error;
  }

  public ProgressMessage error(final String error) {
    this.error = error;
    return this;
  }

  public String progress() {
    return progress;
  }

  public ProgressMessage progress(final String progress) {
    this.progress = progress;
    return this;
  }

  public ProgressDetail progressDetail() {
    return progressDetail;
  }

  public ProgressMessage progressDetail(final ProgressDetail progressDetail) {
    this.progressDetail = progressDetail;
    return this;
  }

  /**
   * Checks if the stream field contains a string a like "Successfully built 2d6e00052167", and
   * if so, returns the image id. Otherwise null is returned. This string is expected when an image
   * is built successfully.
   * @return The image id if this is a build success message, otherwise null.
   */
  public String buildImageId() {
    // stream messages end with new line, so call trim to remove it
    return stream != null && stream.startsWith("Successfully built")
           ? stream.substring(stream.lastIndexOf(' ') + 1).trim()
           : null;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("id", id)
        .add("status", status)
        .add("stream", stream)
        .add("error", error)
        .add("progress", progress)
        .add("progressDetail", progressDetail)
        .toString();
  }

}
