# Docker Client

[![Build Status](https://travis-ci.org/spotify/docker-client.svg?branch=master)](https://travis-ci.org/spotify/docker-client)
[![codecov](https://codecov.io/github/spotify/docker-client/coverage.svg?branch=master)](https://codecov.io/github/spotify/docker-client?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.spotify/docker-client.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.spotify%22%20docker-client)
[![License](https://img.shields.io/github/license/spotify/docker-client.svg)](LICENSE)

This is a [Docker](https://github.com/docker/docker) client written in Java.
It is used in many critical production systems at Spotify.

* [Version compatibility](#version-compatibility)
* [Download](#download)
* [Usage Example](#usage-example)
* [Getting Started](#getting-started)
* [Prerequisites](#prerequisites)
* [Testing](#testing)
* [Releasing](#releasing)
* [A Note on Shading](#a-note-on-shading)
* [Code of Conduct](#code-of-conduct)
* [User Manual](https://github.com/spotify/docker-client/blob/master/docs/user_manual.md)

## Version compatibility
docker-client is built and tested against the six most recent minor releases of Docker.
Right now these are 1.12.6 - 17.09.0-ce (specifically the ones [here][1]).
We upload the artifact tested on Docker 1.13.1.
See [Docker docs on the mapping between Docker version and API version][3].

## Download

Download the latest JAR or grab [via Maven][maven-search].

```xml
<dependency>
  <groupId>com.spotify</groupId>
  <artifactId>docker-client</artifactId>
  <version>LATEST-VERSION</version>
</dependency>
```


## Usage Example

```java
// Create a client based on DOCKER_HOST and DOCKER_CERT_PATH env vars
final DockerClient docker = DefaultDockerClient.fromEnv().build();

// Pull an image
docker.pull("busybox");

// Bind container ports to host ports
final String[] ports = {"80", "22"};
final Map<String, List<PortBinding>> portBindings = new HashMap<>();
for (String port : ports) {
    List<PortBinding> hostPorts = new ArrayList<>();
    hostPorts.add(PortBinding.of("0.0.0.0", port));
    portBindings.put(port, hostPorts);
}

// Bind container port 443 to an automatically allocated available host port.
List<PortBinding> randomPort = new ArrayList<>();
randomPort.add(PortBinding.randomPort("0.0.0.0"));
portBindings.put("443", randomPort);

final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

// Create container with exposed ports
final ContainerConfig containerConfig = ContainerConfig.builder()
    .hostConfig(hostConfig)
    .image("busybox").exposedPorts(ports)
    .cmd("sh", "-c", "while :; do sleep 1; done")
    .build();

final ContainerCreation creation = docker.createContainer(containerConfig);
final String id = creation.id();

// Inspect container
final ContainerInfo info = docker.inspectContainer(id);

// Start container
docker.startContainer(id);

// Exec command inside running container with attached STDOUT and STDERR
final String[] command = {"sh", "-c", "ls"};
final ExecCreation execCreation = docker.execCreate(
    id, command, DockerClient.ExecCreateParam.attachStdout(),
    DockerClient.ExecCreateParam.attachStderr());
final LogStream output = docker.execStart(execCreation.id());
final String execOutput = output.readFully();

// Kill container
docker.killContainer(id);

// Remove container
docker.removeContainer(id);

// Close the docker client
docker.close();
```

## Getting Started

If you're looking for how to use docker-client, see the [User Manual][2].
If you're looking for how to build and develop it, keep reading.

## Prerequisites

docker-client should be buildable on any platform with Docker 1.6+, JDK7+, and a recent version of
Maven 3.

### A note on using Docker for Mac

If you are using Docker for Mac and `DefaultDockerClient.fromEnv()`, it might not be clear
what value to use for the `DOCKER_HOST` environment variable. The value you should use is
`DOCKER_HOST=unix:///var/run/docker.sock`, at least as of version 1.11.1-beta11.

As of version 4.0.8 of docker-client, `DefaultDockerClient.fromEnv()` uses
`unix:///var/run/docker.sock` on OS X by default.

## Testing

If you're running a recent version of docker (>= 1.12), which contains native swarm support, please
ensure that you run `docker swarm init` to initialize the docker swarm.

Make sure Docker daemon is running and that you can do `docker ps`.

You can run tests on their own with `mvn test`. Note that the tests start and stop a large number of
containers, so the list of containers you see with `docker ps -a` will start to get pretty long
after many test runs. You may find it helpful to occasionally issue `docker rm $(docker ps -aq)`.

## Releasing

Commits to the master branch will trigger our continuous integration agent to build the jar and
release by uploading to Sonatype. If you are a project maintainer with the necessary credentials,
you can also build and release locally by running the below.

```sh
mvn clean [-DskipTests -Darguments=-DskipTests] -Dgpg.keyname=<key ID used for signing artifacts> release:prepare release:perform
```

## A note on shading

Please note that in releases 2.7.6 and earlier, the default artifact was the shaded version.
When upgrading to version 2.7.7, you will need to include the shaded classifier if you relied on
the shaded dependencies in the docker-client jar.

Standard:

```xml
<dependency>
  <groupId>com.spotify</groupId>
  <artifactId>docker-client</artifactId>
  <version>3.5.12</version>
</dependency>
```

Shaded:

```xml
<dependency>
  <groupId>com.spotify</groupId>
  <artifactId>docker-client</artifactId>
  <classifier>shaded</classifier>
  <version>3.5.12</version>
</dependency>
```

**This is particularly important if you use Jersey 1.x in your project. To avoid conflicts with
docker-client and Jersey 2.x, you will need to explicitly specify the shaded version above.**


  [1]: https://travis-ci.org/spotify/docker-client
  [2]: docs/user_manual.md
  [3]: https://docs.docker.com/engine/api/v1.27/#section/Versioning


## Code of conduct

This project adheres to the [Open Code of Conduct][code-of-conduct]. By participating, you are
expected to honor this code.

  [code-of-conduct]: https://github.com/spotify/code-of-conduct/blob/master/code-of-conduct.md
  [maven-search]: https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.spotify%22%20docker-client
