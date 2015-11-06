/*
 * Copyright (c) 2014 CyDesign Ltd.
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

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.spotify.docker.client.AuthConfigParser;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class AuthConfig {

  @JsonProperty("Username") private String username;
  @JsonProperty("Password") private String password;
  @JsonProperty("Email") private String email;
  @JsonProperty("ServerAddress") private String serverAddress;

  private AuthConfig() {
  }

  private AuthConfig(final Builder builder) {
    this.username = builder.username;
    this.password = builder.password;
    this.email = builder.email;
    this.serverAddress = builder.serverAddress;
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

  public String serverAddress() {
    return serverAddress;
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
    if (username != null ? !username.equals(config.username) : config.username != null) {
      return false;
    }
    if (password != null ? !password.equals(config.password) : config.password != null) {
      return false;
    }
    if (email != null ? !email.equals(config.email) : config.email != null) {
      return false;
    }
    if (serverAddress != null ?
        !serverAddress.equals(config.serverAddress) : config.serverAddress != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = username != null ? username.hashCode() : 0;
    result = 31 * result + (password != null ? password.hashCode() : 0);
    result = 31 * result + (email != null ? email.hashCode() : 0);
    result = 31 * result + (serverAddress != null ? serverAddress.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .add("password", password)
        .add("email", email)
        .add("serverAddress", serverAddress)
        .toString();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public Builder fromDockerConfig(String serverAddress) {
    return new AuthConfigParser().getBuilderFor(serverAddress);
  }

  public Builder fromDockerConfig() {
    return new AuthConfigParser().getBuilder();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String username;
    private String password;
    private String email;
    // Default to the public Docker registry.
    private String serverAddress = "https://index.docker.io/v1/";

    private Builder() {
    }

    private Builder(final AuthConfig config) {
      this.username = config.username;
      this.password = config.password;
      this.email = config.email;
      this.serverAddress = config.serverAddress;
    }

    public Builder username(final String username) {
      this.username = username;
      return this;
    }

    public String username() {
      return username;
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

    public Builder serverAddress(final String serverAddress) {
      this.serverAddress = serverAddress;
      return this;
    }

    public String serverAddress() {
      return serverAddress;
    }


    public AuthConfig build() {
      return new AuthConfig(this);
    }
  }
}
