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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import com.google.api.client.util.Clock;
import com.google.common.annotations.VisibleForTesting;

import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A RegistryAuthSupplier for getting access tokens from a Amazon Web Services service or user
 * account. This implementation uses the aos-java-sdk-core library to get an access
 * token given an account's credentials, and will refresh the token if it expires within a
 * configurable amount of time.
 * </p>
 * 
 * <p>
 * To construct a new instance, use any of the static methods that return a {@link Builder}.
 * </p>
 */
public class ElasticContainerRegistryAuthSupplier implements RegistryAuthSupplier {
  private static final Logger log = LoggerFactory
      .getLogger(ElasticContainerRegistryAuthSupplier.class);
  
  /**
   * Constructs an ElasticContainerRegistryAuthSupplier using the default provider chain.
   * 
   * @see DefaultAWSCredentialsProviderChain
   * @see Builder
   */
  public static Builder getApplicationDefaultDefault() throws IOException {
    return forProvider(new DefaultAWSCredentialsProviderChain());
  }
  
  /**
   * Constructs an ElasticContainerRegistryAuthSupplier using data from the program
   * environment variables AWS_ACCESS_KEY and AWS_SECRET_ACCESS_KEY.
   * 
   * @see EnvironmentVariableCredentialsProvider
   * @see Builder
   */
  public static Builder forEnvironmentVariables() throws IOException {
    return forProvider(new EnvironmentVariableCredentialsProvider());
  }
  
  /**
   * Constructs an ElasticContainerRegistryAuthSupplier using data from the program
   * environment variables AWS_ACCESS_KEY and AWS_SECRET_ACCESS_KEY.
   * 
   * @see SystemPropertiesCredentialsProvider
   * @see Builder
   */
  public static Builder forSystemProperties() throws IOException {
    return forProvider(new SystemPropertiesCredentialsProvider());
  }
  
  /**
   * Constructs an ElasticContainerRegistryAuthSupplier using the default profile
   * from the system default profile config file.
   * 
   * @see ProfileCredentialsProvider
   * @see Builder
   */
  public static Builder forProfile() throws IOException {
    return forProfile("default");
  }
  
  /**
   * Constructs an ElasticContainerRegistryAuthSupplier using the given profile
   * from the system default profile config file.
   * 
   * @see ProfileCredentialsProvider
   * @see Builder
   */
  public static Builder forProfile(String profile) throws IOException {
    return forProvider(new ProfileCredentialsProvider(profile));
  }
  
  /**
   * Constructs an ElasticContainerRegistryAuthSupplier using the given credentials.
   * 
   * @see ProfileCredentialsProvider
   * @see Builder
   */
  public static Builder forCredentials(
      String awsTokenId,
      String awsTokenSecret) throws IOException {
    return forCredentials(new BasicAWSCredentials(awsTokenId, awsTokenSecret));
  }
  
  /**
   * Constructs an ElasticContainerRegistryAuthSupplier using the given credentials.
   * 
   * @see ProfileCredentialsProvider
   * @see Builder
   */
  public static Builder forCredentials(AWSCredentials credentials) throws IOException {
    return forProvider(new AWSStaticCredentialsProvider(credentials));
  }
  
  /**
   * Constructs an ElasticContainerRegistryAuthSupplier using the given credentials.
   * 
   * @see ProfileCredentialsProvider
   * @see Builder
   */
  public static Builder forProvider(AWSCredentialsProvider provider) throws IOException {
    return new Builder(provider);
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
    private final AWSCredentialsProvider provider;
    private long minimumExpiryMillis;

    public Builder(final AWSCredentialsProvider provider) {
      this.provider = provider;
      this.minimumExpiryMillis = TimeUnit.MINUTES.toMillis(1L);
    }
    
    public AWSCredentialsProvider getProvider() {
      return provider;
    }
    
    public long getMinimumExpiryMillis() {
      return minimumExpiryMillis;
    }
    
    public Builder withMinimumExpiryMillis(long amount, TimeUnit unit) {
      this.minimumExpiryMillis = TimeUnit.MILLISECONDS.convert(amount, unit);
      return this;
    }

    public ElasticContainerRegistryAuthSupplier build() {
      // log some sort of identifier for the credentials, which requires looking at the
      // instance type
      log.info("Loading credentials from " + provider.getClass().getName());

      return new ElasticContainerRegistryAuthSupplier(getMinimumExpiryMillis(), getProvider());
    }
  }

