/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
 * --
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -/-/-
 */

package com.spotify.docker.client.messages;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Info {

  @Nullable
  @JsonProperty("Architecture")
  public abstract String architecture();

  @Nullable
  @JsonProperty("ClusterStore")
  public abstract String clusterStore();

  @Nullable
  @JsonProperty("CgroupDriver")
  public abstract String cgroupDriver();

  @NotNull
  @JsonProperty("Containers")
  public abstract Integer containers();

  @Nullable
  @JsonProperty("ContainersRunning")
  public abstract Integer containersRunning();

  @Nullable
  @JsonProperty("ContainersStopped")
  public abstract Integer containersStopped();

  @Nullable
  @JsonProperty("ContainersPaused")
  public abstract Integer containersPaused();

  @Nullable
  @JsonProperty("CpuCfsPeriod")
  public abstract Boolean cpuCfsPeriod();

  @Nullable
  @JsonProperty("CpuCfsQuota")
  public abstract Boolean cpuCfsQuota();

  @NotNull
  @JsonProperty("Debug")
  public abstract Boolean debug();

  @NotNull
  @JsonProperty("DockerRootDir")
  public abstract String dockerRootDir();

  @NotNull
  @JsonProperty("Driver")
  public abstract String storageDriver();

  @NotNull
  @JsonProperty("DriverStatus")
  public abstract ImmutableList<ImmutableList<String>> driverStatus();

  /**
   * @return Execution Driver
   * @deprecated Removed in API 1.24 https://github.com/docker/docker/pull/24501
   */
  @SuppressWarnings("DeprecatedIsStillUsed")
  @Deprecated
  @Nullable
  @JsonProperty("ExecutionDriver")
  public abstract String executionDriver();

  @Nullable
  @JsonProperty("ExperimentalBuild")
  public abstract Boolean experimentalBuild();

  @Nullable
  @JsonProperty("HttpProxy")
  public abstract String httpProxy();

  @Nullable
  @JsonProperty("HttpsProxy")
  public abstract String httpsProxy();

  @NotNull
  @JsonProperty("ID")
  public abstract String id();

  @NotNull
  @JsonProperty("IPv4Forwarding")
  public abstract Boolean ipv4Forwarding();

  @NotNull
  @JsonProperty("Images")
  public abstract Integer images();

  @NotNull
  @JsonProperty("IndexServerAddress")
  public abstract String indexServerAddress();

  @Nullable
  @JsonProperty("InitPath")
  public abstract String initPath();

  @Nullable
  @JsonProperty("InitSha1")
  public abstract String initSha1();

  @Nullable
  @JsonProperty("KernelMemory")
  public abstract Boolean kernelMemory();

  @NotNull
  @JsonProperty("KernelVersion")
  public abstract String kernelVersion();

  @NotNull
  @JsonProperty("Labels")
  public abstract ImmutableList<String> labels();

  @NotNull
  @JsonProperty("MemTotal")
  public abstract Long memTotal();

  @NotNull
  @JsonProperty("MemoryLimit")
  public abstract Boolean memoryLimit();

  @NotNull
  @JsonProperty("NCPU")
  public abstract Integer cpus();

  @NotNull
  @JsonProperty("NEventsListener")
  public abstract Integer eventsListener();

  @NotNull
  @JsonProperty("NFd")
  public abstract Integer fileDescriptors();

  @NotNull
  @JsonProperty("NGoroutines")
  public abstract Integer goroutines();

  @NotNull
  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("NoProxy")
  public abstract String noProxy();

  @Nullable
  @JsonProperty("OomKillDisable")
  public abstract Boolean oomKillDisable();

  @NotNull
  @JsonProperty("OperatingSystem")
  public abstract String operatingSystem();

  @Nullable
  @JsonProperty("OSType")
  public abstract String osType();

  @Nullable
  @JsonProperty("Plugins")
  public abstract Plugins plugins();

  @NotNull
  @JsonProperty("RegistryConfig")
  public abstract RegistryConfig registryConfig();

  @Nullable
  @JsonProperty("ServerVersion")
  public abstract String serverVersion();

  @NotNull
  @JsonProperty("SwapLimit")
  public abstract Boolean swapLimit();

  @Nullable
  @JsonProperty("SystemStatus")
  public abstract ImmutableList<ImmutableList<String>> systemStatus();

  @NotNull
  @JsonProperty("SystemTime")
  public abstract Date systemTime();

  @JsonCreator
  static Info create(
      @JsonProperty("Architecture") final String architecture,
      @JsonProperty("ClusterStore") final String clusterStore,
      @JsonProperty("CgroupDriver") final String cgroupDriver,
      @JsonProperty("Containers") final Integer containers,
      @JsonProperty("ContainersRunning") final Integer containersRunning,
      @JsonProperty("ContainersStopped") final Integer containersStopped,
      @JsonProperty("ContainersPaused") final Integer containersPaused,
      @JsonProperty("CpuCfsPeriod") final Boolean cpuCfsPeriod,
      @JsonProperty("CpuCfsQuota") final Boolean cpuCfsQuota,
      @JsonProperty("Debug") final Boolean debug,
      @JsonProperty("DockerRootDir") final String dockerRootDir,
      @JsonProperty("Driver") final String storageDriver,
      @JsonProperty("DriverStatus") final List<List<String>> driverStatus,
      @JsonProperty("ExecutionDriver") final String executionDriver,
      @JsonProperty("ExperimentalBuild") final Boolean experimentalBuild,
      @JsonProperty("HttpProxy") final String httpProxy,
      @JsonProperty("HttpsProxy") final String httpsProxy,
      @JsonProperty("ID") final String id,
      @JsonProperty("IPv4Forwarding") final Boolean ipv4Forwarding,
      @JsonProperty("Images") final Integer images,
      @JsonProperty("IndexServerAddress") final String indexServerAddress,
      @JsonProperty("InitPath") final String initPath,
      @JsonProperty("InitSha1") final String initSha1,
      @JsonProperty("KernelMemory") final Boolean kernelMemory,
      @JsonProperty("KernelVersion") final String kernelVersion,
      @JsonProperty("Labels") final List<String> labels,
      @JsonProperty("MemTotal") final Long memTotal,
      @JsonProperty("MemoryLimit") final Boolean memoryLimit,
      @JsonProperty("NCPU") final Integer cpus,
      @JsonProperty("NEventsListener") final Integer eventsListener,
      @JsonProperty("NFd") final Integer fileDescriptors,
      @JsonProperty("NGoroutines") final Integer goroutines,
      @JsonProperty("Name") final String name,
      @JsonProperty("NoProxy") final String noProxy,
      @JsonProperty("OomKillDisable") final Boolean oomKillDisable,
      @JsonProperty("OperatingSystem") final String operatingSystem,
      @JsonProperty("OSType") final String osType,
      @JsonProperty("Plugins") final Plugins plugins,
      @JsonProperty("RegistryConfig") final RegistryConfig registryConfig,
      @JsonProperty("ServerVersion") final String serverVersion,
      @JsonProperty("SwapLimit") final Boolean swapLimit,
      @JsonProperty("SystemStatus") final List<List<String>> systemStatus,
      @JsonProperty("SystemTime") final Date systemTime) {
    final ImmutableList<ImmutableList<String>> driverStatusT =
        driverStatus == null ? ImmutableList.<ImmutableList<String>>of()
                             : ImmutableList.of(ImmutableList.copyOf(driverStatus.get(0)));
    final ImmutableList<String> labelsT =
        labels == null ? ImmutableList.<String>of() : ImmutableList.copyOf(labels);
    final ImmutableList<ImmutableList<String>> systemStatusT =
        systemStatus == null ? ImmutableList.<ImmutableList<String>>of()
                             : ImmutableList.of(ImmutableList.copyOf(systemStatus.get(0)));
    return new AutoValue_Info(architecture, clusterStore, cgroupDriver, containers,
        containersRunning, containersStopped, containersPaused, cpuCfsPeriod, cpuCfsQuota, debug,
        dockerRootDir, storageDriver, driverStatusT, executionDriver, experimentalBuild, httpProxy,
        httpsProxy, id, ipv4Forwarding, images, indexServerAddress, initPath, initSha1,
        kernelMemory, kernelVersion, labelsT, memTotal, memoryLimit, cpus, eventsListener,
        fileDescriptors, goroutines, name, noProxy, oomKillDisable, operatingSystem, osType,
        plugins, registryConfig, serverVersion, swapLimit, systemStatusT, systemTime);
  }

  @AutoValue
  public abstract static class Plugins {

    @NotNull
    @JsonProperty("Volumes")
    public abstract ImmutableList<String> volumes();

    @NotNull
    @JsonProperty("Networks")
    public abstract ImmutableList<String> networks();

    @JsonCreator
    static Plugins create(
        @JsonProperty("Volumes") final List<String> volumes,
        @JsonProperty("Networks") final List<String> networks) {
      final ImmutableList<String> volumesT =
          volumes == null ? ImmutableList.<String>of() : ImmutableList.copyOf(volumes);
      final ImmutableList<String> networksT =
          networks == null ? ImmutableList.<String>of() : ImmutableList.copyOf(networks);
      return new AutoValue_Info_Plugins(volumesT, networksT);
    }
  }

  @AutoValue
  public abstract static class RegistryConfig {

    @NotNull
    @JsonProperty("IndexConfigs")
    public abstract ImmutableMap<String, IndexConfig> indexConfigs();

    @NotNull
    @JsonProperty("InsecureRegistryCIDRs")
    public abstract ImmutableList<String> insecureRegistryCidrs();

    @JsonCreator
    static RegistryConfig create(
        @JsonProperty("IndexConfigs") final Map<String, IndexConfig> indexConfigs,
        @JsonProperty("InsecureRegistryCIDRs") final List<String> insecureRegistryCidrs) {
      final ImmutableMap<String, IndexConfig> indexConfigsT =
          indexConfigs == null
          ? ImmutableMap.<String, IndexConfig>of() : ImmutableMap.copyOf(indexConfigs);
      final ImmutableList<String> insecureRegistryCidrsT =
          insecureRegistryCidrs == null
          ? ImmutableList.<String>of() : ImmutableList.copyOf(insecureRegistryCidrs);
      return new AutoValue_Info_RegistryConfig(indexConfigsT, insecureRegistryCidrsT);
    }
  }

  @AutoValue
  public abstract static class IndexConfig {

    @NotNull
    @JsonProperty("Name")
    public abstract String name();

    @NotNull
    @JsonProperty("Mirrors")
    public abstract ImmutableList<String> mirrors();

    @NotNull
    @JsonProperty("Secure")
    public abstract Boolean secure();

    @NotNull
    @JsonProperty("Official")
    public abstract Boolean official();

    @JsonCreator
    static IndexConfig create(
        @JsonProperty("Name") final String name,
        @JsonProperty("Mirrors") final List<String> mirrors,
        @JsonProperty("Secure") final Boolean secure,
        @JsonProperty("Official") final Boolean official) {
      final ImmutableList<String> mirrorsT =
          mirrors == null ? ImmutableList.<String>of() : ImmutableList.copyOf(mirrors);
      return new AutoValue_Info_IndexConfig(name, mirrorsT, secure, official);
    }
  }
}
