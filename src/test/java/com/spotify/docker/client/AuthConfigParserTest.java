package com.spotify.docker.client;

import com.google.common.io.Resources;
import com.spotify.docker.client.messages.AuthConfig;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuthConfigParserTest {

    AuthConfig dockerIo;
    AuthConfig myDock;
    AuthConfig defaultConfig;

    @Before
    public void setup() {
        dockerIo = AuthConfig.builder()
                .serverAddress("https://index.docker.io/v1/")
                .username("dockerman")
                .password("sw4gy0lo")
                .email("dockerman@hub.com")
                .build();
        myDock = AuthConfig.builder()
                .serverAddress("https://narnia.mydock.io/v1/")
                .username("megaman")
                .password("riffraff")
                .email("megaman@mydock.com")
                .build();
        defaultConfig = AuthConfig.builder().build();
    }

    @Test
    public void testFromFullConfig() {
        AuthConfig authConfig = new AuthConfigParser(getTestFile("dockerConfig/fullConfig.json"))
                .getBuilder().build();
        assertEquals(authConfig, dockerIo);
    }

    @Test
    public void testFromFullDockerCFG() {
        AuthConfig authConfig = new AuthConfigParser(getTestFile("dockerConfig/fulldockercfg"))
                .getBuilder().build();
        assertEquals(authConfig, dockerIo);
    }

    @Test
    public void testFromIncompleteConfig() {
        AuthConfig authConfig = new AuthConfigParser(getTestFile("dockerConfig/incompleteConfig.json"))
                .getBuilder().build();
        assertEquals(defaultConfig, authConfig);
    }

    @Test
    public void testWrongConfig() {
        AuthConfig authConfig = new AuthConfigParser(getTestFile("dockerConfig/wrongConfig.json"))
                .getBuilder().build();
        assertEquals(defaultConfig, authConfig);

    }

    @Test
    public void testFromMissingConfig() {
        String randomFileName = RandomStringUtils.randomAlphanumeric(16) + ".json";
        AuthConfig authConfig = new AuthConfigParser(randomFileName).getBuilder().build();
        assertEquals(defaultConfig, authConfig);
    }

    @Test
    public void testGettingMultiConfig(){
        AuthConfig myDockParsed = new AuthConfigParser(getTestFile("dockerConfig/multiConfig.json"))
                .getBuilderFor("https://narnia.mydock.io/v1/").build();
        assertEquals(myDock, myDockParsed);
        AuthConfig dockerIoParsed = new AuthConfigParser(getTestFile("dockerConfig/multiConfig.json"))
                .getBuilderFor("https://index.docker.io/v1/").build();
        assertEquals(dockerIo, dockerIoParsed);
    }

    private String getTestFile(String path) {
        return Resources.getResource(path).getPath();
    }
}