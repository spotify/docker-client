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

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ContainerMount {

  @JsonProperty("Source") private String source;
  @JsonProperty("Destination") private String destination;
  @JsonProperty("Mode") private String mode;
  @JsonProperty("RW") private Boolean rwInfo;

  public String getSource() {
    return source;
  }

  public String getDestination() {
    return destination;
  }

  public String getMode() {
    return mode;
  }

  public Boolean getRwInfo() {
    return rwInfo;
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

    if (source != null ? !source.equals(that.source) : that.source != null) {
      return false;
    }
    if (destination != null ? !destination.equals(that.destination) : that.destination != null) {
      return false;
    }
    if (mode != null ? !mode.equals(that.mode) : that.mode != null) {
      return false;
    }
    if (rwInfo != null ? !rwInfo.equals(that.rwInfo) : that.rwInfo != null) {
      return false;
    }
    
    return true;
  }

  @Override
  public int hashCode() {
    int result = source != null ? source.hashCode() : 0;
    result = 31 * result + (destination != null ? destination.hashCode() : 0);
    result = 31 * result + (mode != null ? mode.hashCode() : 0);
    result = 31 * result + (rwInfo != null ? rwInfo.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("source", source)
        .add("destination", destination)
        .add("mode", mode)
        .add("rw", rwInfo)
        .toString();
  }
}
