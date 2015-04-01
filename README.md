Docker Client [![Circle CI](https://circleci.com/gh/spotify/docker-client.png?style=badge)](https://circleci.com/gh/spotify/docker-client)
=============

This is a simple [Docker](https://github.com/dotcloud/docker) client written in Java.

Usage
-----

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
DockerClient docker = DefaultDockerClient.authConfig(authConfig).build();

// Create container with exposed ports
final String[] ports = {"80", "22"};
final ContainerConfig config = ContainerConfig.builder()
    .image("busybox").exposedPorts(ports)
    .cmd("sh", "-c", "while :; do sleep 1; done")
    .build();

// Bind container ports to host ports
final Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
for(String port : ports) {
    List<PortBinding> hostPorts = new ArrayList<PortBinding>();
    hostPorts.add(PortBinding.of("0.0.0.0", port));
    portBindings.put(port, hostPorts);
}
final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

final ContainerCreation creation = docker.createContainer(config);
final String id = creation.id();

// Inspect container
final ContainerInfo info = docker.inspectContainer(id);

// Start container
docker.startContainer(id, hostConfig);

// Exec command inside running container with attached STDOUT and STDERR
final String[] command = {"bash", "-c", "ls"};
final String execId = docker.execCreate(id, command, DockerClient.ExecParameter.STDOUT, DockerClient.ExecParameter.STDERR);
final LogStream output = docker.execStart(execId);
final String execOutput = output.readFully();

// Kill container
docker.killContainer(id);

// Remove container
docker.removeContainer(id);
```

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

Maven
-----

Please note that in releases 2.7.6 and earlier, the default artifact was the shaded version.  When upgrading to version 2.7.7, you will need to include the shaded classifier if you relied on the shaded dependencies in the
docker-client jar.

Standard:

```xml
<dependency>
  <groupId>com.spotify</groupId>
  <artifactId>docker-client</artifactId>
  <version>2.7.7</version>
</dependency>
```

Shaded:

```xml
<dependency>
  <groupId>com.spotify</groupId>
  <artifactId>docker-client</artifactId>
  <classifier>shaded</classifier>
  <version>2.7.7</version>
</dependency>
```

**This is particularly important if you use Jersey 1.x in your project. To avoid conflicts with docker-client and Jersey 2.x, you will need to explicitly specify the shaded version above.**

Testing
-------

You can run tests on their own with `mvn test`. Note that the tests start and stop a large number of
containers, so the list of containers you see with `docker ps -a` will start to get pretty long
after many test runs. You may find it helpful to occassionally issue `docker rm $(docker ps -aq)`.

Releasing
---------

Commits to the master branch will trigger our continuous integration agent to build the jar and
release by uploading to Sonatype. If you are a project maintainer with the necessary credentials,
you can also build and release locally by running the below.

```sh
mvn release:clean
mvn release:prepare
mvn release:perform
```
