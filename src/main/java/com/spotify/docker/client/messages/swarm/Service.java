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
import com.google.common.collect.ImmutableMap;

import java.util.Date;
import java.util.Map;

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Service {

  @JsonProperty("ID")
  public abstract String id();

  @JsonProperty("Version")
  public abstract Version version();

  @JsonProperty("CreatedAt")
  public abstract Date createdAt();

  @JsonProperty("UpdatedAt")
  public abstract Date updatedAt();

  @JsonProperty("Spec")
  public abstract ServiceSpec spec();

  @JsonProperty("Endpoint")
  public abstract Endpoint endpoint();

  @Nullable
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

    /**
     * Filter by label.
     */
    @Nullable
    public abstract ImmutableMap<String, String> labels();
    
    public static Builder builder() {
      return new AutoValue_Service_Criteria.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder serviceId(final String serviceId);

      /**
       * @deprecated  As of release 7.0.0, replaced by {@link #serviceId(String)}.
       */
      @Deprecated
      public Builder withServiceId(final String serviceId) {
        serviceId(serviceId);
        return this;
      }

      public abstract Builder serviceName(final String serviceName);

      /**
       * @deprecated  As of release 7.0.0, replaced by {@link #serviceName(String)}.
       */
      @Deprecated
      public Builder withServiceName(final String serviceName) {
        serviceName(serviceName);
        return this;
      }

      public abstract Builder labels(final Map<String, String> labels);
      
      abstract ImmutableMap.Builder<String, String> labelsBuilder();

      public Builder addLabel(final String label, final String value) {
        labelsBuilder().put(label, value);
        return this;
      }
      
      public abstract Criteria build();
    }
  }

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
