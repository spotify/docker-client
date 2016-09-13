package com.spotify.docker.client;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.spotify.docker.client.messages.ServiceCreateOptions;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.ContainerSpec;
import com.spotify.docker.client.messages.swarm.Driver;
import com.spotify.docker.client.messages.swarm.EndpointSpec;
import com.spotify.docker.client.messages.swarm.PortConfig;
import com.spotify.docker.client.messages.swarm.ResourceRequirements;
import com.spotify.docker.client.messages.swarm.Resources;
import com.spotify.docker.client.messages.swarm.RestartPolicy;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.ServiceMode;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.Task;
import com.spotify.docker.client.messages.swarm.TaskSpec;
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
    public void testCreateService() throws Exception {
        ServiceSpec spec = ServiceSpec.builder().withName("ping00").withTaskTemplate(TaskSpec
                .builder()
                .withContainerSpec(ContainerSpec.builder().withImage("alpine")
                        .withCommands(new String[] {"ping", "192.168.171.135"}).build())
                .withLogDriver(Driver.builder().withName("json-file").withOption("max-file", "3")
                        .withOption("max-size", "10M").build())
                .withResources(ResourceRequirements.builder()
                        .withLimits(Resources.builder().withMemoryBytes(10 * 1024 * 1024).build())
                        .build())
                .withRestartPolicy(RestartPolicy.builder().withCondition("on-failure")
                        .withDelay(10000000).withMaxAttempts(10).build())
                .build())
                .withServiceMode(ServiceMode.withReplicas(4))
                .withEndpointSpec(
                        EndpointSpec.builder()
                                .withPorts(new PortConfig[] {PortConfig.builder().withName("web")
                                        .withProtocol("tcp").withPublishedPort(8080)
                                        .withTargetPort(80).build()})
                                .build())
                .build();
        ServiceCreateResponse response = client.createService(spec, new ServiceCreateOptions());
        System.out.println("Started service: " + response.id());
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
    public void testRemoveService() throws Exception {
        List<Service> services = client.listServices();
        if (services.size() > 0) {
            client.removeService(services.get(0).id());
            System.out.println("Removed service: " + services.get(0).spec().name());
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
