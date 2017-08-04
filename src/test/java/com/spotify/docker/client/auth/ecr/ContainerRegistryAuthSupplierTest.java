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

import static com.amazonaws.util.Base64.encode;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.ecr.AmazonECR;
import com.amazonaws.services.ecr.model.AuthorizationData;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenRequest;
import com.amazonaws.services.ecr.model.GetAuthorizationTokenResult;
import com.amazonaws.util.Base64;
import com.google.api.client.util.Clock;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import java.io.IOException;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Matchers;

public class ContainerRegistryAuthSupplierTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private final DateTime expiration = new DateTime(2017, 5, 23, 16, 25);
  private final String tokenValue = new String(encode("test-username:test-password".getBytes()));

  private AmazonECR ecrClient = mock(AmazonECR.class);

  private final AuthorizationData authData1 = new AuthorizationData()
      .withAuthorizationToken(tokenValue).withExpiresAt(expiration.toDate())
      .withProxyEndpoint("http://proxy");

  private final Clock clock = mock(Clock.class);
  private final int minimumExpirationSecs = 30;
  private final EcrCredentials credentials = spy(new EcrCredentials(ecrClient, authData1));
  private final ContainerRegistryAuthSupplier supplier = spy(new ContainerRegistryAuthSupplier(
      ecrClient, clock, minimumExpirationSecs, credentials));

  private static Matcher<RegistryAuth> matchesAccessToken(final AuthorizationData accessToken) {
    String decoded = new String(Base64.decode(accessToken.getAuthorizationToken()));
    final String username = decoded.split(":")[0];
    final String password = decoded.split(":")[1];

    final Matcher<RegistryAuth> usernameMatcher = new FeatureMatcher<RegistryAuth, String>(
        is(username), "username", "username") {
      @Override
      protected String featureValueOf(final RegistryAuth actual) {
        return actual.username();
      }
    };

    final Matcher<RegistryAuth> passwordMatcher = new FeatureMatcher<RegistryAuth, String>(
        is(password), "password", "password") {
      @Override
      protected String featureValueOf(final RegistryAuth actual) {
        return actual.password();
      }
    };

    return allOf(usernameMatcher, passwordMatcher);
  }

  @Before
  public void before() {
    GetAuthorizationTokenResult res = new GetAuthorizationTokenResult()
        .withAuthorizationData(authData1);
    doReturn(res).when(ecrClient).getAuthorizationToken(
        Matchers.any(GetAuthorizationTokenRequest.class));
  }

  @Test
  public void testAuthForImage_NoRefresh() throws Exception {
    when(clock.currentTimeMillis()).thenReturn(
        expiration.minusSeconds(minimumExpirationSecs + 1).getMillis());

    assertThat(
        supplier.authFor("1234567890.dkr.ecr.eu-west-1.amazonaws.com/foobar/barfoo:latest"),
        matchesAccessToken(authData1));

    verify(credentials, never()).refresh();
  }

  @Test
  public void testAuthForImage_RefreshNeeded() throws Exception {
    doReturn(true).when(supplier).needsRefresh(Matchers.any(AuthorizationData.class));

    assertThat(
        supplier.authFor("1234567890.dkr.ecr.eu-west-1.amazonaws.com/foobar/barfoo:latest"),
        matchesAccessToken(authData1));

    verify(credentials).refresh();
  }

  @Test
  public void testAuthForImage_TokenExpired() throws Exception {
    doReturn(true).when(supplier).needsRefresh(Matchers.any(AuthorizationData.class));

    assertThat(
        supplier.authFor("1234567890.dkr.ecr.eu-west-1.amazonaws.com/foobar/barfoo:latest"),
        matchesAccessToken(authData1));

    verify(credentials).refresh();
  }

  @Test
  public void testAuthForImage_NonEcrImage() throws Exception {
    assertThat(supplier.authFor("foobar"), is(nullValue()));
  }

  @Test
  public void testAuthForImage_ExceptionOnRefresh() throws Exception {
    doReturn(true).when(supplier).needsRefresh(Matchers.any(AuthorizationData.class));

    final IOException ex = new IOException("failure!!");
    doThrow(ex).when(credentials).refresh();

    // the exception should propagate up
    exception.expect(DockerException.class);
    exception.expectCause(is(ex));

    supplier.authFor("1234567890.dkr.ecr.eu-west-1.amazonaws.com/example/foobar:1.2.3");
  }

  @Test
  public void testAuthForSwarm_NoRefresh() throws Exception {
    doReturn(false).when(supplier).needsRefresh(Matchers.any(AuthorizationData.class));

    assertThat(supplier.authForSwarm(), matchesAccessToken(authData1));

    verify(credentials, never()).refresh();
  }

  @Test
  public void testAuthForSwarm_RefreshNeeded() throws Exception {
    doReturn(true).when(supplier).needsRefresh(Matchers.any(AuthorizationData.class));

    assertThat(supplier.authForSwarm(), matchesAccessToken(authData1));

    verify(credentials).refresh();
  }

  @Test
  public void testAuthForSwarm_ExceptionOnRefresh() throws Exception {
    doReturn(true).when(supplier).needsRefresh(Matchers.any(AuthorizationData.class));

    doThrow(new IOException("failure!!")).when(credentials).refresh();

    assertThat(supplier.authForSwarm(), is(nullValue()));
  }

  @Test
  public void testAuthForBuild_NoRefresh() throws Exception {
    doReturn(false).when(supplier).needsRefresh(Matchers.any(AuthorizationData.class));

    final RegistryConfigs configs = supplier.authForBuild();
    assertThat(configs.configs().values(), is(not(empty())));
    assertThat(configs.configs().values(), everyItem(matchesAccessToken(authData1)));

    verify(credentials, never()).refresh();
  }

  @Test
  public void testAuthForBuild_RefreshNeeded() throws Exception {
    doReturn(true).when(supplier).needsRefresh(Matchers.any(AuthorizationData.class));

    final RegistryConfigs configs = supplier.authForBuild();
    assertThat(configs.configs().values(), is(not(empty())));
    assertThat(configs.configs().values(), everyItem(matchesAccessToken(authData1)));

    verify(credentials).refresh();
  }

  @Test
  public void testAuthForBuild_ExceptionOnRefresh() throws Exception {
    doReturn(true).when(supplier).needsRefresh(Matchers.any(AuthorizationData.class));

    doThrow(new IOException("failure!!")).when(credentials).refresh();

    final RegistryConfigs configs = supplier.authForBuild();
    assertThat(configs.configs().values(), is(empty()));
  }

}
