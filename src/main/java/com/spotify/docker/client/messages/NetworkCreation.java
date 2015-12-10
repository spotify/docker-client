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
import com.google.common.collect.ImmutableList;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class NetworkCreation {

  @JsonProperty("Id") private String id;
  @JsonProperty("Warnings") private ImmutableList<String> warnings;

  public NetworkCreation() {
  }

  public NetworkCreation(final String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  public List<String> getWarnings() {
    return warnings;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final NetworkCreation that = (NetworkCreation) o;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (warnings != null ? !warnings.equals(that.warnings) : that.warnings != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (warnings != null ? warnings.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("warnings", warnings)
        .toString();
  }
}
