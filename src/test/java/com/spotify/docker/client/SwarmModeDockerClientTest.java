package com.spotify.docker.client;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.Task;
import com.spotify.docker.client.messages.swarm.TaskStatus;

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
        List<Service> services = client
                .listServices(Service.find().withServiceId("7spwad2wl02cdogg6bylyr90g").build());
        for (Service service : services) {
            System.out.println(service.toString());
        }
    }

    @Test
    public void testListServicesFilterByName() throws Exception {
        List<Service> services =
                client.listServices(Service.find().withServiceName("ping00").build());
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

    @Test
    public void testListTaskWithId() throws Exception {
        List<Task> tasks =
                client.listTasks(Task.find().withTaskId("bj8rh5auppiejwi9prvhlcdpa").build());
        for (Task task : tasks) {
            System.out.println(task.toString());
        }
    }

    @Test
    public void testListTasksForServiceName() throws Exception {
        List<Task> tasks = client.listTasks(Task.find().withServiceName("ping00").build());
        for (Task task : tasks) {
            System.out.println(task.toString());
        }
    }

    @Test
    public void testListTasksWithDesiredState() throws Exception {
        List<Task> tasks = client
                .listTasks(Task.find().withDesiredState(TaskStatus.TASK_STATE_SHUTDOWN).build());
        for (Task task : tasks) {
            System.out.println(task.toString());
        }
    }

    @Test
    public void testListTasksWithMultipleCriteria() throws Exception {
        List<Task> tasks = client.listTasks(Task.find().withServiceName("ping00")
                .withDesiredState(TaskStatus.TASK_STATE_RUNNING).build());
        for (Task task : tasks) {
            System.out.println(task.toString());
        }
    }
}
