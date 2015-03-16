package com.spotify.docker.client;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class DefaultDockerClientUnitTest {

  @Test
  public void testHostForUnixSocket() {
    DefaultDockerClient client = DefaultDockerClient.builder()
        .uri(DefaultDockerClient.DEFAULT_UNIX_ENDPOINT).build();
    assertThat(client.getHost(), equalTo("localhost"));
  }
  
  @Test
  public void testHostForLocalHttps() {
    DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://localhost:2375").build();
    assertThat(client.getHost(), equalTo("localhost"));
  }
  
  @Test
  public void testHostForFQDNHttps() {
    DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://perdu.com:2375").build();
    assertThat(client.getHost(), equalTo("perdu.com"));
  }
  
  @Test
  public void testHostForIPHttps() {
    DefaultDockerClient client = DefaultDockerClient.builder()
        .uri("https://192.168.53.103:2375").build();
    assertThat(client.getHost(), equalTo("192.168.53.103"));
  }
}
