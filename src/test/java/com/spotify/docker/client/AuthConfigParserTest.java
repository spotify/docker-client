package com.spotify.docker.client;

import com.google.common.io.Resources;
import com.spotify.docker.client.messages.AuthConfig;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuthConfigParserTest {

    AuthConfig fullConfig;
    AuthConfig defaultConfig;

    @Before
    public void setup() {
        fullConfig = AuthConfig.builder()
                .serverAddress("https://index.docker.io/v1/")
                .username("dockerman")
                .password("sw4gy0lo")
                .email("dockerman@hub.com")
                .build();
        defaultConfig = AuthConfig.builder().build();
    }

    @Test
    public void testFromFullConfig() {
        AuthConfig authConfig = new AuthConfigParser(Resources.getResource("dockerConfig/fullConfig.json").getPath()).getBuilder().build();
        assertEquals(authConfig, fullConfig);
    }

    @Test
    public void testFromFullDockerCFG() {
        AuthConfig authConfig = new AuthConfigParser(Resources.getResource("dockerConfig/fulldockercfg").getPath()).getBuilder().build();
        assertEquals(authConfig, fullConfig);
    }

    @Test
    public void testFromIncompleteConfig() {
        AuthConfig authConfig = new AuthConfigParser(Resources.getResource("dockerConfig/incompleteConfig.json").getPath()).getBuilder().build();
        assertEquals(defaultConfig, authConfig);
    }

    @Test
    public void testWrongConfig() {
        AuthConfig authConfig = new AuthConfigParser(Resources.getResource("dockerConfig/wrongConfig.json").getPath()).getBuilder().build();
        assertEquals(defaultConfig, authConfig);

    }

    @Test
    public void testFromMissingConfig() {
        String randomFileName = RandomStringUtils.randomAlphanumeric(16) + ".json";
        AuthConfig authConfig = new AuthConfigParser(randomFileName).getBuilder().build();
        assertEquals(defaultConfig, authConfig);
    }
}