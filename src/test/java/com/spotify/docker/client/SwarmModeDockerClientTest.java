package com.spotify.docker.client;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.messages.ServiceListOptions;
import com.spotify.docker.client.messages.swarm.Service;

public class SwarmModeDockerClientTest {

    /** Client */
    DefaultSwarmModeDockerClient client;

    /** Docker version */
    String version;

    @Before
    public void setup() throws Exception {
        final DefaultSwarmModeDockerClient.Builder builder = DefaultSwarmModeDockerClient.builder();

        builder.uri("http://192.168.171.135:2375");
        this.client = builder.build();
        this.version = client.version().apiVersion();

        System.out.printf("Connected to Docker version %s\n", version);
    }

    @Test
    public void testListServices() throws Exception {
        List<Service> services = client.listServices(new ServiceListOptions());
        for (Service service : services) {
            System.out.println("Service:\n\n" + service.toString() + "\n\n");
        }
    }
}
