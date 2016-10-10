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

import java.util.Date;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Swarm {

  @JsonProperty("ID")
  private String id;

  @JsonProperty("Version")
  private Version version;

  @JsonProperty("CreatedAt")
  private Date createdAt;

  @JsonProperty("UpdatedAt")
  private Date updatedAt;

  @JsonProperty("Spec")
  private SwarmSpec swarmSpec;

  @JsonProperty("JoinTokens")
  private JoinTokens joinTokens;

  public String id() {
    return id;
  }

  public Version version() {
    return version;
  }

  public Date createdAt() {
    return createdAt == null ? null : new Date(createdAt.getTime());
  }

  public Date updatedAt() {
    return updatedAt == null ? null : new Date(updatedAt.getTime());
  }

  public SwarmSpec spec() {
    return swarmSpec;
  }

  public JoinTokens joinTokens() {
    return joinTokens;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Swarm that = (Swarm) o;

    return Objects.equals(this.id, that.id) && Objects.equals(this.version, that.version)
           && Objects.equals(this.createdAt, that.createdAt)
           && Objects.equals(this.updatedAt, that.updatedAt)
           && Objects.equals(this.swarmSpec, that.swarmSpec)
           && Objects.equals(this.joinTokens, that.joinTokens);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, version, createdAt, updatedAt, swarmSpec, joinTokens);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).add("version", version)
        .add("createdAt", createdAt).add("updatedAt", updatedAt).add("spec", swarmSpec)
        .add("joinTokens", joinTokens).toString();
  }
}
