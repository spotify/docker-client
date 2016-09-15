/*
 * Copyright (c) 2014 Spotify AB.
 * Copyright (c) 2014 Oleg Poleshuk.
 * Copyright (c) 2014 CyDesign Ltd.
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

import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;

import com.google.common.base.Supplier;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import com.spotify.docker.client.exceptions.TaskNotFoundException;
import com.spotify.docker.client.messages.ServiceCreateOptions;
import com.spotify.docker.client.messages.ServiceCreateResponse;
import com.spotify.docker.client.messages.swarm.Service;
import com.spotify.docker.client.messages.swarm.ServiceSpec;
import com.spotify.docker.client.messages.swarm.Swarm;
import com.spotify.docker.client.messages.swarm.Task;
import com.spotify.docker.client.messages.swarm.Task.Criteria;

/**
 * Extends standard Docker client with "Swarm Mode" features.
 * 
 * @author Derek
 */
public class DefaultSwarmModeDockerClient extends DefaultDockerClient
        implements SwarmModeDockerClient {

    public DefaultSwarmModeDockerClient(Builder builder,
            Supplier<ClientBuilder> clientBuilderSupplier) {
        super(builder, clientBuilderSupplier);
    }

    public DefaultSwarmModeDockerClient(Builder builder) {
        super(builder);
    }

    public DefaultSwarmModeDockerClient(String uri) {
        super(uri);
    }

    public DefaultSwarmModeDockerClient(URI uri, DockerCertificates dockerCertificates) {
        super(uri, dockerCertificates);
    }

    public DefaultSwarmModeDockerClient(URI uri) {
        super(uri);
    }

    private static final GenericType<List<Service>> SERVICE_LIST =
            new GenericType<List<Service>>() {
            };

    private static final GenericType<List<Task>> TASK_LIST = new GenericType<List<Task>>() {
    };

    /* (non-Javadoc)
     * 
     * @see com.spotify.docker.client.SwarmModeDockerClient#inspectSwarm() */
    @Override
    public Swarm inspectSwarm() throws DockerException, InterruptedException {
        final WebTarget resource = resource().path("swarm");
        return request(GET, Swarm.class, resource, resource.request(APPLICATION_JSON_TYPE));
    }

    /* (non-Javadoc)
     * 
     * @see com.spotify.docker.client.SwarmModeDockerClient#createService(com.spotify.docker.client.
     * messages.swarm.ServiceSpec, com.spotify.docker.client.messages.ServiceCreateOptions) */
    @Override
    public ServiceCreateResponse createService(ServiceSpec spec, ServiceCreateOptions options)
            throws DockerException, InterruptedException {
        final WebTarget resource = resource().path("services").path("create");

        try {
            return request(POST, ServiceCreateResponse.class, resource,
                    resource.request(APPLICATION_JSON_TYPE), Entity.json(spec));
        } catch (DockerRequestException e) {
            switch (e.status()) {
            case 406:
                throw new DockerException("Server error or node is not part of swarm.", e);
            case 409:
                throw new DockerException("Name conflicts with an existing object.", e);
            default:
                throw e;
            }
        }
    }

    /* (non-Javadoc)
     * 
     * @see com.spotify.docker.client.SwarmModeDockerClient#inspectService(java.lang.String) */
    @Override
    public Service inspectService(String serviceId) throws DockerException, InterruptedException {
        try {
            final WebTarget resource = resource().path("services").path(serviceId);
            return request(GET, Service.class, resource, resource.request(APPLICATION_JSON_TYPE));
        } catch (DockerRequestException e) {
            switch (e.status()) {
            case 404:
                throw new ServiceNotFoundException(serviceId);
            default:
                throw e;
            }
        }
    }

    /* (non-Javadoc)
     * 
     * @see com.spotify.docker.client.SwarmModeDockerClient#updateService(java.lang.String,
     * java.lang.String, com.spotify.docker.client.messages.swarm.ServiceSpec) */
    @Override
    public void updateService(String serviceId, String version, ServiceSpec spec)
            throws DockerException, InterruptedException {
        try {
            WebTarget resource = resource().path("services").path(serviceId).path("update");
            resource = resource.queryParam("version", version);
            request(POST, Void.class, resource, resource.request(APPLICATION_JSON_TYPE),
                    Entity.json(spec));
        } catch (DockerRequestException e) {
            switch (e.status()) {
            case 404:
                throw new ServiceNotFoundException(serviceId);
            default:
                throw e;
            }
        }
    }

    /* (non-Javadoc)
     * 
     * @see com.spotify.docker.client.SwarmModeDockerClient#listServices() */
    @Override
    public List<Service> listServices() throws DockerException, InterruptedException {
        final WebTarget resource = resource().path("services");
        return request(GET, SERVICE_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
    }

    /* (non-Javadoc)
     * 
     * @see com.spotify.docker.client.SwarmModeDockerClient#listServices(com.spotify.docker.client.
     * messages.swarm.Service.Criteria) */
    public List<Service> listServices(Service.Criteria criteria)
            throws DockerException, InterruptedException {
        WebTarget resource = resource().path("services");
        final Map<String, List<String>> filters = new HashMap<String, List<String>>();

        if (criteria.getServiceId() != null) {
            filters.put("id", Collections.singletonList(criteria.getServiceId()));
        }
        if (criteria.getServiceName() != null) {
            filters.put("name", Collections.singletonList(criteria.getServiceName()));
        }

        resource = resource.queryParam("filters", urlEncodeFilters(filters));
        return request(GET, SERVICE_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
    }

    /* (non-Javadoc)
     * 
     * @see com.spotify.docker.client.SwarmModeDockerClient#removeService(java.lang.String) */
    @Override
    public void removeService(String serviceId) throws DockerException, InterruptedException {
        try {
            final WebTarget resource = resource().path("services").path(serviceId);
            request(DELETE, resource, resource.request(APPLICATION_JSON_TYPE));
        } catch (DockerRequestException e) {
            switch (e.status()) {
            case 404:
                throw new ServiceNotFoundException(serviceId);
            default:
                throw e;
            }
        }
    }

    /* (non-Javadoc)
     * 
     * @see com.spotify.docker.client.SwarmModeDockerClient#inspectTask(java.lang.String) */
    @Override
    public Task inspectTask(String taskId) throws DockerException, InterruptedException {
        try {
            final WebTarget resource = resource().path("tasks").path(taskId);
            return request(GET, Task.class, resource, resource.request(APPLICATION_JSON_TYPE));
        } catch (DockerRequestException e) {
            switch (e.status()) {
            case 404:
                throw new TaskNotFoundException(taskId);
            default:
                throw e;
            }
        }
    }

    /* (non-Javadoc)
     * 
     * @see com.spotify.docker.client.SwarmModeDockerClient#listTasks() */
    @Override
    public List<Task> listTasks() throws DockerException, InterruptedException {
        final WebTarget resource = resource().path("tasks");
        return request(GET, TASK_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
    }

    /* (non-Javadoc)
     * 
     * @see
     * com.spotify.docker.client.SwarmModeDockerClient#listTasks(com.spotify.docker.client.messages.
     * swarm.Task.Criteria) */
    @Override
    public List<Task> listTasks(Criteria criteria) throws DockerException, InterruptedException {
        WebTarget resource = resource().path("tasks");
        final Map<String, List<String>> filters = new HashMap<String, List<String>>();

        if (criteria.getTaskId() != null) {
            filters.put("id", Collections.singletonList(criteria.getTaskId()));
        }
        if (criteria.getTaskName() != null) {
            filters.put("name", Collections.singletonList(criteria.getTaskName()));
        }
        if (criteria.getServiceName() != null) {
            filters.put("service", Collections.singletonList(criteria.getServiceName()));
        }
        if (criteria.getNodeId() != null) {
            filters.put("node", Collections.singletonList(criteria.getNodeId()));
        }
        if (criteria.getLabel() != null) {
            filters.put("label", Collections.singletonList(criteria.getLabel()));
        }
        if (criteria.getDesiredState() != null) {
            filters.put("desired-state", Collections.singletonList(criteria.getDesiredState()));
        }

        resource = resource.queryParam("filters", urlEncodeFilters(filters));
        return request(GET, TASK_LIST, resource, resource.request(APPLICATION_JSON_TYPE));
    }

    public static DefaultSwarmModeDockerClient.Builder builder() {
        return new DefaultSwarmModeDockerClient.Builder();
    }

    public static class Builder extends DefaultDockerClient.Builder {

        @Override
        public DefaultSwarmModeDockerClient build() {
            return new DefaultSwarmModeDockerClient(this);
        }
    }
}
