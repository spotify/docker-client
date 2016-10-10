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
public class ResourceRequirements {

  @JsonProperty("Limits")
  private Resources limits;

  @JsonProperty("Reservations")
  private Resources reservations;

  public Resources limits() {
    return limits;
  }

  public Resources reservations() {
    return reservations;
  }

  public static class Builder {

    private ResourceRequirements req = new ResourceRequirements();

    public Builder withLimits(Resources limits) {
      req.limits = limits;
      return this;
    }

    public Builder withReservations(Resources reservations) {
      req.reservations = reservations;
      return this;
    }

    public ResourceRequirements build() {
      return req;
    }
  }

  public static ResourceRequirements.Builder builder() {
    return new ResourceRequirements.Builder();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ResourceRequirements that = (ResourceRequirements) o;

    return Objects.equals(this.limits, that.limits)
           && Objects.equals(this.reservations, that.reservations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(limits, reservations);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("limits", limits)
        .add("reservations", reservations).toString();
  }
}
