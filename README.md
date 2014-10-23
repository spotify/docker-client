Docker Client [![Build Status](https://travis-ci.org/spotify/docker-client.svg)](https://travis-ci.org/spotify/docker-client)
=============

This is a simple [Docker](https://github.com/dotcloud/docker) client written in Java.

Usage
-----

```java
final DockerClient docker = new DefaultDockerClient("http://localhost:2375");

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

### Unix socket support

Unix socket support is available on Linux since v2.5.0:

```java
final DockerClient docker = new DefaultDockerClient("unix:///var/run/docker.sock");
```

### HTTPS support

We can connect to [HTTPS-secured Docker instances](https://docs.docker.com/articles/https/)
with client-server authentication. The semantics are similar to using [the `DOCKER_CERT_PATH`
environment variable](https://docs.docker.com/articles/https/#client-modes):

```java
final DockerClient docker = new DefaultDockerClient.builder()
    .uri(URI.create("https://boot2docker:2376"))
    .dockerCertificates(new DockerCertificates("/Users/rohan/.docker/boot2docker-vm/"))
    .build();
```

Maven
-----

```xml
<dependency>
  <groupId>com.spotify</groupId>
  <artifactId>docker-client</artifactId>
  <version>2.5.0</version>
</dependency>
```


Releasing
---------

```sh
mvn release:clean
mvn release:prepare
mvn release:perform
```
