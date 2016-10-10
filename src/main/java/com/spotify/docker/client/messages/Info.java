/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.docker.client.messages;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Info {

  @JsonProperty("Architecture")
  private String architecture;
  @JsonProperty("ClusterStore")
  private String clusterStore;
  @JsonProperty("CgroupDriver")
  private String cgroupDriver;
  @JsonProperty("Containers")
  private int containers;
  @JsonProperty("ContainersRunning")
  private Integer containersRunning;
  @JsonProperty("ContainersStopped")
  private Integer containersStopped;
  @JsonProperty("ContainersPaused")
  private Integer containersPaused;
  @JsonProperty("CpuCfsPeriod")
  private Boolean cpuCfsPeriod;
  @JsonProperty("CpuCfsQuota")
  private Boolean cpuCfsQuota;
  @JsonProperty("Debug")
  private Boolean debug;
  @JsonProperty("DockerRootDir")
  private String dockerRootDir;
  @JsonProperty("Driver")
  private String storageDriver;
  @JsonProperty("DriverStatus")
  private List<List<String>> driverStatus;
  @JsonProperty("ExecutionDriver")
  private String executionDriver;
  @JsonProperty("ExperimentalBuild")
  private Boolean experimentalBuild;
  @JsonProperty("HttpProxy")
  private String httpProxy;
  @JsonProperty("HttpsProxy")
  private String httpsProxy;
  @JsonProperty("ID")
  private String id;
  @JsonProperty("IPv4Forwarding")
  private boolean ipv4Forwarding;
  @JsonProperty("Images")
  private int images;
  @JsonProperty("IndexServerAddress")
  private String indexServerAddress;
  @JsonProperty("InitPath")
  private String initPath;
  @JsonProperty("InitSha1")
  private String initSha1;
  @JsonProperty("KernelMemory")
  private Boolean kernelMemory;
  @JsonProperty("KernelVersion")
  private String kernelVersion;
  @JsonProperty("Labels")
  private List<String> labels;
  @JsonProperty("MemTotal")
  private long memTotal;
  @JsonProperty("MemoryLimit")
  private Boolean memoryLimit;
  @JsonProperty("NCPU")
  private int cpus;
  @JsonProperty("NEventsListener")
  private int eventsListener;
  @JsonProperty("NFd")
  private int fileDescriptors;
  @JsonProperty("NGoroutines")
  private int goroutines;
  @JsonProperty("Name")
  private String name;
  @JsonProperty("NoProxy")
  private String noProxy;
  @JsonProperty("OomKillDisable")
  private Boolean oomKillDisable;
  @JsonProperty("OperatingSystem")
  private String operatingSystem;
  @JsonProperty("OSType")
  private String osType;
  @JsonProperty("Plugins")
  private Plugins plugins;
  @JsonProperty("RegistryConfig")
  private RegistryConfig registryConfig;
  @JsonProperty("ServerVersion")
  private String serverVersion;
  @JsonProperty("SwapLimit")
  private Boolean swapLimit;
  @JsonProperty("SystemStatus")
  private List<List<String>> systemStatus;
  @JsonProperty("SystemTime")
  private Date systemTime;

  public String architecture() {
    return architecture;
  }

  public String clusterStore() {
    return clusterStore;
  }

  public String cgroupDriver() {
    return cgroupDriver;
  }

  public int containers() {
    return containers;
  }

  public Integer containersRunning() {
    return containersRunning;
  }

  public Integer containersStopped() {
    return containersStopped;
  }

  public Integer containersPaused() {
    return containersPaused;
  }

  public Boolean cpuCfsPeriod() {
    return cpuCfsPeriod;
  }

  public Boolean cpuCfsQuota() {
    return cpuCfsQuota;
  }

  public Boolean debug() {
    return debug;
  }

  public String dockerRootDir() {
    return dockerRootDir;
  }

  public String storageDriver() {
    return storageDriver;
  }

  public List<List<String>> driverStatus() {
    return driverStatus;
  }

  /**
   * @return Execution Driver
   * @deprecated Removed in API 1.24 https://github.com/docker/docker/pull/24501
   */
  @Deprecated
  public String executionDriver() {
    return executionDriver;
  }

  public Boolean experimentalBuild() {
    return experimentalBuild;
  }

  public String httpProxy() {
    return httpProxy;
  }

  public String httpsProxy() {
    return httpsProxy;
  }

  public String id() {
    return id;
  }

  public boolean ipv4Forwarding() {
    return ipv4Forwarding;
  }

  public int images() {
    return images;
  }

  public String indexServerAddress() {
    return indexServerAddress;
  }

  public String initPath() {
    return initPath;
  }

  public String initSha1() {
    return initSha1;
  }

  public Boolean kernelMemory() {
    return kernelMemory;
  }

  public String kernelVersion() {
    return kernelVersion;
  }

  public List<String> labels() {
    return labels;
  }

  public long memTotal() {
    return memTotal;
  }

  public Boolean memoryLimit() {
    return memoryLimit;
  }

  public int cpus() {
    return cpus;
  }

  public int eventsListener() {
    return eventsListener;
  }

  public int fileDescriptors() {
    return fileDescriptors;
  }

  public int goroutines() {
    return goroutines;
  }

  public String name() {
    return name;
  }

  public String noProxy() {
    return noProxy;
  }

  public Boolean oomKillDisable() {
    return oomKillDisable;
  }

  public String operatingSystem() {
    return operatingSystem;
  }

  public String osType() {
    return osType;
  }

  public Plugins plugins() {
    return plugins;
  }

  public RegistryConfig registryConfig() {
    return registryConfig;
  }

  public String serverVersion() {
    return serverVersion;
  }

  public Boolean swapLimit() {
    return swapLimit;
  }

  public List<List<String>> systemStatus() {
    return systemStatus;
  }

  public Date systemTime() {
    return systemTime == null ? null : new Date(systemTime.getTime());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Info that = (Info) o;

    return Objects.equals(this.architecture, that.architecture) &&
           Objects.equals(this.clusterStore, that.clusterStore) &&
           Objects.equals(this.cgroupDriver, that.cgroupDriver) &&
           Objects.equals(this.containers, that.containers) &&
           Objects.equals(this.containersRunning, that.containersRunning) &&
           Objects.equals(this.containersStopped, that.containersStopped) &&
           Objects.equals(this.containersPaused, that.containersPaused) &&
           Objects.equals(this.cpuCfsPeriod, that.cpuCfsPeriod) &&
           Objects.equals(this.cpuCfsQuota, that.cpuCfsQuota) &&
           Objects.equals(this.cpus, that.cpus) &&
           Objects.equals(this.debug, that.debug) &&
           Objects.equals(this.dockerRootDir, that.dockerRootDir) &&
           Objects.equals(this.driverStatus, that.driverStatus) &&
           Objects.equals(this.eventsListener, that.eventsListener) &&
           Objects.equals(this.executionDriver, that.executionDriver) &&
           Objects.equals(this.experimentalBuild, that.experimentalBuild) &&
           Objects.equals(this.fileDescriptors, that.fileDescriptors) &&
           Objects.equals(this.goroutines, that.goroutines) &&
           Objects.equals(this.httpProxy, that.httpProxy) &&
           Objects.equals(this.httpsProxy, that.httpsProxy) &&
           Objects.equals(this.id, that.id) &&
           Objects.equals(this.images, that.images) &&
           Objects.equals(this.indexServerAddress, that.indexServerAddress) &&
           Objects.equals(this.initPath, that.initPath) &&
           Objects.equals(this.initSha1, that.initSha1) &&
           Objects.equals(this.ipv4Forwarding, that.ipv4Forwarding) &&
           Objects.equals(this.kernelVersion, that.kernelVersion) &&
           Objects.equals(this.labels, that.labels) &&
           Objects.equals(this.memoryLimit, that.memoryLimit) &&
           Objects.equals(this.memTotal, that.memTotal) &&
           Objects.equals(this.name, that.name) &&
           Objects.equals(this.noProxy, that.noProxy) &&
           Objects.equals(this.oomKillDisable, that.oomKillDisable) &&
           Objects.equals(this.operatingSystem, that.operatingSystem) &&
           Objects.equals(this.osType, that.osType) &&
           Objects.equals(this.plugins, that.plugins) &&
           Objects.equals(this.registryConfig, that.registryConfig) &&
           Objects.equals(this.serverVersion, that.serverVersion) &&
           Objects.equals(this.storageDriver, that.storageDriver) &&
           Objects.equals(this.swapLimit, that.swapLimit) &&
           Objects.equals(this.systemStatus, that.systemStatus) &&
           Objects.equals(this.systemTime, that.systemTime)
        ;
  }

  @Override
  public int hashCode() {
    return Objects.hash(architecture, clusterStore, cgroupDriver, containers, containersRunning,
                        containersStopped, containersPaused, cpuCfsPeriod, cpuCfsQuota, cpus, debug,
                        dockerRootDir, driverStatus, eventsListener, executionDriver,
                        experimentalBuild, fileDescriptors, goroutines, httpProxy, httpsProxy,
                        id, images, indexServerAddress, initPath, initSha1, ipv4Forwarding,
                        kernelMemory, kernelVersion, labels, memoryLimit, memTotal, name, noProxy,
                        oomKillDisable, operatingSystem, osType, plugins, registryConfig,
                        serverVersion, storageDriver, swapLimit, systemStatus, systemTime);
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("architecture", architecture)
        .add("clusterStore", clusterStore)
        .add("cgroupDriver", cgroupDriver)
        .add("containers", containers)
        .add("containersRunning", containersRunning)
        .add("containersStopped", containersStopped)
        .add("containersPaused", containersPaused)
        .add("cpuCfsPeriod", cpuCfsPeriod)
        .add("cpuCfsQuota", cpuCfsQuota)
        .add("debug", debug)
        .add("dockerRootDir", dockerRootDir)
        .add("storageDriver", storageDriver)
        .add("driverStatus", driverStatus)
        .add("executionDriver", executionDriver)
        .add("experimentalBuild", experimentalBuild)
        .add("httpProxy", httpProxy)
        .add("httpsProxy", httpsProxy)
        .add("id", id)
        .add("ipv4Forwarding", ipv4Forwarding)
        .add("images", images)
        .add("indexServerAddress", indexServerAddress)
        .add("initPath", initPath)
        .add("initSha1", initSha1)
        .add("kernelMemory", kernelMemory)
        .add("kernelVersion", kernelVersion)
        .add("labels", labels)
        .add("memTotal", memTotal)
        .add("memoryLimit", memoryLimit)
        .add("cpus", cpus)
        .add("eventsListener", eventsListener)
        .add("fileDescriptors", fileDescriptors)
        .add("goroutines", goroutines)
        .add("name", name)
        .add("noProxy", noProxy)
        .add("oomKillDisable", oomKillDisable)
        .add("operatingSystem", operatingSystem)
        .add("osType", osType)
        .add("plugins", plugins)
        .add("registryConfig", registryConfig)
        .add("serverVersion", serverVersion)
        .add("swapLimit", swapLimit)
        .add("systemStatus", systemStatus)
        .add("systemTime", systemTime)
        .toString();
  }

  public static class Plugins {

    @JsonProperty("Volumes")
    private ImmutableList<String> volume;
    @JsonProperty("Networks")
    private ImmutableList<String> network;

    public List<String> volume() {
      return volume;
    }

    public List<String> network() {
      return network;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final Plugins that = (Plugins) o;

      return Objects.equals(volume, that.volume) &&
             Objects.equals(network, that.network);
    }

    @Override
    public int hashCode() {
      return Objects.hash(volume, network);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("volume", volume)
          .add("network", network)
          .toString();
    }
  }

  public static class RegistryConfig {

    @JsonProperty("IndexConfigs")
    private ImmutableMap<String, IndexConfig> indexConfigs;
    @JsonProperty("InsecureRegistryCIDRs")
    private ImmutableList<String> insecureRegistryCidrs;

    public Map<String, IndexConfig> indexConfigs() {
      return indexConfigs;
    }

    public List<String> insecureRegistryCidrs() {
      return insecureRegistryCidrs;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final RegistryConfig that = (RegistryConfig) o;

      return Objects.equals(this.indexConfigs, that.indexConfigs) &&
             Objects.equals(this.insecureRegistryCidrs, that.insecureRegistryCidrs);
    }

    @Override
    public int hashCode() {
      return Objects.hash(indexConfigs, insecureRegistryCidrs);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("indexConfigs", indexConfigs)
          .add("insecureRegistryCidrs", insecureRegistryCidrs)
          .toString();
    }
  }

  public static class IndexConfig {

    @JsonProperty("Name")
    private String name;
    @JsonProperty("Mirrors")
    private List<String> mirrors;
    @JsonProperty("Secure")
    private Boolean secure;
    @JsonProperty("Official")
    private Boolean official;

    @JsonCreator
    public IndexConfig() {
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final IndexConfig that = (IndexConfig) o;
      return Objects.equals(this.name, that.name) &&
             Objects.equals(this.mirrors, that.mirrors) &&
             Objects.equals(this.secure, that.secure) &&
             Objects.equals(this.official, that.official);

    }

    @Override
    public int hashCode() {
      return Objects.hash(name, mirrors, secure, official);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("name", name)
          .add("mirrors", mirrors)
          .add("secure", secure)
          .add("official", official)
          .toString();
    }
  }
}
