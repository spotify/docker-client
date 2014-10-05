package com.spotify.docker.client.messages;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ContainerStateTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ObjectMapper objectMapper;

  @Before
  public void setUp() throws Exception {
    objectMapper = new ObjectMapper();
  }

  @Test
  public void testLoadFromRandomFixture() throws Exception {
    ContainerState containerState = objectMapper
        .readValue(fixture("fixtures/container-state-random.json"), ContainerState.class);
    assertThat(containerState.paused(), is(false));
    assertThat(containerState.restarting(), is(false));
    assertThat(containerState.running(), is(true));
    assertThat(containerState.exitCode(), is(0));
    assertThat(containerState.pid(), is(27629));
    assertThat(containerState.startedAt(), is("2014-10-02T07:59:58.929064724Z"));
    assertThat(containerState.finishedAt(), is("0001-01-01T00:00:00Z"));

  }

  @Test
  public void testLoadFromRandomFixtureMissingProperty() throws Exception {
    ContainerState containerState = objectMapper
        .readValue(fixture("fixtures/container-state-missing-property.json"), ContainerState.class);
  }
  @Test
  public void testLoadInvalidConatainerStateJson() throws Exception {
    expectedException.expect(JsonMappingException.class);
    objectMapper.readValue(fixture("fixtures/container-state-invalid.json"), ContainerState.class);

  }

  @Test
  public void testLoadInvalidJson() throws Exception {
    expectedException.expect(JsonParseException.class);
    objectMapper.readValue(fixture("fixtures/invalid.json"), ContainerState.class);

  }

  private static String fixture(String filename) throws IOException {
    return Resources.toString(Resources.getResource(filename), Charsets.UTF_8).trim();
  }
}