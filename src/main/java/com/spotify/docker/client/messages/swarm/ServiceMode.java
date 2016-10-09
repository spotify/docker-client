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
public class ServiceMode {

  @JsonProperty("Replicated")
  private ReplicatedService replicated;

  @JsonProperty("Global")
  private GlobalService global;

  public ReplicatedService replicated() {
    return replicated;
  }

  public GlobalService global() {
    return global;
  }

  public static ServiceMode withReplicas(long replicas) {
    return ServiceMode.builder()
        .withReplicatedService(ReplicatedService.builder().withReplicas(replicas).build())
        .build();
  }

  public static ServiceMode withGlobal() {
    return ServiceMode.builder().withGlobalService(new GlobalService()).build();
  }

  public static class Builder {

    private ServiceMode mode = new ServiceMode();

    public Builder withReplicatedService(ReplicatedService replicated) {
      mode.replicated = replicated;
      return this;
    }

    public Builder withGlobalService(GlobalService global) {
      mode.global = global;
      return this;
    }

    public ServiceMode build() {
      return mode;
    }
  }

  public static ServiceMode.Builder builder() {
    return new ServiceMode.Builder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ServiceMode that = (ServiceMode) o;

    return Objects.equals(this.replicated, that.replicated)
           && Objects.equals(this.global, that.global);
  }

  @Override
  public int hashCode() {
    return Objects.hash(replicated, global);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("replicated", replicated).add("global", global)
        .toString();
  }
}
