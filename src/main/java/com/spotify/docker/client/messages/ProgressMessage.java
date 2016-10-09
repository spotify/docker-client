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

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProgressMessage {

  // Prefix that appears before the actual image digest in a 1.6 status message. E.g.:
  // {"status":"Digest: sha256:ebd39c3e3962f804787f6b0520f8f1e35fbd5a01ab778ac14c8d6c37978e8445"}
  private static final String STATUS_DIGEST_PREFIX_16 = "Digest: ";

  // In 1.8, the message instead looks like
  // {"status":"<some-tag>: digest: <digest> size: <size>"}
  private static final String STATUS_DIGEST_PREFIX_18 = "digest: ";
  private static final String STATUS_SIZE_PREFIX_18 = "size: ";

  @JsonProperty
  private String id;
  @JsonProperty
  private String status;
  @JsonProperty
  private String stream;
  @JsonProperty
  private String error;
  @JsonProperty
  private String progress;
  @JsonProperty
  private ProgressDetail progressDetail;

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
   * Checks if the stream field contains a string a like "Successfully built 2d6e00052167", and if
   * so, returns the image id. Otherwise null is returned. This string is expected when an image is
   * built successfully.
   *
   * @return The image id if this is a build success message, otherwise null.
   */
  public String buildImageId() {
    // stream messages end with new line, so call trim to remove it
    return stream != null && stream.startsWith("Successfully built")
           ? stream.substring(stream.lastIndexOf(' ') + 1).trim()
           : null;
  }

  public String digest() {
    if (status == null) {
      return null;
    }

    // the 1.6 format:
    // Digest : <digest ... >
    if (status.startsWith(STATUS_DIGEST_PREFIX_16)) {
      return status.substring(STATUS_DIGEST_PREFIX_16.length()).trim();
    }

    // the 1.8 format:
    // <image-tag>: digest: <digest...> size: <some count of bytes>
    final int digestIndex = status.indexOf(STATUS_DIGEST_PREFIX_18);
    final int sizeIndex = status.indexOf(STATUS_SIZE_PREFIX_18);
    // make sure both substrings exist and that size comes after digest
    if (digestIndex > -1 && sizeIndex > digestIndex) {
      final int start = digestIndex + STATUS_DIGEST_PREFIX_18.length();
      return status.substring(start, sizeIndex - 1);
    }

    return null;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("status", status)
        .add("stream", stream)
        .add("error", error)
        .add("progress", progress)
        .add("progressDetail", progressDetail)
        .toString();
  }

}
