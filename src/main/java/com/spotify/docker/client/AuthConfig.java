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

package com.spotify.docker.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Objects;
import com.sun.jersey.core.util.Base64;

/**
 * Configuration object holding auth information for pushing to Docker
 */
public class AuthConfig {

    private String username;
    private String password;
    private String email;
    private String auth;

    public AuthConfig() {
    }

    public AuthConfig(String user, String password, String email, String auth) {
		this.username = user;
		this.password = password;
		this.email = email;
		this.auth = auth;
    }

    @JsonIgnore
    public String toHeaderValue() {
    	ObjectNode ret = new ObjectNode(JsonNodeFactory.instance);
        
    	ret.put("username", username);
    	ret.put("password", password);
    	ret.put("email", email);
    	ret.put("auth", auth);
        
        return new String(Base64.encode(ret.toString().getBytes()));
    }
    
    @Override
    public int hashCode() {
      int result = username != null ? username.hashCode() : 0;
      result = 31 * result + (password != null ? password.hashCode() : 0);
      result = 31 * result + (email != null ? email.hashCode() : 0);
      result = 31 * result + (auth != null ? auth.hashCode() : 0);
      return result;
    }
    @Override
    public String toString() {
      return Objects.toStringHelper(this)
              .add("username", username)
              .add("password", password)
              .add("email", email)
              .add("auth", auth)
              .toString();
    }
}
