package com.spotify.docker.client.messages;

import static com.spotify.docker.FixtureUtil.fixture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.ObjectMapperProvider;
import org.junit.Test;

/**
 * Test cases around the deserialization of the docker info object.
 */
public class DockerInfoTest {

  private final ObjectMapper objectMapper = ObjectMapperProvider.objectMapper();

  /**
   * Test that when we deserialize the docker info response we properly parse the Plugins.Network
   * json path.
   *
   * @throws Exception when we fail to deserialize
   */
  @Test
  public void dockerInfoNetworkDesirializerTest_1_23() throws Exception {
    Info info = objectMapper.readValue(fixture("fixtures/1.23/docker_info.json"), Info.class);
    assertThat(info.plugins(), is(not(nullValue())));
    assertThat(info.plugins().networks(), is(not(nullValue())));
    assertThat(info.plugins().networks().size(), is(greaterThan(0)));
  }
}
