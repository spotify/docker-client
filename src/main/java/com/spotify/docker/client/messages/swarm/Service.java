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

    @JsonProperty("Version")
    private Version version;

    @JsonProperty("CreatedAt")
    private String createdAt;

    @JsonProperty("UpdatedAt")
    private String updatedAt;

    @JsonProperty("Spec")
    private ServiceSpec spec;

    @JsonProperty("Endpoint")
    private Endpoint endpoint;

    @JsonProperty("UpdateStatus")
    private UpdateStatus updateStatus;

    public String id() {
        return id;
    }

    public Version version() {
        return version;
    }

    public String createdAt() {
        return createdAt;
    }

    public String updatedAt() {
        return updatedAt;
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

    public static class Criteria {

        /** Filter by service id */
        String serviceId;

        /** Filter by service name */
        String serviceName;

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
    }

    public static class CriteriaBuilder {

        /** Criteria being built */
        private Criteria criteria = new Criteria();

        public CriteriaBuilder forServiceId(String serviceId) {
            criteria.setServiceId(serviceId);
            return this;
        }

        public CriteriaBuilder forServiceName(String serviceName) {
            criteria.setServiceName(serviceName);
            return this;
        }

        public Criteria build() {
            return criteria;
        }
    }

    public static CriteriaBuilder newCriteriaBuilder() {
        return new Service.CriteriaBuilder();
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

        return Objects.equals(this.id, that.id) && Objects.equals(this.version, that.version)
                && Objects.equals(this.createdAt, that.createdAt)
                && Objects.equals(this.updatedAt, that.updatedAt)
                && Objects.equals(this.spec, that.spec)
                && Objects.equals(this.endpoint, that.endpoint)
                && Objects.equals(this.updateStatus, that.updateStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, createdAt, updatedAt, spec, endpoint, updateStatus);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("version", version)
                .add("createdAt", createdAt).add("updatedAt", updatedAt).add("spec", spec)
                .add("endpoint", endpoint).add("updateStatus", updateStatus).toString();
    }
}
