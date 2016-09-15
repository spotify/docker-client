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
package com.spotify.docker.client;

import java.util.List;
import java.util.logging.Logger;

import org.glassfish.jersey.filter.LoggingFilter;

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
import com.spotify.docker.client.messages.swarm.Swarm;
import com.spotify.docker.client.messages.swarm.Task;
import com.spotify.docker.client.messages.swarm.TaskSpec;
import com.spotify.docker.client.messages.swarm.TaskStatus;

public class SwarmModeDockerClientTest {

    /** Client */
    DefaultSwarmModeDockerClient client;

    /** Docker version */
    String version;

    public void setup() throws Exception {
        final DefaultSwarmModeDockerClient.Builder builder = DefaultSwarmModeDockerClient.builder();

        builder.uri("http://192.168.171.135:2375");
        this.client = builder.build();
        final Logger logger = Logger.getLogger(SwarmModeDockerClientTest.class.getName());
        client.getClient().register(new LoggingFilter(logger, true));

        this.version = client.version().apiVersion();

        System.out.printf("Connected to Docker API version %s\n", version);
    }

    public void testInspectSwarm() throws Exception {
        final Swarm swarm = client.inspectSwarm();
        System.out.println(swarm.toString());
    }

    public void testCreateService() throws Exception {
        final ServiceSpec spec = ServiceSpec.builder().withName("ping00").withTaskTemplate(TaskSpec
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
        final ServiceCreateResponse response =
                client.createService(spec, new ServiceCreateOptions());
        System.out.println("Started service: " + response.id());
    }

    public void testInspectService() throws Exception {
        final Service service = client.inspectService("6k8oteesq47dzkei1s2d0f061");
        System.out.println(service.toString());
    }

    public void testUpdateService() throws Exception {
        client.updateService("6k8oteesq47dzkei1s2d0f061", "851",
                ServiceSpec.builder().withServiceMode(ServiceMode.withReplicas(5)).build());
        System.out.println("Successfully updated service.");
    }

    public void testListServices() throws Exception {
        final List<Service> services = client.listServices();
        for (final Service service : services) {
            System.out.println(service.toString());
        }
    }

    public void testListServicesFilterById() throws Exception {
        final List<Service> services = client
                .listServices(Service.find().withServiceId("7spwad2wl02cdogg6bylyr90g").build());
        for (final Service service : services) {
            System.out.println(service.toString());
        }
    }

    public void testListServicesFilterByName() throws Exception {
        final List<Service> services =
                client.listServices(Service.find().withServiceName("ping00").build());
        for (final Service service : services) {
            System.out.println(service.toString());
        }
    }

    public void testRemoveService() throws Exception {
        final List<Service> services = client.listServices();
        if (services.size() > 0) {
            client.removeService(services.get(0).id());
            System.out.println("Removed service: " + services.get(0).spec().name());
        }
    }

    public void testInspectTask() throws Exception {
        final Task task = client.inspectTask("1dzn0uomkbdv81xybcyev5cyt");
        System.out.println(task.toString());
    }

    public void testListTasks() throws Exception {
        final List<Task> tasks = client.listTasks();
        for (final Task task : tasks) {
            System.out.println(task.toString());
        }
    }

    public void testListTaskWithId() throws Exception {
        final List<Task> tasks =
                client.listTasks(Task.find().withTaskId("bj8rh5auppiejwi9prvhlcdpa").build());
        for (final Task task : tasks) {
            System.out.println(task.toString());
        }
    }

    public void testListTasksForServiceName() throws Exception {
        final List<Task> tasks = client.listTasks(Task.find().withServiceName("ping00").build());
        for (final Task task : tasks) {
            System.out.println(task.toString());
        }
    }

    public void testListTasksWithDesiredState() throws Exception {
        final List<Task> tasks = client
                .listTasks(Task.find().withDesiredState(TaskStatus.TASK_STATE_SHUTDOWN).build());
        for (final Task task : tasks) {
            System.out.println(task.toString());
        }
    }

    public void testListTasksWithMultipleCriteria() throws Exception {
        final List<Task> tasks = client.listTasks(Task.find().withServiceName("ping00")
                .withDesiredState(TaskStatus.TASK_STATE_RUNNING).build());
        for (final Task task : tasks) {
            System.out.println(task.toString());
        }
    }
}
