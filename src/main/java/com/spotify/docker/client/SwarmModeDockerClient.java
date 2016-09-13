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

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ServiceCreateOptions;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.Task;

/**
 * Extends standard Docker client with "Swarm Mode" extensions.
 */
public interface SwarmModeDockerClient extends DockerClient {

    /**
     * Create a new service.
     * 
     * @param spec
     * @param options
     * @return
     * @throws DockerException
     * @throws InterruptedException
     */
    ServiceCreateResponse createService(ServiceSpec spec, ServiceCreateOptions options)
            throws DockerException, InterruptedException;

    /**
     * List all services.
     * 
     * @return
     * @throws DockerException
     * @throws InterruptedException
     */
    List<Service> listServices() throws DockerException, InterruptedException;

    /**
     * List services that match the given criteria.
     * 
     * @param criteria
     * @return
     * @throws DockerException
     * @throws InterruptedException
     */
    List<Service> listServices(Service.Criteria criteria)
            throws DockerException, InterruptedException;

    /**
     * Remove an existing service.
     * 
     * @param serviceId
     * @throws DockerException
     * @throws InterruptedException
     */
    void removeService(String serviceId) throws DockerException, InterruptedException;

    /**
     * List all tasks.
     * 
     * @return
     * @throws DockerException
     * @throws InterruptedException
     */
    List<Task> listTasks() throws DockerException, InterruptedException;

    /**
     * List tasks that match the given criteria.
     * 
     * @param criteria
     * @return
     * @throws DockerException
     * @throws InterruptedException
     */
    List<Task> listTasks(Task.Criteria criteria) throws DockerException, InterruptedException;
}
