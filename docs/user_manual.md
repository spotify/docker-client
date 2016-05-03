# User Manual

This user manual is made to correspond to Docker's [API docs][1] (e.g. [API 1.18][2]).

* [Creating a DockerClient](#creating-a-docker-client)
  * [Unix socket support](#unix-socket-support)
  * [HTTPS support](#https-support)
  * [Connection pooling](#connection-pooling)
* [Containers](#containers)
  * [List containers](#list-containers)
  * [Create a container](#create-a-container)
  * [Inspect a container](#inspect-a-container)
  * [List processes running inside a container](#list-processes-running-inside-a-container)
  * [Get container logs](#get-container-logs)
  * [Inspect changes on a container's filesystem](#inspect-changes-on-a-containers-filesystem)
  * [Export a container](#export-a-container)
  * [Get container stats based on resource usage](#get-container-stats-based-on-resource-usage)
  * [Resize a container TTY](#resize-a-container-tty)
  * [Start a container](#start-a-container)
  * [Stop a container](#stop-a-container)
  * [Restart a container](#restart-a-container)
  * [Kill a container](#kill-a-container)
  * [Rename a container](#rename-a-container)
  * [Pause a container](#pause-a-container)
  * [Unpause a container](#unpause-a-container)
  * [Attach to a container](#attach-to-a-container)
  * [Attach to a container (websocket)](#attach-to-a-container-websocket)
  * [Wait a container](#wait-a-container)
  * [Remove a container](#remove-a-container)
  * [Copy files or folders from a container](#copy-files-or-folders-from-a-container)
* [Images](#images)
  * [List Images](#list-images)
  * [Build image from a Dockerfile](#build-image-from-a-dockerfile)
  * [Create an image](#create-an-image)
  * [Inspect an image](#inspect-an-image)
  * [Get the history of an image](#get-the-history-of-an-image)
  * [Push an image on the registry](#push-an-image-on-the-registry)
  * [Tag an image into a repository](#tag-an-image-into-a-repository)
  * [Remove an image](#remove-an-image)
  * [Search images](#search-images)
* [Miscellaneous](#miscellaneous)
  * [Check auth configuration](#check-auth-configuration)
  * [Display system-wide information](#display-system-wide-information)
  * [Show the docker version information](#show-the-docker-version-information)
  * [Ping the docker server](#ping-the-docker-server)
  * [Create a new image from a container’s changes](#create-a-new-image-from-a-containers-changes)
  * [Monitor Docker’s events](#monitor-dockers-events)
  * [Get a tarball containing all images in a repository](#get-a-tarball-containing-all-images-in-a-repository)
  * [Get a tarball containing all images.](#get-a-tarball-containing-all-images)
  * [Load a tarball with a set of images and tags into docker](#load-a-tarball-with-a-set-of-images-and-tags-into-docker)
  * [Image tarball format](#image-tarball-format)
  * [Exec Create](#exec-create)
  * [Exec Start](#exec-start)
  * [Exec Resize](#exec-resize)
  * [Exec Inspect](#exec-inspect)
  * [Mounting volumes in a container](#mounting-volumes-in-a-container)
  * [Using RESTEasy instead of Jersey]#(resteasy)


## Creating a docker-client

```java
// Create a client based on DOCKER_HOST and DOCKER_CERT_PATH env vars
final DockerClient docker = DefaultDockerClient.fromEnv().build();

// or use the builder
final DockerClient docker = DefaultdockerClient.builder()
  // Set various options
  .build();
```

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


## Containers

### List containers

```java
final List<Container> containers = docker.listContainers();

// List all containers. Only running containers are shown by default.
final List<Container> containers = docker.listContainers(ListContainersParam.allContainers());
```

### Create a container

```java
final ContainerCreation container = docker.createContainer(ContainerConfig.builder().build());
```

### Inspect a container

```java
final ContainerInfo info = docker.inspectContainer("containerID");
```

### List processes running inside a container

Not implemented. PRs welcome.

### Get container logs

```java
final String logs;
try (LogStream stream = client.logs("containerID", LogsParam.stdout(), LogsParam.stderr())) {
  logs = stream.readFully();
}
```

### Inspect changes on a container's filesystem

Not implemented. PRs welcome.

### Export a container

```java
ImmutableSet.Builder<String> files = ImmutableSet.builder();
try (TarArchiveInputStream tarStream = new TarArchiveInputStream(docker.exportContainer(id))) {
  TarArchiveEntry entry;
  while ((entry = tarStream.getNextTarEntry()) != null) {
    files.add(entry.getName());
  }
}
```

### Get container stats based on resource usage

```java
final ContainerStats stats = docker.stats("containerID");
```

### Resize a container TTY

Not implemented. PRs welcome.

### Start a container

```java
docker.startContainer("containerID");
```

### Restart a container

```java
docker.restartContainer("containerID");
// or with a seconds to wait before restarting parameter
docker.restartContainer("containerID", 10);
```

### Kill a container

```java
docker.killContainer("containerID");
```

### Rename a container

```java
docker.renameContainer("oldContainerID", "newContainerID");
```

### Pause a container

```java
docker.pauseContainer("containerID");
```

### Unpause a container

```java
docker.unpauseContainer("containerID");
```

### Attach to a container

```java
final String logs;
try (LogStream stream = docker.attachContainer(volumeContainer,
      AttachParameter.LOGS, AttachParameter.STDOUT,
      AttachParameter.STDERR, AttachParameter.STREAM)) {
  logs = stream.readFully();
}
```

### Attach to a container (websocket)

Not implemented. PRs welcome.

### Wait a container

```java
final ContainerExit exit = docker.waitContainer("containerID");
```

### Remove a container

```java
docker.removeContainer("containerID");
```

### Copy files or folders from a container

```java
ImmutableSet.Builder<String> files = ImmutableSet.builder();
try (TarArchiveInputStream tarStream =
    new TarArchiveInputStream(docker.copyContainer(id, "/bin"))) {
  TarArchiveEntry entry;
  while ((entry = tarStream.getNextTarEntry()) != null) {
    files.add(entry.getName());
  }
}
```


## Images

### List images

```java
final List<Image> quxImages = docker.listImages(ListImagesParam.withLabel("foo", "qux"));
```

### Build image from a Dockerfile

```java
final AtomicReference<String> imageIdFromMessage = new AtomicReference<>();

final String returnedImageId = docker.build(
    Paths.get(dockerDirectory), "test", new ProgressHandler() {
      @Override
      public void progress(ProgressMessage message) throws DockerException {
        final String imageId = message.buildImageId();
        if (imageId != null) {
          imageIdFromMessage.set(imageId);
        }
      }
    });
```

### Create an image


```java
// By pulling
final AuthConfig authConfig = AuthConfig.builder().email(AUTH_EMAIL).username(AUTH_USERNAME)
  .password(AUTH_PASSWORD).build();
docker.pull("dxia2/scratch-private:latest", authConfig);

// or by loading from a source
final File imageFile = new File("/path/to/image/file");
final String image = "busybox-test" + System.nanoTime();
try (InputStream imagePayload = new BufferedInputStream(new FileInputStream(imageFile))) {
  docker.load(image, imagePayload);
}
```

### Inspect an image

```java
final ImageInfo info = docker.inspectImage("imageID")
```

### Get the history of an image

Not implemented yet. PRs welcome.

### Push an image on the registry

```java
docker.push("imageID");
```

### Tag an image into a repository

```java
docker.pull("busybox:latest");

final String name = "testRepo/tagForce:sometag";
// Assign name to first image
docker.tag("busybox:latest", name);

// Force-re-assign tag to another image
docker.tag("busybox:buildroot-2014.02", name, true);
```

### Remove an image

```java
docker.removeImage("imageID");
```

### Search images

```java
final List<ImageSearchResult> searchResult = docker.searchImages("busybox");
```

## Miscellaneous

### Check auth configuration

```java
final AuthConfig authConfig = AuthConfig.builder().email(AUTH_EMAIL).username(AUTH_USERNAME)
  .password(AUTH_PASSWORD).build();
final int statusCode = docker.auth(authConfig);
assertThat(statusCode, equalTo(200));
```

### Display system-wide information

```java
final Info info = docker.info();
```

### Show the docker version information

```java
final Version version = docker.version();
```

### Ping the docker server

```java
final String pingResponse = docker.ping();
assertThat(pingResponse, equalTo("OK"));
```

### Create a new image from a container's changes

```java
// Pull image
docker.pull("busybox:latest");

// Create container
final ContainerConfig config = ContainerConfig.builder()
    .image("busybox:latest")
    .build();
final String name = randomName();
final ContainerCreation creation = docker.createContainer(config, name);
final String id = creation.id();

final String tag = "foobar";
final ContainerCreation newContainer = docker.commitContainer(
    id, "mosheeshel/busybox", tag, config, "CommitedByTest-" + tag, "newContainer");

final ImageInfo imageInfo = docker.inspectImage(newContainer.id());
assertThat(imageInfo.author(), is("newContainer"));
assertThat(imageInfo.comment(), is("CommitedByTest-" + "foobar"));
```

### Monitor Docker's events

```java
docker.pull("busybox:latest");
final EventStream eventStream = docker.events();
final ContainerConfig config = ContainerConfig.builder()
    .image("busybox:latest")
    .build();
final ContainerCreation container = docker.createContainer(config, randomName());
docker.startContainer(container.id());

final Event createEvent = eventStream.next();
assertThat(createEvent.status(), equalTo("create"));
assertThat(createEvent.id(), equalTo(container.id()));
assertThat(createEvent.from(), startsWith("busybox:"));
assertThat(createEvent.time(), notNullValue());

final Event startEvent = eventStream.next();
assertThat(startEvent.status(), equalTo("start"));
assertThat(startEvent.id(), equalTo(container.id()));
assertThat(startEvent.from(), startsWith("busybox:"));
assertThat(startEvent.time(), notNullValue());

eventStream.close();
```

### Get a tarball containing all images in a repository

```java
final File imageFile = save(BUSYBOX);
assertTrue(imageFile.length() > 0);

final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
assertTrue("Temp directory " + tmpDir.getAbsolutePath() + " does not exist", tmpDir.exists());
final File imageFile = new File(tmpDir, "busybox-" + System.nanoTime() + ".tar");

imageFile.createNewFile();
imageFile.deleteOnExit();
final byte[] buffer = new byte[2048];
int read;

try (OutputStream imageOutput = new BufferedOutputStream(new FileOutputStream(imageFile))) {
  try (InputStream imageInput = docker.save("busybox", authConfig)) {
    while ((read = imageInput.read(buffer)) > -1) {
      imageOutput.write(buffer, 0, read);
    }
  }
}
```


### Get a tarball containing all images.

Not implemented yet. PRs welcome.

### Load a tarball with a set of images and tags into docker

Not implemented yet. PRs welcome.

### Exec Create

```java
final String execId = docker.execCreate(containerId, new String[]{"sh", "-c", "exit 2"});

try (final LogStream stream = docker.execStart(execId)) {
  stream.readFully();
}

final ExecState state = docker.execInspect(execId);
assertThat(state.id(), is(execId));
assertThat(state.running(), is(false));
assertThat(state.exitCode(), is(2));
assertThat(state.openStdin(), is(true));
assertThat(state.openStderr(), is(true));
assertThat(state.openStdout(), is(true));
}
```

### Exec Start

See [example above](#exec-create).

### Exec Resize

Not implemented yet. PRs welcome.

### Exec Inspect

See [example above](#exec-create).

### Mounting volumes in a container
To mount a host directory into a container, create the container with a `HostConfig`.
You can set the local path and remote path in the `binds()` method on the `HostConfig.Builder`.
There are two ways to make a bind:
1. Pass `binds()` a set of strings of the form `"local_path:container_path"`.
2. Create a `Bind` object and pass it to `binds()`.

If you only need to create a volume in a container, and you don't need it to mount any
particular directory on the host, you can use the `volumes()` method on the
`ContainerConfig.Builder`.

```java
final HostConfig hostConfig =
  HostConfig.builder()
    .appendBinds("/local/path:/remote/path")
    .appendBinds(Bind.from("/another/local/path")
               .to("/another/remote/path")
               .readOnly(true)
               .build())
    .build();
final ContainerConfig volumeConfig =
  ContainerConfig.builder()
    .image("busybox:latest")
    .volumes("/foo")   // This volume will not mount any host directory
    .hostConfig(hostConfig)
    .build();
```

#### A note on mounts
Be aware that, starting with API version 1.20 (docker version 1.8.x), information
about a container's volumes is returned with the key `"Mounts"`, not `"Volumes"`.
As such, the `ContainerInfo.volumes()` method is deprecated. Instead, use
`ContainerInfo.mounts()`.

  [1]: https://docs.docker.com/engine/reference/api/docker_remote_api/
  [2]: https://docs.docker.com/engine/reference/api/docker_remote_api_v1.18/

### Using RESTEasy instead of Jersey

To use RESTEasy in place of Jersey you just need replace a few dependencies and configure the RESTEasyClientFactory.  

    <dependency>
       <groupId>com.spotify</groupId>
       <artifactId>docker-client</artifactId>
       <version>4.0.6-SNAPSHOT</version>
       <exclusions>
          <exclusion>
             <groupId>org.glassfish.jersey.connectors</groupId>
             <artifactId>jersey-apache-connector</artifactId>
          </exclusion>
          <exclusion>
             <groupId>org.glassfish.jersey.media</groupId>
             <artifactId>jersey-media-json-jackson</artifactId>
          </exclusion>
          <exclusion>
             <groupId>org.glassfish.jersey.core</groupId>
             <artifactId>jersey-client</artifactId>
          </exclusion>
       </exclusions>
    </dependency>
	 <dependency>
       <groupId>org.jboss.resteasy</groupId>
       <artifactId>resteasy-client</artifactId>
       <version>3.0.16.Final</version>
    </dependency>
    <dependency>
       <groupId>org.jboss.resteasy</groupId>
       <artifactId>resteasy-jaxrs</artifactId>
       <version>3.0.16.Final</version>
    </dependency>
    <dependency>
       <groupId>org.jboss.resteasy</groupId>
       <artifactId>resteasy-jackson2-provider</artifactId>
       <version>3.0.16.Final</version>
    </dependency>
    
    
To configure the RESTEasy Client Factory:

    DockerClient docker = DefaultDockerClient.builder()
       .uri(baseUrl)
       .clientFactory(new ResteasyClientFactory())
       .build();
       
       
#### Caveats to using alternate JAX-RS Implementations

   * Events currently only work with Jersey
   