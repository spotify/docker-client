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

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Service {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Meta")
    private Meta meta;

    @JsonProperty("Spec")
    private ServiceSpec spec;

    @JsonProperty("Endpoint")
    private Endpoint endpoint;

    @JsonProperty("UpdateStatus")
    private UpdateStatus updateStatus;

    public String id() {
        return id;
    }

    public Meta meta() {
        return meta;
    }

    public ServiceSpec spec() {
        return spec;
    }

    public Endpoint endpoint() {
        return endpoint;
    }

    public UpdateStatus updateStatus() {
        return updateStatus;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Service that = (Service) o;

        return Objects.equals(this.id, that.id) && Objects.equals(this.meta, that.meta)
                && Objects.equals(this.spec, that.spec)
                && Objects.equals(this.endpoint, that.endpoint)
                && Objects.equals(this.updateStatus, that.updateStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, meta, spec, endpoint, updateStatus);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("meta", meta).add("spec", spec)
                .add("endpoint", endpoint).add("updateStatus", updateStatus).toString();
    }
}
