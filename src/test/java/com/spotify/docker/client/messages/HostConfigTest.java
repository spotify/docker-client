/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.docker.client.messages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.spotify.docker.client.ObjectMapperProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class HostConfigTest {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapperProvider().getContext(HostConfig.class);
    }

    @Test
    public void testJsonAlways() throws Exception {
        HostConfig hostConfig = objectMapper
                .readValue(fixture("fixtures/hostConfig/restartPolicyAlways.json"),
                        HostConfig.class);
        assertThat(hostConfig.restartPolicy(), is(HostConfig.RestartPolicy.always()));
    }

    @Test
    public void testJsonUnlessStopped() throws Exception {
        HostConfig hostConfig = objectMapper
                .readValue(fixture("fixtures/hostConfig/restartPolicyUnlessStopped.json"),
                        HostConfig.class);
        assertThat(hostConfig.restartPolicy(), is(HostConfig.RestartPolicy.unlessStopped()));
    }

    @Test
    public void testJsonOnFailure() throws Exception {
        HostConfig hostConfig = objectMapper
                .readValue(fixture("fixtures/hostConfig/restartPolicyOnFailure.json"),
                        HostConfig.class);
        assertThat(hostConfig.restartPolicy(), is(HostConfig.RestartPolicy.onFailure(5)));
    }

    private static String fixture(String filename) throws IOException {
        return Resources.toString(Resources.getResource(filename), Charsets.UTF_8).trim();
    }
}
