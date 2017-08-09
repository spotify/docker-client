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
import com.amazonaws.services.ecr.AmazonECRClientBuilder;
import com.amazonaws.services.ecr.model.AuthorizationData;
import com.amazonaws.util.Base64;
import com.google.api.client.util.Clock;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A RegistryAuthSupplier for authenticating against an AWS Elastic Container Registry.
 */
public class ContainerRegistryAuthSupplier implements RegistryAuthSupplier {

  private static final Logger log = LoggerFactory.getLogger(ContainerRegistryAuthSupplier.class);

  /**
   * Constructs a ContainerRegistryAuthSupplier using the Application Default Credentials.
   *
   * @see Builder
   */
  public static Builder forDefaultClient() throws IOException {
    return new Builder(AmazonECRClientBuilder.defaultClient());
  }

  /**
   * Constructs a ContainerRegistryAuthSupplier using the specified credentials.
   *
   * @see Builder
   */
  public static Builder forEcrClient(final AmazonECR ecr) {
    return new Builder(ecr);
  }

  /**
   * A Builder of ContainerRegistryAuthSupplier.
   * <p>
   * The default value for the minimum expiry time of an access token is one minute. When the
   * ContainerRegistryAuthSupplier is asked for a RegistryAuth, it will check if the existing
   * authorization token for the AWS AuthorizationData expires within this amount of time.
   * If it does, then the AuthorizationData is refreshed before being returned.
   * </p>
   */
  public static class Builder {

    private final AmazonECR ecr;
    private long minimumExpiryMillis = TimeUnit.MINUTES.toMillis(1);

    public Builder(final AmazonECR ecr) {
      this.ecr = ecr;
    }

    /**
     * Changes the minimum expiry time used to refresh AccessTokens before they expire. The default
     * value is one minute.
     */
    public Builder withMinimumExpiry(long duration, TimeUnit timeUnit) {
      this.minimumExpiryMillis = TimeUnit.MILLISECONDS.convert(duration, timeUnit);

      return this;
    }

    public ContainerRegistryAuthSupplier build() {
      final Clock clock = Clock.SYSTEM;

      return new ContainerRegistryAuthSupplier(ecr, clock, minimumExpiryMillis,
          new EcrCredentials(ecr));
    }
  }

  private final AmazonECR ecr;

  // TODO (mbrown): change to java.time.Clock once on Java 8
  private final Clock clock;
  private EcrCredentials credentials;
  private final long minimumExpiryMillis;

  @VisibleForTesting
  ContainerRegistryAuthSupplier(final AmazonECR ecr, final Clock clock,
                                final long minimumExpiryMillis, final EcrCredentials credentials) {
    Preconditions.checkArgument(ecr != null, "ecr");
    this.ecr = ecr;
    this.clock = clock;
    this.minimumExpiryMillis = minimumExpiryMillis;
    this.credentials = credentials;
  }

  /**
   * Get an accessToken to use, possibly refreshing the token if it expires within the
   * minimumExpiryMillis.
   */
  private AuthorizationData getAccessToken() throws IOException {
    // synchronize attempts to refresh the accessToken
    synchronized (ecr) {
      if (needsRefresh(credentials.getAuthorizationData())) {
        credentials.refresh();
      }
    }

    Preconditions.checkState(credentials.getAuthorizationData() != null,
        "authorizationData should have been refreshed");

    return credentials.getAuthorizationData();
  }

  boolean needsRefresh(final AuthorizationData accessToken) {
    if (accessToken == null) {
      // has not yet been fetched
      return true;
    }

    final Date expirationTime = accessToken.getExpiresAt();

    // Don't refresh if expiration time hasn't been provided.
    if (expirationTime == null) {
      return true;
    }

    // refresh the token if it expires "soon"
    final long expiresIn = expirationTime.getTime() - clock.currentTimeMillis();

    return expiresIn <= minimumExpiryMillis;
  }

  @Override
  public RegistryAuth authFor(final String imageName) throws DockerException {
    final String[] imageParts = imageName.split("/", 2);

    if ((imageParts.length < 2) || !imageParts[0].contains(".dkr.ecr.")
        || !imageParts[0].contains(".amazonaws.com")) {
      // not an image on ECR
      return null;
    }

    final AuthorizationData accessToken;

    try {
      accessToken = getAccessToken();
    } catch (IOException e) {
      throw new DockerException(e);
    }

    return authForAuthorizationData(accessToken);
  }

  // see http://docs.aws.amazon.com/AmazonECR/latest/APIReference/API_AuthorizationData.html
  private RegistryAuth authForAuthorizationData(final AuthorizationData accessToken) {
    if (accessToken == null) {
      throw new IllegalArgumentException();
    }

    String decoded = new String(Base64.decode(accessToken.getAuthorizationToken()));
    String username = decoded.split(":")[0];
    String password = decoded.split(":")[1];

    return RegistryAuth.builder().username(username).password(password)
        .serverAddress(accessToken.getProxyEndpoint()).build();
  }

  @Override
  public RegistryAuth authForSwarm() throws DockerException {
    final AuthorizationData accessToken;

    try {
      accessToken = getAccessToken();
    } catch (IOException e) {
      // ignore the exception, as the user may not care if swarm is authenticated to use GCR
      log.warn("unable to get access token for AWS Elastic Container Registry due to exception, "
               + "configuration for Swarm will not contain RegistryAuth for ECR", e);

      return null;
    }

    return authForAuthorizationData(accessToken);
  }

  @Override
  public RegistryConfigs authForBuild() throws DockerException {
    final AuthorizationData accessToken;

    try {
      accessToken = getAccessToken();
    } catch (IOException e) {
      // do not fail as the GCR access token may not be necessary for building the image
      // currently
      // being built
      log.warn("unable to get access token for AWS Elastic Container Registry, "
               + "configuration for building image will not contain RegistryAuth for ECR", e);

      return RegistryConfigs.empty();
    }

    final Map<String, RegistryAuth> configs = new HashMap<String, RegistryAuth>(1);
    configs.put(accessToken.getProxyEndpoint(), authForAuthorizationData(accessToken));

    return RegistryConfigs.create(configs);
  }
}
