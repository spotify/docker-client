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

import com.google.common.base.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;


import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class AuthConfig {

  @JsonProperty("User") private String user;
  @JsonProperty("Password") private String password;
  @JsonProperty("Email") private String email;

  private AuthConfig() {
  }

  private AuthConfig(final Builder builder) {
    this.user = builder.user;
    this.password = builder.password;
    this.email = builder.email;
  }

  public String user() {
    return user;
  }

  public String password() {
    return password;
  }

  public String email() {
    return email;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final AuthConfig config = (AuthConfig) o;
    if (user != null ? !user.equals(config.user) : config.user != null) {
      return false;
    }
    if (password != null ? !password.equals(config.password) : config.password != null) {
      return false;
    }
    if (email != null ? !email.equals(config.email) : config.email != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = user != null ? user.hashCode() : 0;
    result = 31 * result + (password != null ? password.hashCode() : 0);
    result = 31 * result + (email != null ? email.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("user", user)
        .add("password", password)
        .add("email", email)
        .toString();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String user;
    private String password;
    private String email;

    private Builder() {
    }

    private Builder(final AuthConfig config) {
      this.user = config.user;
      this.password = config.password;
      this.email = config.email;
    }

    public Builder user(final String user) {
      this.user = user;
      return this;
    }

    public String user() {
      return user;
    }

    public Builder password(final String password) {
      this.password = password;
      return this;
    }

    public String password() {
      return password;
    }

    public Builder email(final String email) {
      this.email = email;
      return this;
    }

    public String email() {
      return email;
    }

    public AuthConfig build() {
      return new AuthConfig(this);
    }
  }
}
