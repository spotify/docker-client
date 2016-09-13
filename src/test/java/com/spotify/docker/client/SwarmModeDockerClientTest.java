package com.spotify.docker.client;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.Task;

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
        List<Service> services = client.listServices();
        for (Service service : services) {
            System.out.println(service.toString());
        }
    }

    @Test
    public void testListServicesFilterById() throws Exception {
        List<Service> services = client.listServices(
                Service.newCriteriaBuilder().forServiceId("7spwad2wl02cdogg6bylyr90g").build());
        for (Service service : services) {
            System.out.println(service.toString());
        }
    }

    @Test
    public void testListServicesFilterByName() throws Exception {
        List<Service> services =
                client.listServices(Service.newCriteriaBuilder().forServiceName("ping00").build());
        for (Service service : services) {
            System.out.println(service.toString());
        }
    }

    @Test
    public void testListTasks() throws Exception {
        List<Task> tasks = client.listTasks();
        for (Task task : tasks) {
            System.out.println(task.toString());
        }
    }
}
