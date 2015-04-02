package com.spotify.docker.client.messages;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.spotify.docker.client.ObjectMapperProvider;

public class ContainerTest {

	@Rule
	  public ExpectedException expectedException = ExpectedException.none();

	  private ObjectMapper objectMapper;

	  @Before
	  public void setUp() throws Exception {
	    objectMapper = new ObjectMapperProvider().getContext(Container.class);
	  }

	  @Test
	  public void testLoadFromFixture() throws Exception {
	    Container container = objectMapper
	        .readValue(fixture("fixtures/container-ports-as-string.json"), Container.class);
	    assertThat(container.portsAsString(), is("0.0.0.0:80->88/tcp"));  
	  }	 
      
	  private static String fixture(String filename) throws IOException {
          return Resources.toString(Resources.getResource(filename), Charsets.UTF_8).trim();
	  }
}
