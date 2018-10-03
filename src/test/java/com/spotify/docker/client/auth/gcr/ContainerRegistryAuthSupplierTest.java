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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.TimeUnit;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ContainerRegistryAuthSupplierTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final DateTime expiration = new DateTime(2017, 5, 23, 16, 25);
  private final String tokenValue = "abc123.foobar";

  // we can't really mock GoogleCredentials since getAccessToken() is a final method (which can't be
  // mocked). We can construct an instance of GoogleCredentials for a made-up accessToken though.
  private final AccessToken accessToken = new AccessToken(tokenValue, expiration.toDate());
  private final GoogleCredentials credentials = new GoogleCredentials(accessToken);

  private final Clock clock = mock(Clock.class);
  private final int minimumExpirationSecs = 30;

  // we wrap the call to GoogleCredentials.refresh() in this interface because the actual
  // implementation in the GoogleCredentials class will throw an exception that it isn't
  // implemented - only subclasses (constructed from real InputStreams containing real credentials)
  // implement that method.
  private final ContainerRegistryAuthSupplier.CredentialRefresher refresher = mock(
      ContainerRegistryAuthSupplier.CredentialRefresher.class);

  private final ContainerRegistryAuthSupplier supplier =
      new ContainerRegistryAuthSupplier(credentials, clock,
          TimeUnit.SECONDS.toMillis(minimumExpirationSecs), refresher);

  private static Matcher<RegistryAuth> matchesAccessToken(final AccessToken accessToken) {
    // we use two featurematchers because a normal CustomTypeSafeMatcher will call
    // RegistryAuth.toString() in building the failure message, and the toString of that class
    // purposefully hides sensitive data like the password.

    // username is always the same
    final String username = "oauth2accesstoken";
    final String password = accessToken.getTokenValue();

    final Matcher<RegistryAuth> usernameMatcher =
        new FeatureMatcher<RegistryAuth, String>(is(username), "username", "username") {
          @Override
          protected String featureValueOf(final RegistryAuth actual) {
            return actual.username();
          }
        };

    final Matcher<RegistryAuth> passwordMatcher =
        new FeatureMatcher<RegistryAuth, String>(is(password), "password", "password") {
          @Override
          protected String featureValueOf(final RegistryAuth actual) {
            return actual.password();
          }
        };

    return allOf(usernameMatcher, passwordMatcher);
  }

  @Test
  public void testAuthForImage_NoRefresh() throws Exception {
    when(clock.millis())
        .thenReturn(expiration.minusSeconds(minimumExpirationSecs + 1).getMillis());

    assertThat(supplier.authFor("gcr.io/foobar/barfoo:latest"), matchesAccessToken(accessToken));

    verify(refresher, never()).refresh(credentials);
  }

  @Test
  public void testAuthForImage_RefreshNeeded() throws Exception {
    when(clock.millis())
        .thenReturn(expiration.minusSeconds(minimumExpirationSecs - 1).getMillis());

    assertThat(supplier.authFor("gcr.io/foobar/barfoo:latest"), matchesAccessToken(accessToken));

    verify(refresher).refresh(credentials);
  }

  @Test
  public void testAuthForImage_TokenExpired() throws Exception {
    when(clock.millis()).thenReturn(expiration.plusMinutes(1).getMillis());

    assertThat(supplier.authFor("gcr.io/foobar/barfoo:latest"), matchesAccessToken(accessToken));

    verify(refresher).refresh(credentials);
  }

  @Test
  public void testAuthForImage_NonGcrImage() throws Exception {
    when(clock.millis())
        .thenReturn(expiration.minusSeconds(minimumExpirationSecs + 1).getMillis());

    assertThat(supplier.authFor("foobar"), is(nullValue()));
  }

  @Test
  public void testAuthForImage_ExceptionOnRefresh() throws Exception {
    when(clock.millis())
        .thenReturn(expiration.minusSeconds(minimumExpirationSecs - 1).getMillis());

    final IOException ex = new IOException("failure!!");
    doThrow(ex).when(refresher).refresh(credentials);

    // the exception should propagate up
    exception.expect(DockerException.class);
    exception.expectCause(is(ex));

    supplier.authFor("gcr.io/example/foobar:1.2.3");
  }

  @Test
  public void testAuthForImage_TokenWithoutExpirationDoesNotCauseRefresh() throws Exception {
    final AccessToken accessToken = new AccessToken(tokenValue, null);
    final GoogleCredentials credentials = new GoogleCredentials(accessToken);

    final ContainerRegistryAuthSupplier supplier =
        new ContainerRegistryAuthSupplier(credentials, clock,
            TimeUnit.SECONDS.toMillis(minimumExpirationSecs), refresher);

    assertThat(supplier.authFor("gcr.io/foobar/barfoo:latest"), matchesAccessToken(accessToken));

    verify(refresher, never()).refresh(credentials);
  }

  @Test
  public void testAuthForSwarm_NoRefresh() throws Exception {
    when(clock.millis())
        .thenReturn(expiration.minusSeconds(minimumExpirationSecs + 1).getMillis());

    assertThat(supplier.authForSwarm(), matchesAccessToken(accessToken));

    verify(refresher, never()).refresh(credentials);
  }

  @Test
  public void testAuthForSwarm_RefreshNeeded() throws Exception {
    when(clock.millis())
        .thenReturn(expiration.minusSeconds(minimumExpirationSecs - 1).getMillis());

    assertThat(supplier.authForSwarm(), matchesAccessToken(accessToken));

    verify(refresher).refresh(credentials);
  }

  @Test
  public void testAuthForSwarm_ExceptionOnRefresh() throws Exception {
    when(clock.millis())
        .thenReturn(expiration.minusSeconds(minimumExpirationSecs - 1).getMillis());

    doThrow(new IOException("failure!!")).when(refresher).refresh(credentials);

    assertThat(supplier.authForSwarm(), is(nullValue()));
  }

  @Test
  public void testAuthForSwarm_TokenWithoutExpirationDoesNotCauseRefresh() throws Exception {
    final AccessToken accessToken = new AccessToken(tokenValue, null);
    final GoogleCredentials credentials = new GoogleCredentials(accessToken);

    final ContainerRegistryAuthSupplier supplier =
        new ContainerRegistryAuthSupplier(credentials, clock,
            TimeUnit.SECONDS.toMillis(minimumExpirationSecs), refresher);

    assertThat(supplier.authForSwarm(), matchesAccessToken(accessToken));

    verify(refresher, never()).refresh(credentials);
  }

  @Test
  public void testAuthForBuild_NoRefresh() throws Exception {
    when(clock.millis())
        .thenReturn(expiration.minusSeconds(minimumExpirationSecs + 1).getMillis());

    final RegistryConfigs configs = supplier.authForBuild();
    assertThat(configs.configs().values(), is(not(empty())));
    assertThat(configs.configs().values(), everyItem(matchesAccessToken(accessToken)));

    verify(refresher, never()).refresh(credentials);
  }

  @Test
  public void testAuthForBuild_RefreshNeeded() throws Exception {
    when(clock.millis())
        .thenReturn(expiration.minusSeconds(minimumExpirationSecs - 1).getMillis());

    final RegistryConfigs configs = supplier.authForBuild();
    assertThat(configs.configs().values(), is(not(empty())));
    assertThat(configs.configs().values(), everyItem(matchesAccessToken(accessToken)));

    verify(refresher).refresh(credentials);
  }

  @Test
  public void testAuthForBuild_ExceptionOnRefresh() throws Exception {
    when(clock.millis())
        .thenReturn(expiration.minusSeconds(minimumExpirationSecs - 1).getMillis());

    doThrow(new IOException("failure!!")).when(refresher).refresh(credentials);

    final RegistryConfigs configs = supplier.authForBuild();
    assertThat(configs.configs().values(), is(empty()));
  }

  @Test
  public void testAuthForBuild_TokenWithoutExpirationDoesNotCauseRefresh() throws Exception {
    final AccessToken accessToken = new AccessToken(tokenValue, null);
    final GoogleCredentials credentials = new GoogleCredentials(accessToken);

    final ContainerRegistryAuthSupplier supplier =
        new ContainerRegistryAuthSupplier(credentials, clock,
            TimeUnit.SECONDS.toMillis(minimumExpirationSecs), refresher);

    final RegistryConfigs configs = supplier.authForBuild();
    assertThat(configs.configs().values(), is(not(empty())));
    assertThat(configs.configs().values(), everyItem(matchesAccessToken(accessToken)));

    verify(refresher, never()).refresh(credentials);
  }
}
