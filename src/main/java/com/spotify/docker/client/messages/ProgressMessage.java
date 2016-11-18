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

package com.spotify.docker.client.messages;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ProgressMessage {

  // Prefix that appears before the actual image digest in a 1.6 status message. E.g.:
  // {"status":"Digest: sha256:ebd39c3e3962f804787f6b0520f8f1e35fbd5a01ab778ac14c8d6c37978e8445"}
  private static final String STATUS_DIGEST_PREFIX_16 = "Digest: ";

  // In 1.8, the message instead looks like
  // {"status":"<some-tag>: digest: <digest> size: <size>"}
  private static final String STATUS_DIGEST_PREFIX_18 = "digest: ";
  private static final String STATUS_SIZE_PREFIX_18 = "size: ";

  @Nullable
  @JsonProperty("id")
  public abstract String id();

  @Nullable
  @JsonProperty("status")
  public abstract String status();

  @Nullable
  @JsonProperty("stream")
  public abstract String stream();

  @Nullable
  @JsonProperty("error")
  public abstract String error();

  @Nullable
  @JsonProperty("progress")
  public abstract String progress();

  @Nullable
  @JsonProperty("progressDetail")
  public abstract ProgressDetail progressDetail();

  @JsonCreator
  static ProgressMessage create(
      @JsonProperty("id") final String id,
      @JsonProperty("status") final String status,
      @JsonProperty("stream") final String stream,
      @JsonProperty("error") final String error,
      @JsonProperty("progress") final String progress,
      @JsonProperty("progressDetail") final ProgressDetail progressDetail) {
    return builder()
        .id(id)
        .status(status)
        .stream(stream)
        .error(error)
        .progress(progress)
        .progressDetail(progressDetail)
        .build();
  }

  public static Builder builder() {
    return new AutoValue_ProgressMessage.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder id(String id);

    public abstract Builder status(String status);

    public abstract Builder stream(String stream);

    public abstract Builder error(String error);

    public abstract Builder progress(String progress);

    public abstract Builder progressDetail(ProgressDetail progressDetail);

    public abstract ProgressMessage build();
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
    final String stream = stream();
    return stream != null && stream.startsWith("Successfully built")
           ? stream.substring(stream.lastIndexOf(' ') + 1).trim()
           : null;
  }

  public String digest() {
    final String status = status();
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
}
