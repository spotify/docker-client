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
import static com.google.common.base.Strings.isNullOrEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class SwarmInitRequest {

    private static final String DEFAULT_LISTEN_ADDR = "0.0.0.0:2377";

    @JsonProperty("ListenAddr")
    private String listenAddr;

    @JsonProperty("AdvertiseAddr")
    private String advertiseAddr;

    @JsonProperty("ForceNewCluster")
    private Boolean forceNewCluster;

    @JsonProperty("Spec")
    private SwarmSpec spec;

    private SwarmInitRequest(final Builder builder) {
        this.listenAddr = isNullOrEmpty(builder.listenAddr)
                          ? DEFAULT_LISTEN_ADDR :  builder.listenAddr;
        this.advertiseAddr = builder.advertiseAddr;
        this.forceNewCluster = builder.forceNewCluster;
        this.spec = builder.spec;
    }

    public String listenAddr() {
        return listenAddr;
    }

    public String advertiseAddr() {
        return advertiseAddr;
    }

    public Boolean forceNewCluster() {
        return forceNewCluster;
    }

    public SwarmSpec spec() {
        return spec;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SwarmInitRequest that = (SwarmInitRequest) o;

        return Objects.equals(this.listenAddr, that.listenAddr)
               && Objects.equals(this.advertiseAddr, that.advertiseAddr)
               && Objects.equals(this.spec, that.spec);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listenAddr, advertiseAddr, spec);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("listenAddr", listenAddr)
            .add("advertiseAddr", advertiseAddr)
            .add("forceNewCluster", forceNewCluster)
            .add("spec", spec)
            .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String listenAddr;
        private String advertiseAddr;
        private boolean forceNewCluster;
        private SwarmSpec spec;

        public Builder listenAddr(final String listenAddr) {
            this.listenAddr = listenAddr;
            return this;
        }

        public Builder advertiseAddr(final String advertiseAddr) {
            this.advertiseAddr = advertiseAddr;
            return this;
        }

        public Builder forceNewCluster(final boolean forceNewCluster) {
            this.forceNewCluster = forceNewCluster;
            return this;
        }

        public Builder spec(final SwarmSpec spec) {
            this.spec = spec;
            return this;
        }

        public SwarmInitRequest build() {
            return new SwarmInitRequest(this);
        }
    }
}
