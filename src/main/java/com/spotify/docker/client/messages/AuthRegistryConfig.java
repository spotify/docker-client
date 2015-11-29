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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.google.common.base.MoreObjects;

import java.util.HashMap;
import java.util.Map;

/**
  * A formatted string passed in X-Registry-Config request header.
  * {"myrepository":{
  *   "username":"myusername",
  *   "password":"mypassword",
  *   "auth":"",
  *   "email":"myemail",
  *   "serveraddress":"myserverAddress"}}
*/
public class AuthRegistryConfig {


  private final Map<String, String> properties = new HashMap<String, String>();

  private final Map<String, Map<String, String>> configs =
                             new HashMap<String, Map<String, String>>();
  private final String repository;
  private final String username;
  private final String password;
  private final String auth;
  private final String email;
  private final String serverAddress;

  public static final AuthRegistryConfig EMPTY = new AuthRegistryConfig("", "", "", "", "", "");

  /**
   * Wrapper to support X-Registry-Config header with auth.
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

    final AuthRegistryConfig config = (AuthRegistryConfig) o;
    if (repository != null ? !repository.equals(config.repository) : config.repository != null) {
      return false;
    }
    if (username != null ? !username.equals(config.username) : config.username != null) {
      return false;
    }
    if (password != null ? !password.equals(config.password) : config.password != null) {
      return false;
    }
    if (auth != null ? !auth.equals(config.auth) : config.auth != null) {
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
    int result = repository != null ? repository.hashCode() : 0;
    result = 31 * result + (username != null ? username.hashCode() : 0);
    result = 31 * result + (password != null ? password.hashCode() : 0);
    result = 31 * result + (auth != null ? auth.hashCode() : 0);
    result = 31 * result + (email != null ? email.hashCode() : 0);
    result = 31 * result + (serverAddress != null ? serverAddress.hashCode() : 0);
    return result;
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
