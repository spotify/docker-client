[![Build Status](https://travis-ci.org/spotify/docker-client.svg)](https://travis-ci.org/spotify/docker-client)

Docker Client
=============

This is a simple [docker](https://github.com/dotcloud/docker) client written in Java.

```java
final DockerClient docker = new DefaultDockerClient("http://localhost:4243");

// Pull image
docker.pull("busybox");

// Create container
final ContainerConfig config = ContainerConfig.builder()
    .image("busybox")
    .cmd("sh", "-c", "while :; do sleep 1; done")
    .build();
final ContainerCreation creation = docker.createContainer(config);
final String id = creation.id();

// Inspect container
final ContainerInfo info = docker.inspectContainer(id);

// Start container
docker.startContainer(id);

// Kill container
docker.killContainer(id);

// Remove container
docker.removeContainer(id);
```

