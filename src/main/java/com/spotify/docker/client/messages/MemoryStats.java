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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (failcnt == null ? 0 : failcnt.hashCode());
    result = prime * result + (limit == null ? 0 : limit.hashCode());
    result = prime * result + (maxUsage == null ? 0 : maxUsage.hashCode());
    result = prime * result + (usage == null ? 0 : usage.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MemoryStats other = (MemoryStats) obj;
    if (failcnt == null) {
      if (other.failcnt != null) {
        return false;
      }
    } else if (!failcnt.equals(other.failcnt)) {
      return false;
    }
    if (limit == null) {
      if (other.limit != null) {
        return false;
      }
    } else if (!limit.equals(other.limit)) {
      return false;
    }
    if (maxUsage == null) {
      if (other.maxUsage != null) {
        return false;
      }
    } else if (!maxUsage.equals(other.maxUsage)) {
      return false;
    }
    if (usage == null) {
      if (other.usage != null) {
        return false;
      }
    } else if (!usage.equals(other.usage)) {
      return false;
    }
    return true;
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
