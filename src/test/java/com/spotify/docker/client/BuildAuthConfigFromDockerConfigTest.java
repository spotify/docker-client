package com.spotify.docker.client;

import com.google.common.io.Resources;
import com.spotify.docker.client.messages.AuthConfig;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BuildAuthConfigFromDockerConfigTest {

    @Test
    public void testFromFullConfig() {
        AuthConfig authConfig = AuthConfig.builder().fromDockerConfig(Resources.getResource("dockerConfig/fullConfig.json").getPath()).build();
        assertEquals("https://index.docker.io/v1/", authConfig.serverAddress());
        assertEquals("dockerman", authConfig.username());
        assertEquals("sw4gy0lo", authConfig.password());
        assertEquals("dockerman@hub.com", authConfig.email());
    }

    @Test
    public void testFromFullDockerCFG(){
        AuthConfig authConfig = AuthConfig.builder().fromDockerConfig(Resources.getResource("dockerConfig/fulldockercfg").getPath()).build();
        assertEquals("https://index.docker.io/v1/", authConfig.serverAddress());
        assertEquals("dockerman", authConfig.username());
        assertEquals("sw4gy0lo", authConfig.password());
        assertEquals("dockerman@hub.com", authConfig.email());


    }

    @Test
    public void testFromIncompleteConfig() {
        AuthConfig authConfig = AuthConfig.builder().fromDockerConfig(Resources.getResource("dockerConfig/incompleteConfig.json").getPath()).build();
        assertNull(authConfig.password());
        assertNull(authConfig.username());
        assertEquals("https://different.docker.io/v1/", authConfig.serverAddress());
        assertEquals("dockerman@hub.com", authConfig.email());
    }

    @Test
    public void testromWrongConfig() {
        AuthConfig authConfig = AuthConfig.builder().fromDockerConfig(Resources.getResource("dockerConfig/wrongConfig.json").getPath()).build();
        assertEquals("wrong", authConfig.serverAddress());
        assertNull(authConfig.password());
        assertNull(authConfig.username());
        assertNull(authConfig.email());
    }

    @Test
    public void testFromMissingConfig() {
        String randomFileName = RandomStringUtils.randomAlphanumeric(16) + ".json";
        AuthConfig authConfig = AuthConfig.builder().fromDockerConfig(randomFileName).build();
        assertEquals("https://index.docker.io/v1/", authConfig.serverAddress());
        assertNull(authConfig.password());
        assertNull(authConfig.username());
        assertNull(authConfig.email());
    }

}