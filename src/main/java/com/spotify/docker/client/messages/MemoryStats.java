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

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class MemoryStats {

  @JsonProperty("max_usage") private Long maxUsage;
  @JsonProperty("usage") private Long usage;
  @JsonProperty("failcnt") private Long failcnt;
  @JsonProperty("limit") private Long limit;

  public Long maxUsage() {
    return maxUsage;
  }

  public Long usage() {
    return usage;
  }

  public Long failcnt() {
    return failcnt;
  }

  public Long limit() {
    return limit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final MemoryStats that = (MemoryStats) o;

    return Objects.equals(this.maxUsage, that.maxUsage) &&
        Objects.equals(this.usage, that.usage) &&
        Objects.equals(this.failcnt, that.failcnt) &&
        Objects.equals(this.limit, that.limit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(maxUsage, usage, failcnt, limit);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("failcnt", failcnt)
        .add("limit", limit)
        .add("maxUsage", maxUsage)
        .add("usage", usage)
        .toString();
  }
}