  private final Clock clock;
  private final long minimumExpiryMillis;
  private final Authenticator.Factory authenticatorFactory;
  
  /**
   * This is the "current" token. In ECR, most of the activity involves doing
   * pushes and pulls from your "default" registry, which is scoped to your
   * account. As a result, we only cache the most recent token. It would not be
   * difficult to expand this to a Guava Cache if needed.
   */
  private Authenticator.Authentication authentication;

  @VisibleForTesting
  ElasticContainerRegistryAuthSupplier(
      final long minimumExpiryMillis,
      final AWSCredentialsProvider provider) {
    this(Clock.SYSTEM, minimumExpiryMillis, new EcrAuthenticatorFactory(provider));
  }


  @VisibleForTesting
  ElasticContainerRegistryAuthSupplier(
      final Clock clock,
      final long minimumExpiryMillis,
      final Authenticator.Factory authenticatorFactory) {
    this.clock = clock;
    this.minimumExpiryMillis = minimumExpiryMillis;
    this.authenticatorFactory = authenticatorFactory;
  }

  /**
   * @param registry ECR registry, e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com. If
   *                 null, this method returns a token for the user's default registry.
   */
  protected RegistryAuth authForRegistry(final String registry) throws DockerException {
    if (registry != null) {
      final String[] registryParts = registry.split("\\.", 6);
      if (registryParts.length < 6) {
        // not an ECR registry
        return null;
      }
    
      // TODO: Validate account ID, region
      // String accountId = registryParts[0];
      String dkr = registryParts[1];
      String ecr = registryParts[2];
      // String region = registryParts[3];
      String amazonaws = registryParts[4];
      String com = registryParts[5];
      if (!dkr.equals("dkr") || !ecr.equals("ecr")
          || !amazonaws.equals("amazonaws") || !com.equals("com")) {
        // not an image on ECR
        return null;
      }
    }

    Authenticator.Authentication result;
    if (authentication != null && covers(authentication, registry) && !expired(authentication)) {
      result = authentication;
    } else {
      result = authentication = authenticate(registry);
    }
    
    return result != null ? result.toRegistryAuth() : null;
  }
  
  @Override
  public RegistryAuth authFor(final String imageName) throws DockerException {
    final String[] imageParts = imageName.split("/", 2);
    if (imageParts.length < 2) {
      // not an image on ECR
      return null;
    }
    
    // Example: 123456789.dkr.ecr.us-east-1.amazonaws.com
    final String registry = imageParts[0];
    
    return authForRegistry(registry);
  }
  
  @Override
  public RegistryAuth authForSwarm() throws DockerException {
    return authForRegistry(null);
  }

  @Override
  public RegistryConfigs authForBuild() throws DockerException {
    Authenticator.Authentication result;
    if (authentication != null && covers(authentication, null) && !expired(authentication)) {
      result = authentication;
    } else {
      result = authentication = authenticate(null);
    }
    return RegistryConfigs.create(
        Collections.singletonMap(result.registry, result.toRegistryAuth()));
  }

  protected Authenticator.Authentication authenticate(String registry) throws DockerException {
    Authenticator.Authentication result;
    try (Authenticator authenticator = getAuthenticatorFactory().getAuthenticator()) {
      result = authenticator.authenticate(registry);
    }
    return result;
  }
  
  protected boolean expired(Authenticator.Authentication authentication) {
    boolean result;
      
    if (authentication.expiration == -1) {
      result = false;
    } else {
      long now = clock.currentTimeMillis();
      long then = now + minimumExpiryMillis;
      if (then > authentication.expiration) {
        result = true;
      } else {
        result = false;
      }
    }
      
    return result;
  }
  
  private Authenticator.Factory getAuthenticatorFactory() {
    return authenticatorFactory;
  }
  
  private static boolean covers(Authenticator.Authentication authentication, String registry) {
    boolean result;
      
    if (authentication.def) {
      result = registry == null || registry.equals(authentication.registry);
    } else {
      result = Objects.equals(registry, authentication.registry);
    }
      
    return result;
  }
}