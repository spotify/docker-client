# Changelog

## 8.7.2

### Bugfixes

- Fix: make MemoryStat.stats nullable for ARM ([784][])

[784]: https://github.com/spotify/docker-client/pull/784

## 8.7.1

### Bugfixes

- Fix NPE in MultiRegistryAuthSupplier ([783][])
- Enable http proxy support ([742][])

[783]: https://github.com/spotify/docker-client/pull/783
[742]: https://github.com/spotify/docker-client/issues/742

## 8.7.0

Released June 5, 2017

### Expanded RegistryAuthSupplier support
Add RegistryAuthSuppliers for:
- reading from docker config file (ConfigFileRegistryAuthSupplier)
- combining multiple suppliers (MultiRegistryAuthSupplier)

Also change the default behavior of DefaultDockerClient.Builder to use the
ConfigFileRegistryAuthSupplier if no other authentication options are passed to
the Builder, so that out-of-the-box, any authentication info from the docker
config file (at `~/.dockercfg` or `~/.docker/config.json`) is used.

## 8.6.2

Released May 31, 2017

### Bugfixes
- ContainerRegistryAuthSupplier should ignore exceptions in refreshing the
  Access Token unless RegistryAuth info is needed for a GCR image ([773][])

[773]: https://github.com/spotify/docker-client/pull/773

## 8.6.1

Released May 31, 2017

Added NetworkConfig.Attachable.

[768](https://github.com/spotify/docker-client/issues/768) [770](https://github.com/spotify/docker-client/issues/770)

## 8.6.0

Released May 26, 2017

### Revamped support for authentication
This version introduces a new way to configure DefaultDockerClient to use
authentication - the RegistryAuthSupplier interface.

Historically, a single RegistryAuth instance was configured in
DefaultDockerClient at construction-time and the instance would be used
throughout the lifetime of the DefaultDockerClient instance. Many of the static
factory methods in the RegistryAuth class would use the first auth element
found in the docker client config file, and a DefaultDockerClient configured
with `dockerAuth(true)` would have this behavior enabled by default.

Inspired by a desire to be able to integrate with pushing and pulling images to
Google Container Registry (where the docker client config file contains
short-lived access tokens), the previous behavior has been removed and is
replaced by RegistryAuthSupplier. DefaultDockerClient will now invoke the
appropriate method on the configured RegistryAuthSupplier instance before each
API operation that requires authentication. This allows for use of
authentication info that is dynamic and changes during the lifetime of the
DefaultDockerClient instance.

The docker-client library contains an implementation of this interface that
returns static RegistryAuth instances (NoOpRegistryAuthSupplier, which is
configured for you if you use the old method `registryAuth(RegistryAuth)` in
DefaultDockerClient.Builder) and an implementation for refreshing GCR access
tokens with `gcloud docker -a`. We suggest that users implement this interface
themselves if there is a need to customize the behavior.

The new class DockerConfigReader replaces the static factory methods from
RegistryAuth.

The following methods are deprecated and will be removed in a future release:
- DefaultDockerClient.Builder.registryAuth(RegistryAuth)
- all overloads of RegistryAuth.fromDockerConfig(...)

[740](https://github.com/spotify/docker-client/issues/740), [759](https://github.com/spotify/docker-client/pull/759)

### Upgraded dependencies

Jackson has been upgraded from 2.6.0 to 2.8.8.

## 8.5.0 (released May 18 2017)

### Removal of deprecated methods
Previously deprecated DockerClient methods that have a RegistryAuth parameter
but never used the value were removed. These methods are:

- `load(String, InputStream, RegistryAuth)`
- `load(String, InputStream, RegistryAuth, ProgressHandler)`
- `save(String, RegistryAuth)`

### Fix thread-safety issues with DefaultDockerClient's connection pool

See [#446](https://github.com/spotify/docker-client/issues/446) 
and [#744](https://github.com/spotify/docker-client/pulls/744).

## 7.0.0

### Breaking changes

#### Some classes have been renamed

* `AuthConfig` -> `RegistryAuth`
* `AuthRegistryConfig` -> `RegistryConfigs`

#### Some methods have been renamed

* `ContainerConfig.volumes()` -> `ContainerConfig.volumeNames()`

#### Use public static factory methods instead of public constructors

*These breaking changes are required since [AutoValue doesn't allow public
constructors][no-public-constructors].*

* `HostConfig.Bind`
  * `new HostConfig.Bind.BuilderTo("to")` -> `HostConfig.Bind.BuilderTo.create("to")`
  * `new HostConfig.Bind.BuilderFrom("from")` -> `HostConfig.Bind.BuilderFrom.create("from")`
* `new RemovedImage()` -> `RemovedImage.create()`
* `new ProgressMessage()` -> `ProgressMessage.builder()`
* `new PortBinding()` -> `PortBinding.of()`
* `new ContainerExit()` -> `ContainerExit.create()`

#### Use builders instead of public constructors

`new ContainerCreation()` -> `ContainerCreation.builder()`

#### Removed methods

* no public constructor for `ContainerInfo`, `Info`, or `ImageInfo` anymore

#### Getters for AutoValue classes return Immutable collections

*AutoValue recommends using the immutable type (like ImmutableSet<String>) as the actual property type.*

* `List` -> `ImmutableList`
* `Map` -> `ImmutableMap`
* `Set` -> `ImmutableSet`

#### Builders no longer have getters

You'll need to call `build()` on the builder to get their attributes. AutoValue doesn't support
builder getters.


### Deprecations (will be removed in the future)

#### Methods

* `ContainerConfig.Builder.volumes("/foo")` -> `ContainerConfig.Builder.addVolume("/foo")`
*  methods prefixed by `get` in the following classes lose the `get`
  * `ContainerCreation`
  * `ContainerConfig`
*  methods prefixed by `with` in the following classes lose the `with`
  * `TaskSpec.Builder`
  * `PortConfig.Builder`
  * `EndpointSpec.Builder`
  * `RestartPolicy.Builder`
  * `ContainerSpec.Builder`
  * `ServiceSpec.Builder`
  * `NetworkAttachmentConfig.Builder`
  * `Service.Criteria.Builder`
  * `Task.Criteria.Builder`
* `PortBinding.hostPort()` -> `PortBinding.of()`

#### Misc

Deprecations to `Ipam` and `IpamConfig`.

Before:

```java
final Ipam ipam = Ipam.builder()
    .driver("default")
    .config("192.168.0.0/24", "192.168.0.0/24", "192.168.0.1")
    .build();
```

After:

```java
final IpamConfig ipamConfig = IpamConfig.create("192.168.0.0/24", "192.168.0.0/24", "192.168.0.1");
final Ipam ipam = Ipam.builder()
    .driver("default")
    .config(singletonList(ipamConfig))
    .build();
```

  [no-public-constructors]: https://github.com/google/auto/blob/master/value/userguide/howto.md#public_constructor
