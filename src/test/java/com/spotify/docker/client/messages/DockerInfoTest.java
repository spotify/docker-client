package com.spotify.docker.client.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.NullValue;
import com.spotify.docker.client.ObjectMapperProvider;
import org.junit.Test;

import static com.spotify.docker.FixtureUtil.fixture;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Test cases around the deserialization of the docker info object.
 */
public class DockerInfoTest {

    private final ObjectMapper objectMapper = ObjectMapperProvider.objectMapper();

    /**
     * Test that when we deserialize the docker info response we properly parse the Plugins.Network json path.
     * @throws Exception when we fail to deserialize
     */
    @Test
    public void dockerInfoNetworkDesirializerTest_1_23() throws Exception {
        Info info = objectMapper.readValue(fixture("fixtures/1.23/docker_info.json"), Info.class);
        assertThat(info.plugins(), is(not(nullValue())));
        assertThat(info.plugins().networks(), is(not(nullValue())));
        assertThat(info.plugins().networks().size() , is(greaterThan(0)));
    }
}
