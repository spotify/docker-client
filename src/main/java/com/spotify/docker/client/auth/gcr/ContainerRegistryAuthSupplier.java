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

package com.spotify.docker.client.auth.gcr;

import com.google.api.client.util.Clock;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.UserCredentials;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A RegistryAuthSupplier for getting access tokens from a Google Cloud Platform service or user
 * account. This implementation uses the google-auth-library-oauth2-http library to get an access
 * token given an account's credentials, and will refresh the token if it expires within a
 * configurable amount of time.
 * <p>
 * To construct a new instance, use
 * {@link ContainerRegistryAuthSupplier#fromStream(InputStream)} to get a {@link Builder} if you
 * have credentials for the account to use, or you can use
 * {@link ContainerRegistryAuthSupplier#forApplicationDefaultCredentials()}
 * if you would like to use
 * <a href="https://developers.google.com/identity/protocols/application-default-credentials">the
 * Application Default Credentials</a>.</p>
 * <p>
 * The scopes used to fetch an access token and the minimum expiry time can be configured via the
 * Builder before calling {@link Builder#build()}.</p>
 */
public class ContainerRegistryAuthSupplier implements RegistryAuthSupplier {

  private static final Logger log = LoggerFactory.getLogger(ContainerRegistryAuthSupplier.class);

  // the list returned by `gcloud docker -a`
  // this may change in the future, and we can't know all values - but should cover most use cases
  private static final Set<String> GCR_REGISTRIES = ImmutableSet.of(
      "gcr.io",
      "us.gcr.io",
      "eu.gcr.io",
      "asia.gcr.io",
      "b.gcr.io",
      "bucket.gcr.io",
      "l.gcr.io",
      "launcher.gcr.io",
      "appengine.gcr.io",
      "us-mirror.gcr.io",
      "eu-mirror.gcr.io",
      "asia-mirror.gcr.io",
      "mirror.gcr.io"
  );

  /**
   * Constructs a ContainerRegistryAuthSupplier for the account with the given credentials.
   *
   * @see Builder
   */
  public static Builder fromStream(final InputStream credentialsStream) throws IOException {
    final GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
    return new Builder(credentials);
  }

  /**
   * Constructs a ContainerRegistryAuthSupplier using the Application Default Credentials.
   *
   * @see Builder
   */
  public static Builder forApplicationDefaultCredentials() throws IOException {
    return new Builder(GoogleCredentials.getApplicationDefault());
  }

  /**
   * Constructs a ContainerRegistryAuthSupplier using the specified credentials.
   *
   * @see Builder
   */
  public static Builder forCredentials(final GoogleCredentials credentials) {
    return new Builder(credentials);
  }

  /**
   * A Builder of ContainerRegistryAuthSupplier.
   * <p>
   * The access tokens returned by the ContainerRegistryAuthSupplier are scoped to
   * devstorage.read_write by default, these can be customized with {@link #withScopes(Collection)}.
   * </p>
   * <p>
   * The default value for the minimum expiry time of an access token is one minute. When the
   * ContainerRegistryAuthSupplier is asked for a RegistryAuth, it will check if the existing
   * AccessToken for the GoogleCredentials expires within this amount of time. If it does, then the
   * AccessToken is refreshed before being returned. </p>
   */
  public static class Builder {

    private final GoogleCredentials credentials;

    private Collection<String> scopes =
        ImmutableList.of("https://www.googleapis.com/auth/devstorage.read_write");

    private long minimumExpiryMillis = TimeUnit.MINUTES.toMillis(1);

    public Builder(final GoogleCredentials credentials) {
      this.credentials = credentials;
    }

    /**
     * Changes the <a href="https://cloud.google.com/storage/docs/authentication#oauth">scopes</a>
     * used in fetching AccessTokens.
     * <p>
     * The default is devstorage.read_write, which allows pulling and pushing of images.
     * To allow the application using ContainerRegistryAuthSupplier to pull but not push images,
     * change the scope to contain only devstorage.read_only.
     * only</p>
     */
    public Builder withScopes(Collection<String> scopes) {
      this.scopes = scopes;
      return this;
    }

    /**
     * Changes the minimum expiry time used to refresh AccessTokens before they expire. The
     * default value is one minute.
     */
    public Builder withMinimumExpiry(long duration, TimeUnit timeUnit) {
      this.minimumExpiryMillis = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
      return this;
    }

    public ContainerRegistryAuthSupplier build() {
      final GoogleCredentials credentials = this.credentials.createScoped(scopes);

      // log some sort of identifier for the credentials, which requires looking at the
      // instance type
      if (credentials instanceof ServiceAccountCredentials) {
        final String clientEmail = ((ServiceAccountCredentials) credentials).getClientEmail();
        log.info("loaded credentials for service account with clientEmail={}", clientEmail);
      } else if (credentials instanceof UserCredentials) {
        final String clientId = ((UserCredentials) credentials).getClientId();
        log.info("loaded credentials for user account with clientId={}", clientId);
      }

      final Clock clock = Clock.SYSTEM;
      final DefaultCredentialRefresher refresher = new DefaultCredentialRefresher();

      return new ContainerRegistryAuthSupplier(credentials, clock, minimumExpiryMillis, refresher);
    }
  }

  /**
   * Refreshes a GoogleCredentials instance. This only exists for testing - we cannot mock calls to
   * {@link GoogleCredentials#refresh()} as the method is final.
   */
  @VisibleForTesting
  interface CredentialRefresher {

    void refresh(GoogleCredentials credentials) throws IOException;
  }

  private static class DefaultCredentialRefresher implements CredentialRefresher {

    @Override
    public void refresh(final GoogleCredentials credentials) throws IOException {
      credentials.refresh();
    }
  }

  private final GoogleCredentials credentials;
  // TODO (mbrown): change to java.time.Clock once on Java 8
  private final Clock clock;
  private final long minimumExpiryMillis;
  private final CredentialRefresher credentialRefresher;

  @VisibleForTesting
  ContainerRegistryAuthSupplier(
      final GoogleCredentials credentials,
      final Clock clock,
      final long minimumExpiryMillis,
      final CredentialRefresher credentialRefresher) {

    this.credentials = credentials;
    this.clock = clock;
    this.minimumExpiryMillis = minimumExpiryMillis;
    this.credentialRefresher = credentialRefresher;
  }

  /**
   * Get an accessToken to use, possibly refreshing the token if it expires within the
   * minimumExpiryMillis.
   */
  private AccessToken getAccessToken() throws IOException {
    // synchronize attempts to refresh the accessToken
    synchronized (credentials) {
      if (needsRefresh(credentials.getAccessToken())) {
        credentialRefresher.refresh(credentials);
      }
    }
    return credentials.getAccessToken();
  }

  private boolean needsRefresh(final AccessToken accessToken) {
    if (accessToken == null) {
      // has not yet been fetched
      return true;
    }

    final Date expirationTime = credentials.getAccessToken().getExpirationTime();

    // Don't refresh if expiration time hasn't been provided.
    if (expirationTime == null) {
      return false;
    }

    // refresh the token if it expires "soon"
    final long expiresIn = expirationTime.getTime() - clock.currentTimeMillis();

    return expiresIn <= minimumExpiryMillis;
  }

  @Override
  public RegistryAuth authFor(final String imageName) throws DockerException {
    final String[] imageParts = imageName.split("/", 2);
    if (imageParts.length < 2 || !GCR_REGISTRIES.contains(imageParts[0])) {
      // not an image on GCR
      return null;
    }

    final AccessToken accessToken;
    try {
      accessToken = getAccessToken();
    } catch (IOException e) {
      throw new DockerException(e);
    }
    return authForAccessToken(accessToken);
  }

  // see https://cloud.google.com/container-registry/docs/advanced-authentication
  private RegistryAuth authForAccessToken(final AccessToken accessToken) {
    return RegistryAuth.builder()
        .username("oauth2accesstoken")
        .password(accessToken.getTokenValue())
        .build();
  }

  @Override
  public RegistryAuth authForSwarm() throws DockerException {
    final AccessToken accessToken;
    try {
      accessToken = getAccessToken();
    } catch (IOException e) {
      // ignore the exception, as the user may not care if swarm is authenticated to use GCR
      log.warn("unable to get access token for Google Container Registry due to exception, "
               + "configuration for Swarm will not contain RegistryAuth for GCR",
          e);
      return null;
    }
    return authForAccessToken(accessToken);
  }

  @Override
  public RegistryConfigs authForBuild() throws DockerException {
    final AccessToken accessToken;
    try {
      accessToken = getAccessToken();
    } catch (IOException e) {
      // do not fail as the GCR access token may not be necessary for building the image currently
      // being built
      log.warn("unable to get access token for Google Container Registry, "
               + "configuration for building image will not contain RegistryAuth for GCR",
          e);
      return RegistryConfigs.empty();
    }

    final Map<String, RegistryAuth> configs = new HashMap<>(GCR_REGISTRIES.size());
    for (String serverName : GCR_REGISTRIES) {
      configs.put(serverName, authForAccessToken(accessToken));
    }
    return RegistryConfigs.create(configs);
  }
}
