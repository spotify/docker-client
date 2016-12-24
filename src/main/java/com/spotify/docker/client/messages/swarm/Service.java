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

package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Service {

  @NotNull
  @JsonProperty("ID")
  public abstract String id();

  @NotNull
  @JsonProperty("Version")
  public abstract Version version();

  @NotNull
  @JsonProperty("CreatedAt")
  public abstract Date createdAt();

  @NotNull
  @JsonProperty("UpdatedAt")
  public abstract Date updatedAt();

  @NotNull
  @JsonProperty("Spec")
  public abstract ServiceSpec spec();

  @NotNull
  @JsonProperty("Endpoint")
  public abstract Endpoint endpoint();

  @NotNull
  @JsonProperty("UpdateStatus")
  public abstract UpdateStatus updateStatus();

  @AutoValue
  public abstract static class Criteria {

    /**
     * Filter by service id.
     */
    @Nullable
    public abstract String serviceId();

    /**
     * Filter by service name.
     */
    @Nullable
    public abstract String serviceName();

    @NotNull
    public static Builder builder() {
      return new AutoValue_Service_Criteria.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder serviceId(@NotNull final String serviceId);

      public abstract Builder serviceName(@NotNull final String serviceName);

      public abstract Criteria build();
    }
  }

  @NotNull
  public static Criteria.Builder find() {
    return Service.Criteria.builder();
  }

  @JsonCreator
  static Service create(
      @JsonProperty("ID") final String id,
      @JsonProperty("Version") final Version version,
      @JsonProperty("CreatedAt") final Date createdAt,
      @JsonProperty("UpdatedAt") final Date updatedAt,
      @JsonProperty("Spec") final ServiceSpec spec,
      @JsonProperty("Endpoint") final Endpoint endpoint,
      @JsonProperty("UpdateStatus") final UpdateStatus updateStatus) {
    return new AutoValue_Service(id, version, createdAt, updatedAt, spec, endpoint, updateStatus);
  }
}
