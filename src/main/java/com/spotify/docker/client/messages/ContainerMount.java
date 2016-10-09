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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ContainerMount {

  @JsonProperty("Source")
  private String source;
  @JsonProperty("Destination")
  private String destination;
  @JsonProperty("Mode")
  private String mode;
  @JsonProperty("RW")
  private Boolean rw;

  public String source() {
    return source;
  }

  public String destination() {
    return destination;
  }

  public String mode() {
    return mode;
  }

  public Boolean rw() {
    return rw;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ContainerMount that = (ContainerMount) o;

    return Objects.equals(this.source, that.source) &&
           Objects.equals(this.destination, that.destination) &&
           Objects.equals(this.mode, that.mode) &&
           Objects.equals(this.rw, that.rw);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, destination, mode, rw);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("source", source)
        .add("destination", destination)
        .add("mode", mode)
        .add("rw", rw)
        .toString();
  }
}
