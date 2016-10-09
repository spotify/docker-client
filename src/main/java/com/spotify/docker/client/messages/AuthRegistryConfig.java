/*
 * Copyright (c) 2014 Spotify AB.
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

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A formatted string passed in X-Registry-Config request header. {"myrepository":{
 * "username":"myusername", "password":"mypassword", "auth":"", "email":"myemail",
 * "serveraddress":"myserverAddress"}}
 */
public class AuthRegistryConfig {


  private final Map<String, String> properties = new HashMap<String, String>();

  private final Map<String, Map<String, String>> configs = new HashMap<>();
  private final String repository;
  private final String username;
  private final String password;
  private final String auth;
  private final String email;
  private final String serverAddress;

  public static final AuthRegistryConfig EMPTY = new AuthRegistryConfig("", "", "", "", "", "");

  /**
   * Wrapper to support X-Registry-Config header with auth.
   *
   * @param repository    Repository name (uniquely identifies the config)
   * @param username      User name
   * @param password      Password for authentication
   * @param auth          Not used but must be supplied
   * @param email         EMAIL address of authenticated user
   * @param serverAddress The address of the repository
   */
  public AuthRegistryConfig(String repository,
                            String username,
                            String password,
                            String auth,
                            String email,
                            String serverAddress) {
    this.repository = repository;
    this.username = username;
    this.password = password;
    this.auth = auth;
    this.email = email;
    this.serverAddress = serverAddress;
    properties.put("username", username);
    properties.put("password", password);
    properties.put("auth", auth);
    properties.put("email", email);
    properties.put("serveraddress", serverAddress);
    configs.put(repository, properties);
  }

  /**
   * Wrapper to support X-Registry-Config header without auth.
   *
   * @param repository    Repository name (uniquely identifies the config)
   * @param username      User name
   * @param password      Password for authentication
   * @param email         EMAIL address of authenticated user
   * @param serverAddress The address of the repository
   */
  public AuthRegistryConfig(String repository,
                            String username,
                            String password,
                            String email,
                            String serverAddress) {
    this(repository, username, password, "", email, serverAddress);
  }

  @JsonAnyGetter
  public Map<String, Map<String, String>> getConfigs() {
    return configs;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final AuthRegistryConfig that = (AuthRegistryConfig) o;

    return Objects.equals(this.repository, that.repository) &&
           Objects.equals(this.username, that.username) &&
           Objects.equals(this.password, that.password) &&
           Objects.equals(this.auth, that.auth) &&
           Objects.equals(this.email, that.email) &&
           Objects.equals(this.serverAddress, that.serverAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(repository, username, password, auth, email, serverAddress);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("repository", repository)
        .add("username", username)
        .add("password", password)
        .add("auth", auth)
        .add("email", email)
        .add("serverAddress", serverAddress)
        .toString();
  }
}
