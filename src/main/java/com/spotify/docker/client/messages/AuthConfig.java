/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
import java.util.Objects;

/**
 *
 * @author Peter Harman
 */
public class AuthConfig {

    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @JsonProperty("email")
    private String email;

    private AuthConfig() {
    }

    private AuthConfig(Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.email = builder.email;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public String email() {
        return email;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.username);
        hash = 41 * hash + Objects.hashCode(this.password);
        hash = 41 * hash + Objects.hashCode(this.email);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AuthConfig other = (AuthConfig) obj;
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        if (!Objects.equals(this.email, other.email)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("username", username)
                .add("password", password)
                .add("email", email)
                .toString();
    }    

    public static class Builder {

        private String username;
        private String password;
        private String email;

        public Builder() {
        }

        public Builder(AuthConfig config) {

        }

        public String username() {
            return username;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public String password() {
            return password;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public String email() {
            return email;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public AuthConfig build() {
            return new AuthConfig(this);
        }
    }
}
