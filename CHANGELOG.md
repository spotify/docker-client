# Changelog

## 7.0.0

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


### Breaking changes

#### A few classes have been renamed

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

  [no-public-constructors]: https://github.com/google/auto/blob/master/value/userguide/howto.md#public_constructor
