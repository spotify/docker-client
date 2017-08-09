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

import static com.google.common.base.Preconditions.checkState;

import com.amazonaws.services.ecr.AmazonECR;
import com.amazonaws.services.ecr.model.AuthorizationData;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenRequest;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult;
import java.io.IOException;

/**
 * Makes getting the authorization data easier.
 */
class EcrCredentials {

  private final AmazonECR ecr;
  private AuthorizationData authorizationData;

  EcrCredentials(final AmazonECR ecr) {
    this(ecr, null);
  }

  EcrCredentials(final AmazonECR ecr, final AuthorizationData authorizationData) {
    this.ecr = ecr;
    this.authorizationData = authorizationData;
  }

  public AuthorizationData getAuthorizationData() {
    return authorizationData;
  }

  void refresh() throws IOException {
    GetAuthorizationTokenResult authorizationToken = ecr
        .getAuthorizationToken(new GetAuthorizationTokenRequest());
    checkState(authorizationToken != null, "Unable to get auth token result from ECR");

    AuthorizationData data = authorizationToken.getAuthorizationData().get(0);
    checkState(data != null, "Unable to get auth data from ECR");
    this.authorizationData = data;
  }
}
