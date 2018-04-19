/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 - 2017 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.docker.client.auth.ecr;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;

import java.util.Objects;

public interface Authenticator extends AutoCloseable {
  public static interface Factory {
    public Authenticator getAuthenticator();
  }
    
  public static class Authentication {
    public final String username;
    public final String password;
    public final String registry;
    public final long expiration;
    public final boolean def;
        
    public Authentication(
        String username,
        String password,
        String registry,
        long expiration,
        boolean def) {
      this.username   = username;
      this.password   = password;
      this.registry   = registry;
      this.expiration = expiration;
      this.def        = def;
    }
        
    public RegistryAuth toRegistryAuth() {
      return RegistryAuth.builder()
        .username(username)
        .password(password)
        .serverAddress(registry)
        .build();
    }
    
    public int hashCode() {
      return Objects.hash(
        username,
        password,
        registry,
        expiration,
        def);
    }
    
    public boolean equals(Object other) {
      boolean result;
      if (this == other) {
        result = true;
      } else
        if (other == null) {
          result = false;
        } else
          if (other instanceof Authentication) {
            Authentication that = (Authentication) other;
            result = Objects.equals(username, that.username)
              && Objects.equals(password, password)
              && Objects.equals(registry, that.registry)
              && expiration == that.expiration
              && def == that.def;
          } else { 
            result = false;
          }
      return result;
    }
  }
  
  public Authentication authenticate(String registry) throws DockerException;
  
  public void close() throws DockerException;
}
