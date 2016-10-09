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
package com.spotify.docker.client.messages.swarm;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Resources {

  @JsonProperty("NanoCPUs")
  private Long nanoCpus;

  @JsonProperty("MemoryBytes")
  private Long memoryBytes;

  public Long nanoCpus() {
    return nanoCpus;
  }

  public Long memoryBytes() {
    return memoryBytes;
  }

  public static class Builder {

    private Resources resources = new Resources();

    public Builder withNanoCpus(Long nanoCpus) {
      resources.nanoCpus = nanoCpus;
      return this;
    }

    public Builder withMemoryBytes(Long memoryBytes) {
      resources.memoryBytes = memoryBytes;
      return this;
    }

    public Resources build() {
      return resources;
    }
  }

  public static Resources.Builder builder() {
    return new Resources.Builder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Resources that = (Resources) o;

    return Objects.equals(this.nanoCpus, that.nanoCpus)
           && Objects.equals(this.memoryBytes, that.memoryBytes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nanoCpus, memoryBytes);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("nanoCpus", nanoCpus)
        .add("memoryBytes", memoryBytes).toString();
  }
}
