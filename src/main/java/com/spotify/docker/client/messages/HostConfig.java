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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class HostConfig {

  @JsonProperty("Binds")
  private ImmutableList<String> binds;
  @JsonProperty("ContainerIDFile")
  private String containerIdFile;
  @JsonProperty("LxcConf")
  private ImmutableList<LxcConfParameter> lxcConf;
  @JsonProperty("Privileged")
  private Boolean privileged;
  @JsonProperty("PortBindings")
  private Map<String, List<PortBinding>> portBindings;
  @JsonProperty("Links")
  private ImmutableList<String> links;
  @JsonProperty("PublishAllPorts")
  private Boolean publishAllPorts;
  @JsonProperty("Dns")
  private ImmutableList<String> dns;
  @JsonProperty("DnsOptions")
  private ImmutableList<String> dnsOptions;
  @JsonProperty("DnsSearch")
  private ImmutableList<String> dnsSearch;
  @JsonProperty("ExtraHosts")
  private ImmutableList<String> extraHosts;
  @JsonProperty("VolumesFrom")
  private ImmutableList<String> volumesFrom;
  @JsonProperty("CapAdd")
  private ImmutableList<String> capAdd;
  @JsonProperty("CapDrop")
  private ImmutableList<String> capDrop;
  @JsonProperty("NetworkMode")
  private String networkMode;
  @JsonProperty("SecurityOpt")
  private ImmutableList<String> securityOpt;
  @JsonProperty("Devices")
  private ImmutableList<Device> devices;
  @JsonProperty("Memory")
  private Long memory;
  @JsonProperty("MemorySwap")
  private Long memorySwap;
  @JsonProperty("MemoryReservation")
  private Long memoryReservation;
  @JsonProperty("CpuPeriod")
  private Long cpuPeriod;
  @JsonProperty("CpuShares")
  private Long cpuShares;
  @JsonProperty("CpusetCpus")
  private String cpusetCpus;
  @JsonProperty("CpusetMems")
  private String cpusetMems;
  @JsonProperty("CpuQuota")
  private Long cpuQuota;
  @JsonProperty("CgroupParent")
  private String cgroupParent;
  @JsonProperty("RestartPolicy")
  private RestartPolicy restartPolicy;
  @JsonProperty("LogConfig")
  private LogConfig logConfig;
  @JsonProperty("IpcMode")
  private String ipcMode;
  @JsonProperty("Ulimits")
  private ImmutableList<Ulimit> ulimits;
  @JsonProperty("PidMode")
  private String pidMode;
  @JsonProperty("ShmSize")
  private Long shmSize;
  @JsonProperty("OomKillDisable")
  private Boolean oomKillDisable;
  @JsonProperty("OomScoreAdj")
  private Integer oomScoreAdj;
  @JsonProperty("AutoRemove")
  private Boolean autoRemove;
  @JsonProperty("PidsLimit")
  private Integer pidsLimit;
  @JsonProperty("Tmpfs")
  private ImmutableMap<String, String> tmpfs;
  @JsonProperty("ReadonlyRootfs")
  private Boolean readonlyRootfs;

  private HostConfig() {
  }

  private HostConfig(final Builder builder) {
    this.binds = builder.binds;
    this.containerIdFile = builder.containerIdFile;
    this.lxcConf = builder.lxcConf;
    this.privileged = builder.privileged;
    this.portBindings = builder.portBindings;
    this.links = builder.links;
    this.publishAllPorts = builder.publishAllPorts;
    this.dns = builder.dns;
    this.dnsOptions = builder.dnsOptions;
    this.dnsSearch = builder.dnsSearch;
    this.extraHosts = builder.extraHosts;
    this.volumesFrom = builder.volumesFrom;
    this.capAdd = builder.capAdd;
    this.capDrop = builder.capDrop;
    this.networkMode = builder.networkMode;
    this.securityOpt = builder.securityOpt;
    this.devices = builder.devices;
    this.memory = builder.memory;
    this.memorySwap = builder.memorySwap;
    this.memoryReservation = builder.memoryReservation;
    this.cpuPeriod = builder.cpuPeriod;
    this.cpuShares = builder.cpuShares;
    this.cpusetCpus = builder.cpusetCpus;
    this.cpusetMems = builder.cpusetMems;
    this.cpuQuota = builder.cpuQuota;
    this.cgroupParent = builder.cgroupParent;
    this.restartPolicy = builder.restartPolicy;
    this.logConfig = builder.logConfig;
    this.ipcMode = builder.ipcMode;
    this.ulimits = builder.ulimits;
    this.pidMode = builder.pidMode;
    this.shmSize = builder.shmSize;
    this.oomKillDisable = builder.oomKillDisable;
    this.oomScoreAdj = builder.oomScoreAdj;
    this.autoRemove = builder.autoRemove;
    this.pidsLimit = builder.pidsLimit;
    this.tmpfs = builder.tmpfs;
    this.readonlyRootfs = builder.readonlyRootfs;
  }

  public List<String> binds() {
    return binds;
  }

  public String containerIdFile() {
    return containerIdFile;
  }

  public List<LxcConfParameter> lxcConf() {
    return lxcConf;
  }

  public Boolean privileged() {
    return privileged;
  }

  public Map<String, List<PortBinding>> portBindings() {
    return (portBindings == null) ? null : Collections.unmodifiableMap(portBindings);
  }

  public List<String> links() {
    return links;
  }

  public Boolean publishAllPorts() {
    return publishAllPorts;
  }

  public List<String> dns() {
    return dns;
  }

  public List<String> dnsOptions() {
    return dnsOptions;
  }

  public List<String> dnsSearch() {
    return dnsSearch;
  }

  public List<String> extraHosts() {
    return extraHosts;
  }

  public List<String> volumesFrom() {
    return volumesFrom;
  }

  public List<String> capAdd() {
    return capAdd;
  }

  public List<String> capDrop() {
    return capDrop;
  }

  public String networkMode() {
    return networkMode;
  }

  public List<String> securityOpt() {
    return securityOpt;
  }

  public List<Device> devices() {
    return devices;
  }

  public Long memory() {
    return memory;
  }

  public Long memorySwap() {
    return memorySwap;
  }

  public Long getMemoryReservation() {
    return memoryReservation;
  }

  public Long cpuPeriod() {
    return cpuPeriod;
  }

  public Long cpuShares() {
    return cpuShares;
  }

  public String cpusetCpus() {
    return cpusetCpus;
  }

  public String cpusetMems() {
    return cpusetMems;
  }

  public Long cpuQuota() {
    return cpuQuota;
  }

  public String cgroupParent() {
    return cgroupParent;
  }

  public RestartPolicy restartPolicy() {
    return restartPolicy;
  }

  public LogConfig logConfig() {
    return logConfig;
  }

  public String ipcMode() {
    return ipcMode;
  }

  public List<Ulimit> ulimits() {
    return ulimits;
  }

  public Long shmSize() {
    return shmSize;
  }

  public Boolean oomKillDisable() {
    return oomKillDisable;
  }

  public Integer oomScoreAdj() {
    return oomScoreAdj;
  }

  /**
   * Tune container pids limit (set -1 for unlimited).
   * Only works for kernels >= 4.3
   */
  public Integer pidsLimit() {
    return pidsLimit;
  }
  
  public ImmutableMap<String, String> tmpfs() {
    return tmpfs;
  }

  public Boolean readonlyRootfs() {
    return readonlyRootfs;
  }
  
  public Boolean autoRemove() {
    return autoRemove;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final HostConfig that = (HostConfig) obj;

    return Objects.equals(this.binds, that.binds)
           && Objects.equals(this.containerIdFile, that.containerIdFile)
           && Objects.equals(this.lxcConf, that.lxcConf)
           && Objects.equals(this.privileged, that.privileged)
           && Objects.equals(this.portBindings, that.portBindings)
           && Objects.equals(this.links, that.links)
           && Objects.equals(this.publishAllPorts, that.publishAllPorts)
           && Objects.equals(this.dns, that.dns)
           && Objects.equals(this.dnsOptions, that.dnsOptions)
           && Objects.equals(this.dnsSearch, that.dnsSearch)
           && Objects.equals(this.extraHosts, that.extraHosts)
           && Objects.equals(this.volumesFrom, that.volumesFrom)
           && Objects.equals(this.capAdd, that.capAdd)
           && Objects.equals(this.capDrop, that.capDrop)
           && Objects.equals(this.networkMode, that.networkMode)
           && Objects.equals(this.securityOpt, that.securityOpt)
           && Objects.equals(this.devices, that.devices)
           && Objects.equals(this.memory, that.memory)
           && Objects.equals(this.memorySwap, that.memorySwap)
           && Objects.equals(this.memoryReservation, that.memoryReservation)
           && Objects.equals(this.cpuPeriod, that.cpuPeriod)
           && Objects.equals(this.cpuShares, that.cpuShares)
           && Objects.equals(this.cpusetCpus, that.cpusetCpus)
           && Objects.equals(this.cpusetMems, that.cpusetMems)
           && Objects.equals(this.cpuQuota, that.cpuQuota)
           && Objects.equals(this.cgroupParent, that.cgroupParent)
           && Objects.equals(this.restartPolicy, that.restartPolicy)
           && Objects.equals(this.logConfig, that.logConfig)
           && Objects.equals(this.ipcMode, that.ipcMode)
           && Objects.equals(this.ulimits, that.ulimits)
           && Objects.equals(this.oomKillDisable, that.oomKillDisable)
           && Objects.equals(this.oomScoreAdj, that.oomScoreAdj)
           && Objects.equals(this.autoRemove, that.autoRemove)
           && Objects.equals(this.pidsLimit, that.pidsLimit)
           && Objects.equals(this.tmpfs, that.tmpfs)
           && Objects.equals(this.readonlyRootfs, that.readonlyRootfs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(binds, containerIdFile, lxcConf, privileged, portBindings, links,
                        publishAllPorts, dns, dnsOptions, dnsSearch, extraHosts, volumesFrom,
                        capAdd, capDrop, networkMode, securityOpt, devices, memory, memorySwap,
                        memoryReservation, cpuPeriod, cpuShares, cpusetCpus, cpusetMems, cpuQuota,
                        cgroupParent, restartPolicy, logConfig, ipcMode, ulimits, pidMode, shmSize,
                        oomKillDisable, oomScoreAdj, autoRemove, pidsLimit, tmpfs, readonlyRootfs);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("binds", binds)
        .add("containerIdFile", containerIdFile)
        .add("lxcConf", lxcConf)
        .add("privileged", privileged)
        .add("portBindings", portBindings)
        .add("links", links)
        .add("publishAllPorts", publishAllPorts)
        .add("dns", dns)
        .add("dnsOptions", dnsOptions)
        .add("dnsSearch", dnsSearch)
        .add("extraHosts", extraHosts)
        .add("volumesFrom", volumesFrom)
        .add("capAdd", capAdd)
        .add("capDrop", capDrop)
        .add("networkMode", networkMode)
        .add("securityOpt", securityOpt)
        .add("devices", devices)
        .add("memory", memory)
        .add("memorySwap", memorySwap)
        .add("memoryReservation", memoryReservation)
        .add("cpuPeriod", cpuPeriod)
        .add("cpuShares", cpuShares)
        .add("cpusetCpus", cpusetCpus)
        .add("cpusetMems", cpusetMems)
        .add("cpuQuota", cpuQuota)
        .add("cgroupParent", cgroupParent)
        .add("restartPolicy", restartPolicy)
        .add("logConfig", logConfig)
        .add("ipcMode", ipcMode)
        .add("ulimits", ulimits)
        .add("pidMode", pidMode)
        .add("shmSize", shmSize)
        .add("oomKillDisable", oomKillDisable)
        .add("oomScoreAdj", oomScoreAdj)
        .add("autoRemove", autoRemove)
        .add("pidsLimit", pidsLimit)
        .add("tmpfs", tmpfs)
        .add("readonlyRootfs", readonlyRootfs)
        .toString();
  }

  public static class LxcConfParameter {

    @JsonProperty("Key")
    private String key;
    @JsonProperty("Value")
    private String value;

    public LxcConfParameter(final String key, final String value) {
      this.key = key;
      this.value = value;
    }

    public String key() {
      return key;
    }

    public String value() {
      return value;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      final LxcConfParameter that = (LxcConfParameter) obj;

      return Objects.equals(this.key, that.key)
             && Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(key, value);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("key", key)
          .add("value", value)
          .toString();
    }
  }

  public static class RestartPolicy {

    @JsonProperty("Name")
    private String name;
    @JsonProperty("MaximumRetryCount")
    private Integer maxRetryCount;

    public static RestartPolicy always() {
      return new RestartPolicy("always", null);
    }

    public static RestartPolicy unlessStopped() {
      return new RestartPolicy("unless-stopped", null);
    }

    public static RestartPolicy onFailure(Integer maxRetryCount) {
      return new RestartPolicy("on-failure", maxRetryCount);
    }

    // for mapper
    private RestartPolicy() {
    }

    private RestartPolicy(String name, Integer maxRetryCount) {
      this.name = name;
      this.maxRetryCount = maxRetryCount;
    }

    public String name() {
      return name;
    }

    public Integer maxRetryCount() {
      return maxRetryCount;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      final RestartPolicy that = (RestartPolicy) obj;

      return Objects.equals(this.name, that.name)
             && Objects.equals(this.maxRetryCount, that.maxRetryCount);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, maxRetryCount);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("name", name)
          .add("maxRetryCount", maxRetryCount)
          .toString();
    }
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private ImmutableList<String> binds;
    private String containerIdFile;
    private ImmutableList<LxcConfParameter> lxcConf;
    private Boolean privileged;
    private Map<String, List<PortBinding>> portBindings;
    private ImmutableList<String> links;
    private Boolean publishAllPorts;
    private ImmutableList<String> dns;
    private ImmutableList<String> dnsOptions;
    private ImmutableList<String> dnsSearch;
    private ImmutableList<String> extraHosts;
    private ImmutableList<String> volumesFrom;
    private ImmutableList<String> capAdd;
    private ImmutableList<String> capDrop;
    private String networkMode;
    private ImmutableList<String> securityOpt;
    private ImmutableList<Device> devices;
    private Long memory;
    private Long memorySwap;
    private Long memoryReservation;
    private Long cpuPeriod;
    private Long cpuShares;
    private String cpusetCpus;
    private String cpusetMems;
    private Long cpuQuota;
    private String cgroupParent;
    private RestartPolicy restartPolicy;
    private LogConfig logConfig;
    private String ipcMode;
    private ImmutableList<Ulimit> ulimits;
    private String pidMode;
    private Long shmSize;
    private Boolean oomKillDisable;
    private Integer oomScoreAdj;
    private Boolean autoRemove;
    private Integer pidsLimit;
    private ImmutableMap<String, String> tmpfs;
    private Boolean readonlyRootfs;

    private Builder() {
    }

    private Builder(final HostConfig hostConfig) {
      this.binds = hostConfig.binds;
      this.containerIdFile = hostConfig.containerIdFile;
      this.lxcConf = hostConfig.lxcConf;
      this.privileged = hostConfig.privileged;
      this.portBindings = hostConfig.portBindings;
      this.links = hostConfig.links;
      this.publishAllPorts = hostConfig.publishAllPorts;
      this.dns = hostConfig.dns;
      this.dnsOptions = hostConfig.dnsOptions;
      this.dnsSearch = hostConfig.dnsSearch;
      this.extraHosts = hostConfig.extraHosts;
      this.volumesFrom = hostConfig.volumesFrom;
      this.capAdd = hostConfig.capAdd;
      this.capDrop = hostConfig.capDrop;
      this.networkMode = hostConfig.networkMode;
      this.securityOpt = hostConfig.securityOpt;
      this.devices = hostConfig.devices;
      this.memory = hostConfig.memory;
      this.memorySwap = hostConfig.memorySwap;
      this.memoryReservation = hostConfig.memoryReservation;
      this.cpuPeriod = hostConfig.cpuPeriod;
      this.cpuShares = hostConfig.cpuShares;
      this.cpusetCpus = hostConfig.cpusetCpus;
      this.cpusetMems = hostConfig.cpusetMems;
      this.cpuQuota = hostConfig.cpuQuota;
      this.cgroupParent = hostConfig.cgroupParent;
      this.restartPolicy = hostConfig.restartPolicy;
      this.logConfig = hostConfig.logConfig;
      this.ipcMode = hostConfig.ipcMode;
      this.ulimits = hostConfig.ulimits;
      this.pidMode = hostConfig.pidMode;
      this.shmSize = hostConfig.shmSize;
      this.oomKillDisable = hostConfig.oomKillDisable;
      this.oomScoreAdj = hostConfig.oomScoreAdj;
      this.autoRemove = hostConfig.autoRemove;
      this.pidsLimit = hostConfig.pidsLimit;
      this.tmpfs = hostConfig.tmpfs;
      this.readonlyRootfs = hostConfig.readonlyRootfs;
    }

    /**
     * Set the list of binds to the parameter, replacing any existing value.
     * <p>To append to the list instead, use one of the appendBinds() methods.</p>
     *
     * @param binds A list of volume bindings for this container. Each volume binding is a string.
     * @return The builder
     */
    public Builder binds(final List<String> binds) {
      if (binds != null && !binds.isEmpty()) {
        this.binds = copyWithoutDuplicates(binds);
      }

      return this;
    }

    /**
     * Set the list of binds to the parameter, replacing any existing value.
     * <p>To append to the list instead, use one of the appendBinds() methods.</p>
     *
     * @param binds An array of volume bindings for this container. Each volume binding is a
     *              string.
     * @return The builder
     */
    public Builder binds(final String... binds) {
      if (binds != null && binds.length > 0) {
        return binds(Lists.newArrayList(binds));
      }

      return this;
    }

    public List<String> binds() {
      return binds;
    }

    /**
     * Set the list of binds to the parameter, replacing any existing value.
     * <p>To append to the list instead, use one of the appendBinds() methods.</p>
     *
     * @param binds An array of volume bindings for this container. Each volume binding is a {@link
     *              Bind} object.
     * @return The builder
     */
    public Builder binds(final Bind... binds) {
      if (binds == null || binds.length == 0) {
        return this;
      }

      return binds(toStringList(binds));
    }

    private static List<String> toStringList(final Bind[] binds) {
      final List<String> bindStrings = Lists.newArrayList();
      for (final Bind bind : binds) {
        bindStrings.add(bind.toString());
      }
      return bindStrings;
    }

    /**
     * Append binds to the existing list in this builder.
     *
     * @param newBinds An iterable of volume bindings for this container. Each volume binding is a
     *                 String.
     * @return The builder
     */
    public Builder appendBinds(final Iterable<String> newBinds) {
      final List<String> list = new ArrayList<>();
      if (this.binds != null) {
        list.addAll(this.binds);
      }
      list.addAll(Lists.newArrayList(newBinds));
      this.binds = copyWithoutDuplicates(list);

      return this;
    }

    /**
     * Append binds to the existing list in this builder.
     *
     * @param binds An array of volume bindings for this container. Each volume binding is a {@link
     *              Bind} object.
     * @return The builder
     */
    public Builder appendBinds(final Bind... binds) {
      appendBinds(toStringList(binds));
      return this;
    }

    /**
     * Append binds to the existing list in this builder.
     *
     * @param binds An array of volume bindings for this container. Each volume binding is a
     *              String.
     * @return The builder
     */
    public Builder appendBinds(final String... binds) {
      appendBinds(Lists.newArrayList(binds));
      return this;
    }

    private static <T> ImmutableList<T> copyWithoutDuplicates(final List<T> input) {
      final List<T> list = new ArrayList<>(input.size());
      for (final T element : input) {
        if (!list.contains(element)) {
          list.add(element);
        }
      }
      return ImmutableList.copyOf(list);
    }

    public Builder containerIdFile(final String containerIdFile) {
      this.containerIdFile = containerIdFile;
      return this;
    }

    public String containerIdFile() {
      return containerIdFile;
    }

    public Builder lxcConf(final List<LxcConfParameter> lxcConf) {
      if (lxcConf != null && !lxcConf.isEmpty()) {
        this.lxcConf = ImmutableList.copyOf(lxcConf);
      }
      return this;
    }

    public Builder lxcConf(final LxcConfParameter... lxcConf) {
      if (lxcConf != null && lxcConf.length > 0) {
        this.lxcConf = ImmutableList.copyOf(lxcConf);
      }

      return this;
    }

    public List<LxcConfParameter> lxcConf() {
      return lxcConf;
    }

    public Builder privileged(final Boolean privileged) {
      this.privileged = privileged;
      return this;
    }

    public Boolean privileged() {
      return privileged;
    }

    public Builder portBindings(final Map<String, List<PortBinding>> portBindings) {
      if (portBindings != null && !portBindings.isEmpty()) {
        this.portBindings = Maps.newHashMap(portBindings);
      }
      return this;
    }

    public Map<String, List<PortBinding>> portBindings() {
      return portBindings;
    }

    public Builder links(final List<String> links) {
      if (links != null && !links.isEmpty()) {
        this.links = ImmutableList.copyOf(links);
      }

      return this;
    }

    public Builder links(final String... links) {
      if (links != null && links.length > 0) {
        this.links = ImmutableList.copyOf(links);
      }

      return this;
    }

    public List<String> links() {
      return links;
    }

    public Builder publishAllPorts(final Boolean publishAllPorts) {
      this.publishAllPorts = publishAllPorts;
      return this;
    }

    public Boolean publishAllPorts() {
      return publishAllPorts;
    }

    public Builder dns(final List<String> dns) {
      if (dns != null && !dns.isEmpty()) {
        this.dns = ImmutableList.copyOf(dns);
      }

      return this;
    }

    public Builder dns(final String... dns) {
      if (dns != null && dns.length > 0) {
        this.dns = ImmutableList.copyOf(dns);
      }

      return this;
    }

    public List<String> dns() {
      return dns;
    }

    public Builder dnsOptions(final List<String> dnsOptions) {
      if (dnsOptions != null && !dnsOptions.isEmpty()) {
        this.dnsOptions = ImmutableList.copyOf(dnsOptions);
      }

      return this;
    }

    public Builder dnsOptions(final String... dnsOptions) {
      if (dnsOptions != null && dnsOptions.length > 0) {
        this.dnsOptions = ImmutableList.copyOf(dnsOptions);
      }

      return this;
    }

    public List<String> dnsOptions() {
      return dnsOptions;
    }

    public Builder dnsSearch(final List<String> dnsSearch) {
      if (dnsSearch != null && !dnsSearch.isEmpty()) {
        this.dnsSearch = ImmutableList.copyOf(dnsSearch);
      }

      return this;
    }

    public Builder dnsSearch(final String... dnsSearch) {
      if (dnsSearch != null && dnsSearch.length > 0) {
        this.dnsSearch = ImmutableList.copyOf(dnsSearch);
      }

      return this;
    }

    public List<String> dnsSearch() {
      return dnsSearch;
    }

    public Builder extraHosts(final List<String> extraHosts) {
      if (extraHosts != null && !extraHosts.isEmpty()) {
        this.extraHosts = ImmutableList.copyOf(extraHosts);
      }

      return this;
    }

    public Builder extraHosts(final String... extraHosts) {
      if (extraHosts != null && extraHosts.length > 0) {
        this.extraHosts = ImmutableList.copyOf(extraHosts);
      }

      return this;
    }

    public List<String> extraHosts() {
      return extraHosts;
    }

    public Builder volumesFrom(final List<String> volumesFrom) {
      if (volumesFrom != null && !volumesFrom.isEmpty()) {
        this.volumesFrom = ImmutableList.copyOf(volumesFrom);
      }

      return this;
    }

    public Builder volumesFrom(final String... volumesFrom) {
      if (volumesFrom != null && volumesFrom.length > 0) {
        this.volumesFrom = ImmutableList.copyOf(volumesFrom);
      }

      return this;
    }

    public List<String> volumesFrom() {
      return volumesFrom;
    }

    public Builder capAdd(final List<String> capAdd) {
      if (capAdd != null && !capAdd.isEmpty()) {
        this.capAdd = ImmutableList.copyOf(capAdd);
      }

      return this;
    }

    public Builder capAdd(final String... capAdd) {
      if (capAdd != null && capAdd.length > 0) {
        this.capAdd = ImmutableList.copyOf(capAdd);
      }

      return this;
    }

    public List<String> capAdd() {
      return capAdd;
    }

    public Builder capDrop(final List<String> capDrop) {
      if (capDrop != null && !capDrop.isEmpty()) {
        this.capDrop = ImmutableList.copyOf(capDrop);
      }

      return this;
    }

    public Builder capDrop(final String... capDrop) {
      if (capDrop != null && capDrop.length > 0) {
        this.capDrop = ImmutableList.copyOf(capDrop);
      }

      return this;
    }

    public List<String> capDrop() {
      return capDrop;
    }

    public Builder networkMode(final String networkMode) {
      this.networkMode = networkMode;
      return this;
    }

    public String networkMode() {
      return networkMode;
    }

    public Builder securityOpt(final List<String> securityOpt) {
      if (securityOpt != null && !securityOpt.isEmpty()) {
        this.securityOpt = ImmutableList.copyOf(securityOpt);
      }

      return this;
    }

    public Builder securityOpt(final String... securityOpt) {
      if (securityOpt != null && securityOpt.length > 0) {
        this.securityOpt = ImmutableList.copyOf(securityOpt);
      }

      return this;
    }

    public List<String> securityOpt() {
      return securityOpt;
    }

    public Builder devices(final List<Device> devices) {
      if (devices != null && !devices.isEmpty()) {
        this.devices = ImmutableList.copyOf(devices);
      }
      return this;
    }

    public Builder devices(final Device... devices) {
      if (devices != null && devices.length > 0) {
        this.devices = ImmutableList.copyOf(devices);
      }

      return this;
    }

    public List<Device> devices() {
      return devices;
    }

    public Builder memory(final Long memory) {
      this.memory = memory;
      return this;
    }

    public Long memory() {
      return memory;
    }

    public Builder memorySwap(final Long memorySwap) {
      this.memorySwap = memorySwap;
      return this;
    }

    public Long memorySwap() {
      return memorySwap;
    }

    public Builder memoryReservation(final Long memoryReservation) {
      this.memoryReservation = memoryReservation;
      return this;
    }

    public Long memoryReservation() {
      return memoryReservation;
    }

    public Builder cpuPeriod(final Long cpuPeriod) {
      this.cpuPeriod = cpuPeriod;
      return this;
    }

    public Long cpuPeriod() {
      return cpuPeriod;
    }

    public Builder cpuShares(final Long cpuShares) {
      this.cpuShares = cpuShares;
      return this;
    }

    public Long cpuShares() {
      return cpuShares;
    }

    public Builder cpusetCpus(final String cpusetCpus) {
      this.cpusetCpus = cpusetCpus;
      return this;
    }

    public String cpusetCpus() {
      return cpusetCpus;
    }

    public Builder cpusetMems(final String cpusetMems) {
      this.cpusetMems = cpusetMems;
      return this;
    }

    public String cpusetMems() {
      return cpusetMems;
    }

    public Builder cpuQuota(final Long cpuQuota) {
      this.cpuQuota = cpuQuota;
      return this;
    }

    public Long cpuQuota() {
      return cpuQuota;
    }

    public Builder cgroupParent(final String cgroupParent) {
      this.cgroupParent = cgroupParent;
      return this;
    }

    public String cgroupParent() {
      return cgroupParent;
    }

    public Builder restartPolicy(final RestartPolicy restartPolicy) {
      this.restartPolicy = restartPolicy;
      return this;
    }

    public RestartPolicy restartPolicy() {
      return restartPolicy;
    }

    public Builder logConfig(final LogConfig logConfig) {
      this.logConfig = logConfig;
      return this;
    }

    public LogConfig logConfig() {
      return logConfig;
    }

    public Builder ipcMode(final String ipcMode) {
      this.ipcMode = ipcMode;
      return this;
    }

    public String ipcMode() {
      return ipcMode;
    }

    public Builder ulimits(final List<Ulimit> ulimits) {
      this.ulimits = ImmutableList.copyOf(ulimits);
      return this;
    }

    /**
     * Set the PID (Process) Namespace mode for the container.
     * Use this method to join another container's PID namespace. To use the host
     * PID namespace, use {@link #hostPidMode()}.
     *
     * @param container Join the namespace of this container (Name or ID)
     * @return Builder
     */
    public Builder containerPidMode(final String container) {
      this.pidMode = "container:" + container;
      return this;
    }

    /**
     * Set the PID (Process) Namespace mode for the container.
     * Use this method to use the host's PID namespace. To use another container's
     * PID namespace, use {@link #containerPidMode(String)}.
     *
     * @return Builder
     */
    public Builder hostPidMode() {
      this.pidMode = "host";
      return this;
    }

    public Builder shmSize(final Long shmSize) {
      this.shmSize = shmSize;
      return this;
    }

    public Long shmSize() {
      return shmSize;
    }

    public Builder oomKillDisable(final Boolean oomKillDisable) {
      this.oomKillDisable = oomKillDisable;
      return this;
    }

    public Boolean oomKillDisable() {
      return oomKillDisable;
    }

    public Builder oomScoreAdj(final Integer oomScoreAdj) {
      this.oomScoreAdj = oomScoreAdj;
      return this;
    }

    public Integer oomScoreAdj() {
      return oomScoreAdj;
    }

    public Boolean autoRemove() {
      return autoRemove;
    }

    /**
     * Only works for Docker API version >= 1.25.
     */
    public Builder autoRemove(final Boolean autoRemove) {
      this.autoRemove = autoRemove;
      return this;
    }

    public Builder pidsLimit(final Integer pidsLimit) {
      this.pidsLimit = pidsLimit;
      return this;
    }

    public Integer pidsLimit() {
      return pidsLimit;
    }
    
    public Builder readonlyRootfs(final Boolean readonlyRootfs) {
      this.readonlyRootfs = readonlyRootfs;
      return this;
    }

    public Boolean readonlyRootfs() {
      return readonlyRootfs;
    }

    public Builder tmpfs(final ImmutableMap<String, String> tmpfs) {
      this.tmpfs = tmpfs;
      return this;
    }

    public ImmutableMap<String, String> tmpfs() {
      return tmpfs;
    }

    public HostConfig build() {
      return new HostConfig(this);
    }
  }

  public static class Bind {

    private String to;
    private String from;
    private Boolean readOnly;
    private Boolean noCopy;

    private Bind(final Builder builder) {
      this.to = builder.to;
      this.from = builder.from;
      this.readOnly = builder.readOnly;
      this.noCopy = builder.noCopy;
    }

    public static BuilderTo to(final String to) {
      return new BuilderTo(to);
    }

    public static BuilderFrom from(final String from) {
      return new BuilderFrom(from);
    }

    public static BuilderFrom from(final Volume volumeFrom) {
      return new BuilderFrom(volumeFrom);
    }

    public String toString() {
      if (to == null || to.equals("")) {
        return "";
      } else if (from == null || from.equals("")) {
        return to;
      }

      final String bind = from + ":" + to;

      final List<String> options = new ArrayList<>();
      if (readOnly != null && readOnly) {
        options.add("ro");
      }
      if (noCopy != null && noCopy) {
        options.add("nocopy");
      }

      final String optionsValue = Joiner.on(',').join(options);

      return (optionsValue.isEmpty()) ? bind : bind + ":" + optionsValue;
    }

    public static class BuilderTo {

      private String to;

      public BuilderTo(final String to) {
        this.to = to;
      }

      public Builder from(final String from) {
        return new Builder(this, from);
      }

      public Builder from(final Volume volumeFrom) {
        return new Builder(this, volumeFrom);
      }
    }

    public static class BuilderFrom {

      private String from;

      public BuilderFrom(final String from) {
        this.from = from;
      }

      public BuilderFrom(final Volume volumeFrom) {
        this.from = volumeFrom.name();
      }

      public Bind.Builder to(final String to) {
        return new Builder(this, to);
      }
    }

    public static class Builder {

      private String to;
      private String from;
      private Boolean readOnly = false;
      private Boolean noCopy;

      private Builder() {
      }

      private Builder(final BuilderTo toBuilder, final String from) {
        this.to = toBuilder.to;
        this.from = from;
      }

      private Builder(final BuilderTo toBuilder, final Volume volumeFrom) {
        this.to = toBuilder.to;
        this.from = volumeFrom.name();
      }

      private Builder(final BuilderFrom fromBuilder, final String to) {
        this.to = to;
        this.from = fromBuilder.from;
      }

      public Builder to(final String to) {
        this.to = to;
        return this;
      }

      public String to() {
        return to;
      }

      public Builder from(final String from) {
        this.from = from;
        return this;
      }

      public Builder from(final Volume volumeFrom) {
        this.from = volumeFrom.name();
        return this;
      }

      public String from() {
        return from;
      }

      public Builder readOnly(final Boolean readOnly) {
        this.readOnly = readOnly;
        return this;
      }

      public Boolean readOnly() {
        return readOnly;
      }

      public Builder noCopy(final Boolean noCopy) {
        this.noCopy = noCopy;
        return this;
      }

      public Boolean noCopy() {
        return noCopy;
      }

      public Bind build() {
        return new Bind(this);
      }
    }
  }

  public static class Ulimit {

    @JsonProperty("Name")
    private String name;
    @JsonProperty("Soft")
    private Long soft;
    @JsonProperty("Hard")
    private Long hard;

    public Ulimit() {
    }

    private Ulimit(final Builder builder) {
      this.name = builder.name;
      this.soft = builder.soft;
      this.hard = builder.hard;
    }

    public static Builder builder() {
      return new Builder();
    }

    public String name() {
      return name;
    }

    public Long soft() {
      return soft;
    }

    public Long hard() {
      return hard;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      final Ulimit that = (Ulimit) obj;

      return Objects.equals(this.name, that.name)
             && Objects.equals(this.soft, that.soft)
             && Objects.equals(this.hard, that.hard);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, soft, hard);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("name", name)
          .add("soft", soft)
          .add("hard", hard)
          .toString();
    }

    public static class Builder {

      private String name;
      private Long soft;
      private Long hard;

      private Builder() {
      }

      public Ulimit build() {
        return new Ulimit(this);
      }

      public Builder name(final String name) {
        this.name = name;
        return this;
      }

      public Builder soft(final Long soft) {
        this.soft = soft;
        return this;
      }

      public Builder hard(final Long hard) {
        this.hard = hard;
        return this;
      }
    }
  }
}
