# Changelog

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
