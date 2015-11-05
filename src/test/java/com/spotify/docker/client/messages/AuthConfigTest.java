package com.spotify.docker.client.messages;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class AuthConfigTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void verifyBuilder() throws Exception {
    final AuthConfig.Builder builder = AuthConfig.builder();

    // Input to setXXX
    final String username = "username";
    final String password = "password";
    final String email = "email";
    final String serverAddress = "serverAddress";

    // Check setXXX methods
    builder.username(username);
    builder.password(password);
    builder.email(email);
    builder.serverAddress(serverAddress);
    assertEquals("username", username, builder.username());
    assertEquals("password", password, builder.password());
    assertEquals("email", email, builder.email());
    assertEquals("serverAddress", serverAddress, builder.serverAddress());

    // Check final output
    final AuthConfig authConfig = builder.build();
    assertEquals("username", username, authConfig.username());
    assertEquals("password", password, authConfig.password());
    assertEquals("email", email, authConfig.email());
    assertEquals("serverAddress", serverAddress, authConfig.serverAddress());

    // Check toBuilder
    final AuthConfig.Builder rebuilder = authConfig.toBuilder();
    assertEquals("username", username, rebuilder.username());
    assertEquals("password", password, rebuilder.password());
    assertEquals("email", email, rebuilder.email());
    assertEquals("serverAddress", serverAddress, rebuilder.serverAddress());
  }

  @Test
  public void testDefaultServerAddress() throws Exception {
    final AuthConfig.Builder builder = AuthConfig.builder();
    assertThat(builder.serverAddress(), equalTo("https://index.docker.io/v1/"));
  }
}
