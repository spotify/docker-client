# Docker Client [![Circle CI](https://circleci.com/gh/spotify/docker-client.png?style=badge)](https://circleci.com/gh/spotify/docker-client) [![codecov.io](https://codecov.io/github/spotify/docker-client/coverage.svg?branch=master)](https://codecov.io/github/spotify/docker-client?branch=master)


This is a simple [Docker](https://github.com/docker/docker) client written in Java.

**The latest docker-client release has only been tested on Docker API version 1.20. This client
should work with Docker API versions > 1.15, but your mileage may vary.**

## Usage


```java
// Create a client based on DOCKER_HOST and DOCKER_CERT_PATH env vars
final DockerClient docker = DefaultDockerClient.fromEnv().build();

// Pull an image
docker.pull("busybox");

// Pull an image from a private repository
// Server address defaults to "https://index.docker.io/v1/"
AuthConfig authConfig = AuthConfig.builder().email("foo@bar.com").username("foobar")
  .password("secret-password").serverAddress("https://myprivateregistry.com/v1/").build();
docker.pull("foobar/busybox-private:latest", authConfig);

// You can also set the AuthConfig for the DockerClient instead of passing everytime you call pull()
DockerClient docker = DefaultDockerClient.fromEnv().authConfig(authConfig).build();

// Bind container ports to host ports
final String[] ports = {"80", "22"};
final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
for (String port : ports) {
    List<PortBinding> hostPorts = new ArrayList<PortBinding>();
    hostPorts.add(PortBinding.of("0.0.0.0", port));
    portBindings.put(port, hostPorts);
}

// Bind container port 443 to an automatically allocated available host port.
List<PortBinding> randomPort = new ArrayList<PortBinding>();
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
final String[] command = {"bash", "-c", "ls"};
final String execId = docker.execCreate(
    id, command, DockerClient.ExecCreateParam.attachStdout(),
    DockerClient.ExecCreateParam.attachStderr());
final LogStream output = docker.execStart(execId);
final String execOutput = output.readFully();

// Kill container
docker.killContainer(id);

// Remove container
docker.removeContainer(id);

// Close the docker client
docker.close();
```

### Mounting volumes in a container
To mount a host directory into a container, create the container with a `HostConfig`.
You can set the local path and remote path in the `binds()` method on the `HostConfig.Builder`.
There are two ways to make a bind:
1. Pass `binds()` a set of strings of the form `"local_path:container_path"`.
2. Create a `Bind` object and pass it to `binds()`.

If you only need to create a volume in a container, and you don't need it to mount any
particular directory on the host, you can use the `volumes()` method on the 
`ContainerConfig.Builder`.

#### Example:
```java
final HostConfig hostConfig = 
  HostConfig.builder()
    .binds("/local/path:/remote/path")
    .binds(Bind.from("/another/local/path")
               .to("/another/remote/path")
               .readOnly(true)
               .build())
    .build();
final ContainerConfig volumeConfig = 
  ContainerConfig.builder()
    .image(BUSYBOX_LATEST)
    .volumes("/foo")   // This volume will not mount any host directory
    .hostConfig(hostConfig)
    .build();
```

#### Note on Mounts
Be aware that, starting with API version 1.20 (docker version 1.8.x), information 
about a container's volumes is returned with the key `"Mounts"`, not `"Volumes"`.  
As such, the `ContainerInfo.volumes()` method is deprecated. Instead, use
`ContainerInfo.mounts()`.

### Configuration

Both `DefaultDockerClient.builder()` and `DefaultDockerClient.fromEnv()` return a
`DefaultDockerClient.Builder`. The builder can be used to configure and build clients with custom
timeouts, connection pool sizes, and other parameters.

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
final DockerClient docker = DefaultDockerClient.builder()
    .uri(URI.create("https://boot2docker:2376"))
    .dockerCertificates(new DockerCertificates(Paths.get("/Users/rohan/.docker/boot2docker-vm/")))
    .build();
```

### Connection pooling

We use the [Apache HTTP client](https://hc.apache.org/) under the covers, with a shared connection
pool per instance of the Docker client. The default size of this pool is 100 connections, so each
instance of the Docker client can only have 100 concurrent requests in flight.

If you plan on waiting on more than 100 containers at a time (`DockerClient.waitContainer`), or
otherwise need a higher number of concurrent requests, you can modify the connection pool size:

```java
final DockerClient docker = DefaultDockerClient.fromEnv()
    .connectionPoolSize(SOME_LARGE_NUMBER)
    .build()
```

Note that the connect timeout is also applied to acquiring a connection from the pool. If the pool
is exhausted and it takes too long to acquire a new connection for a request, we throw a
`DockerTimeoutException` instead of just waiting forever on a connection becoming available.

## Maven


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


## Testing

Make sure Docker daemon is running and that you can do `docker ps`.

You can run tests on their own with `mvn test`. Note that the tests start and stop a large number of
containers, so the list of containers you see with `docker ps -a` will start to get pretty long
after many test runs. You may find it helpful to occassionally issue `docker rm $(docker ps -aq)`.

## Releasing


Commits to the master branch will trigger our continuous integration agent to build the jar and
release by uploading to Sonatype. If you are a project maintainer with the necessary credentials,
you can also build and release locally by running the below.

```sh
mvn release:clean
mvn release:prepare
mvn release:perform
```
