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
public class Swarm {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Version")
    private Version version;

    @JsonProperty("CreatedAt")
    private String createdAt;

    @JsonProperty("UpdatedAt")
    private String updatedAt;

    @JsonProperty("Spec")
    private Spec spec;

    @JsonProperty("JoinTokens")
    private JoinTokens joinTokens;

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

    public Spec spec() {
        return spec;
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
                && Objects.equals(this.spec, that.spec)
                && Objects.equals(this.joinTokens, that.joinTokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version, createdAt, updatedAt, spec, joinTokens);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("version", version)
                .add("createdAt", createdAt).add("updatedAt", updatedAt).add("spec", spec)
                .add("joinTokens", joinTokens).toString();
    }
}
