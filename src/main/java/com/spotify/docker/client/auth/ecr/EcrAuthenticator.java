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

import com.amazonaws.services.ecr.AmazonECR;
import com.amazonaws.services.ecr.model.AuthorizationData;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenRequest;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult;
import com.spotify.docker.client.exceptions.DockerException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EcrAuthenticator implements Authenticator {
  private final AmazonECR client;
  
  public EcrAuthenticator(AmazonECR client) {
    this.client = client;
  }

  @Override
  public Authentication authenticate(String registry) throws DockerException {
    GetAuthorizationTokenRequest request = new GetAuthorizationTokenRequest();
    if (registry != null) {
      request = request.withRegistryIds(registry);
    }
      
    AuthorizationData authorization;
    try {
      GetAuthorizationTokenResult response = client.getAuthorizationToken(request);
      authorization = response.getAuthorizationData().get(0);
    } catch (Exception e) { 
      throw new DockerException("Failed to retrieve ECR credentials", e);
    }
        
    String auth = new String(Base64.getDecoder()
        .decode(authorization.getAuthorizationToken()), StandardCharsets.UTF_8);
    String[] authParts = auth.split(":", 2);
    if (authParts.length < 2) {
      // Never put credentials -- even encoded credentials -- in an Exception message.
      throw new DockerException("Failed to parse ECR credentials");
    }
              
    String username = authParts[0];
    String password = authParts[1];
     
    String newregistry;
    String endpoint = authorization.getProxyEndpoint();
    if (endpoint.startsWith("https://")) {
      newregistry = endpoint.substring("https://".length(), endpoint.length());
    } else {
      throw new DockerException("Failed to parse ECR endpoint: " + endpoint);
    }
    
    long expiration;
    if (authorization.getExpiresAt() != null) {
      expiration = authorization.getExpiresAt().getTime();
    } else {
      expiration =  -1L;
    }
    
    return new Authentication(username, password, newregistry, expiration, registry == null);
  }
    
  @Override
  public void close() throws DockerException {
    getClient().shutdown();
  }

  public AmazonECR getClient() {
    return client;
  }
}
