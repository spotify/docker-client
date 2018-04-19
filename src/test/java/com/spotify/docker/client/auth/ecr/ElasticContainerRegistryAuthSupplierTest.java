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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.util.Clock;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ElasticContainerRegistryAuthSupplierTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final int minimumExpirationSecs = 30;
  private final DateTime now = new DateTime(2000, 1, 1, 12, 0, 0);
  private final DateTime expiration = now.plusSeconds(2 * minimumExpirationSecs);

  private final Clock clock = mock(Clock.class);
  
  private final Authenticator.Factory authenticatorFactory = mock(Authenticator.Factory.class);

  private final ElasticContainerRegistryAuthSupplier supplier =
      new ElasticContainerRegistryAuthSupplier(
          clock,
          TimeUnit.SECONDS.toMillis(minimumExpirationSecs),
          authenticatorFactory);

  public static final String REGISTRY1 =
      "123456789012.dkr.ecr.us-east-1.amazonaws.com";

  public static final String IMAGE1 =
      "123456789012.dkr.ecr.us-east-1.amazonaws.com/foo/barticus:latest";

  @Test
  public void testAuthForImage_NoRefresh() throws Exception {
    Authenticator.Authentication authentication = new Authenticator.Authentication(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        REGISTRY1,
        expiration.getMillis(),
        false);
    
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(REGISTRY1))
        .thenReturn(authentication);
      
    when(authenticatorFactory.getAuthenticator())
        .thenReturn(authenticator);
      
    when(clock.currentTimeMillis())
        .thenReturn(now.plusSeconds(minimumExpirationSecs - 1).getMillis());

    RegistryAuth auth = authentication.toRegistryAuth();
    
    assertThat(supplier.authFor(IMAGE1), is(auth));

    assertThat(supplier.authFor(IMAGE1), is(auth));

    verify(authenticator, times(1)).authenticate(REGISTRY1);
  }

  @Test
  public void testAuthForImage_RefreshNeeded() throws Exception {
    Authenticator.Authentication authentication = new Authenticator.Authentication(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        REGISTRY1,
        expiration.getMillis(),
        false);
      
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(REGISTRY1))
      .thenReturn(authentication);
        
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);
        
    when(clock.currentTimeMillis())
      .thenReturn(now.plusSeconds(minimumExpirationSecs + 1).getMillis());

    RegistryAuth auth = authentication.toRegistryAuth();
      
    assertThat(supplier.authFor(IMAGE1), is(auth));

    assertThat(supplier.authFor(IMAGE1), is(auth));

    verify(authenticator, times(2)).authenticate(REGISTRY1);
  }

  @Test
  public void testAuthForImage_TokenExpired() throws Exception {
    Authenticator.Authentication authentication = new Authenticator.Authentication(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        REGISTRY1,
        expiration.getMillis(),
        false);
      
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(REGISTRY1))
      .thenReturn(authentication);
        
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);
        
    when(clock.currentTimeMillis())
      .thenReturn(expiration.plusSeconds(minimumExpirationSecs + 1).getMillis());

    RegistryAuth auth = authentication.toRegistryAuth();
      
    assertThat(supplier.authFor(IMAGE1), is(auth));

    assertThat(supplier.authFor(IMAGE1), is(auth));

    verify(authenticator, times(2)).authenticate(REGISTRY1);
  }

  @Test
  public void testAuthForImage_NonEcrImage() throws Exception {
    when(clock.currentTimeMillis())
      .thenReturn(expiration.minusSeconds(minimumExpirationSecs + 1).getMillis());

    assertThat(supplier.authFor("foobar"), is(nullValue()));
  }

  @Test
  public void testAuthForImage_ExceptionOnRefresh() throws Exception {
    final DockerException ex = new DockerException("failure!!");
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(REGISTRY1))
      .thenThrow(ex);
        
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);

    // the exception should propagate up
    exception.expect(DockerException.class);

    supplier.authFor(IMAGE1);
  }

  @Test
  public void testAuthForImage_TokenWithoutExpirationDoesNotCauseRefresh() throws Exception {
    Authenticator.Authentication authentication = new Authenticator.Authentication(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        REGISTRY1,
        -1L,
        false);
      
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(REGISTRY1))
      .thenReturn(authentication);
        
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);
        
    when(clock.currentTimeMillis())
      .thenReturn(expiration.plusSeconds(minimumExpirationSecs + 1).getMillis());

    RegistryAuth auth = authentication.toRegistryAuth();
      
    assertThat(supplier.authFor(IMAGE1), is(auth));

    assertThat(supplier.authFor(IMAGE1), is(auth));

    verify(authenticator, times(1)).authenticate(REGISTRY1);
  }

  @Test
  public void testAuthForSwarm_NoRefresh() throws Exception {
    Authenticator.Authentication authentication = new Authenticator.Authentication(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        REGISTRY1,
        expiration.getMillis(),
        true);
      
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(null))
      .thenReturn(authentication);
        
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);
        
    when(clock.currentTimeMillis())
      .thenReturn(now.plusSeconds(minimumExpirationSecs - 1).getMillis());

    RegistryAuth auth = authentication.toRegistryAuth();
      
    assertThat(supplier.authForSwarm(), is(auth));

    assertThat(supplier.authForSwarm(), is(auth));

    verify(authenticator, times(1)).authenticate(null);
  }

  @Test
  public void testAuthForSwarm_RefreshNeeded() throws Exception {
    Authenticator.Authentication authentication = new Authenticator.Authentication(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        REGISTRY1,
        expiration.getMillis(),
        true);
      
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(null))
      .thenReturn(authentication);
        
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);
        
    when(clock.currentTimeMillis())
      .thenReturn(now.plusSeconds(minimumExpirationSecs + 1).getMillis());

    RegistryAuth auth = authentication.toRegistryAuth();
      
    assertThat(supplier.authForSwarm(), is(auth));

    assertThat(supplier.authForSwarm(), is(auth));

    verify(authenticator, times(2)).authenticate(null);
  }

  @Test
  public void testAuthForSwarm_ExceptionOnRefresh() throws Exception {
    final DockerException ex = new DockerException("failure!!");
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(null))
        .thenThrow(ex);
        
    when(authenticatorFactory.getAuthenticator())
        .thenReturn(authenticator);

    // the exception should propagate up
    exception.expect(DockerException.class);

    supplier.authForSwarm();
  }

  @Test
  public void testAuthForSwarm_TokenWithoutExpirationDoesNotCauseRefresh() throws Exception {
    Authenticator.Authentication authentication = new Authenticator.Authentication(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        REGISTRY1,
        -1L,
        true);
        
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(null))
      .thenReturn(authentication);
          
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);
          
    when(clock.currentTimeMillis())
      .thenReturn(expiration.plusSeconds(minimumExpirationSecs + 1).getMillis());

    RegistryAuth auth = authentication.toRegistryAuth();
        
    assertThat(supplier.authForSwarm(), is(auth));

    assertThat(supplier.authForSwarm(), is(auth));

    verify(authenticator, times(1)).authenticate(null);
  }

  @Test
  public void testAuthForBuild_NoRefresh() throws Exception {
    Authenticator.Authentication authentication = new Authenticator.Authentication(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        REGISTRY1,
        expiration.getMillis(),
        true);
        
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(null))
      .thenReturn(authentication);
          
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);
          
    when(clock.currentTimeMillis())
      .thenReturn(now.plusSeconds(minimumExpirationSecs - 1).getMillis());

    RegistryAuth auth = authentication.toRegistryAuth();
      
    RegistryConfigs configs1 = supplier.authForBuild();
    assertThat(configs1.configs().values(), is(not(empty())));
    assertThat(configs1.configs().values(), everyItem(is(auth)));
    
    RegistryConfigs configs2 = supplier.authForBuild();
    assertThat(configs2.configs().values(), is(not(empty())));
    assertThat(configs2.configs().values(), everyItem(is(auth)));
      
    verify(authenticator, times(1)).authenticate(null);
  }

  @Test
  public void testAuthForBuild_RefreshNeeded() throws Exception {
    Authenticator.Authentication authentication = new Authenticator.Authentication(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        REGISTRY1,
        expiration.getMillis(),
        true);
          
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(null))
      .thenReturn(authentication);
            
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);
            
    when(clock.currentTimeMillis())
      .thenReturn(now.plusSeconds(minimumExpirationSecs + 1).getMillis());

    RegistryAuth auth = authentication.toRegistryAuth();
        
    RegistryConfigs configs1 = supplier.authForBuild();
    assertThat(configs1.configs().values(), is(not(empty())));
    assertThat(configs1.configs().values(), everyItem(is(auth)));
      
    RegistryConfigs configs2 = supplier.authForBuild();
    assertThat(configs2.configs().values(), is(not(empty())));
    assertThat(configs2.configs().values(), everyItem(is(auth)));
        
    verify(authenticator, times(2)).authenticate(null);
  }

  @Test
  public void testAuthForBuild_ExceptionOnRefresh() throws Exception {
    final DockerException ex = new DockerException("failure!!");
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(null))
      .thenThrow(ex);
          
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);

    // the exception should propagate up
    exception.expect(DockerException.class);

    supplier.authForBuild();
  }

  @Test
  public void testAuthForBuild_TokenWithoutExpirationDoesNotCauseRefresh() throws Exception {
    Authenticator.Authentication authentication = new Authenticator.Authentication(
        UUID.randomUUID().toString(),
        UUID.randomUUID().toString(),
        REGISTRY1,
        -1L,
        true);
          
    Authenticator authenticator = mock(Authenticator.class);
    when(authenticator.authenticate(null))
      .thenReturn(authentication);
            
    when(authenticatorFactory.getAuthenticator())
      .thenReturn(authenticator);
            
    when(clock.currentTimeMillis())
      .thenReturn(expiration.plusSeconds(minimumExpirationSecs + 1).getMillis());

    RegistryAuth auth = authentication.toRegistryAuth();
          
    RegistryConfigs configs1 = supplier.authForBuild();
    assertThat(configs1.configs().values(), is(not(empty())));
    assertThat(configs1.configs().values(), everyItem(is(auth)));
    
    RegistryConfigs configs2 = supplier.authForBuild();
    assertThat(configs2.configs().values(), is(not(empty())));
    assertThat(configs2.configs().values(), everyItem(is(auth)));
      
    verify(authenticator, times(1)).authenticate(null);
  }
}
