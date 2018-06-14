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
package com.spotify.docker.client.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

public class Ulimit {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Soft")
    private Long soft;
    @JsonProperty("Hard")
    private Long hard;

    public Ulimit() {
    }

    public Ulimit(final String name, final Long soft,
                  final Long hard) {
        this.name = name;
        this.soft = soft;
        this.hard = hard;
    }

    public String getName() {
        return name;
    }

    public Long getSoft() {
        return soft;
    }

    public Long getHard() {
        return hard;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Ulimit that = (Ulimit) o;

        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.soft, that.soft) &&
                Objects.equals(this.hard, that.hard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, soft, hard);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("soft", soft)
                .add("hard", hard)
                .toString();
    }
}
