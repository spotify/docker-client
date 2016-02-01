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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class HostConfig {

  @JsonProperty("Binds") private ImmutableList<String> binds;
  @JsonProperty("ContainerIDFile") private String containerIDFile;
  @JsonProperty("LxcConf") private ImmutableList<LxcConfParameter> lxcConf;
  @JsonProperty("Privileged") private Boolean privileged;
  @JsonProperty("PortBindings") private Map<String, List<PortBinding>> portBindings;
  @JsonProperty("Links") private ImmutableList<String> links;
  @JsonProperty("PublishAllPorts") private Boolean publishAllPorts;
  @JsonProperty("Dns") private ImmutableList<String> dns;
  @JsonProperty("DnsSearch") private ImmutableList<String> dnsSearch;
  @JsonProperty("ExtraHosts") private ImmutableList<String> extraHosts;
  @JsonProperty("VolumesFrom") private ImmutableList<String> volumesFrom;
  @JsonProperty("NetworkMode") private String networkMode;
  @JsonProperty("SecurityOpt") private ImmutableList<String> securityOpt;
  @JsonProperty("Memory") private Long memory;
  @JsonProperty("MemorySwap") private Long memorySwap;
  @JsonProperty("CpuShares") private Long cpuShares;
  @JsonProperty("CpusetCpus") private String cpusetCpus;
  @JsonProperty("CpuQuota") private Long cpuQuota;
  @JsonProperty("CgroupParent") private String cgroupParent;
  @JsonProperty("RestartPolicy") private RestartPolicy restartPolicy;

  private HostConfig() {
  }

  private HostConfig(final Builder builder) {
    this.binds = builder.binds;
    this.containerIDFile = builder.containerIDFile;
    this.lxcConf = builder.lxcConf;
    this.privileged = builder.privileged;
    this.portBindings = builder.portBindings;
    this.links = builder.links;
    this.publishAllPorts = builder.publishAllPorts;
    this.dns = builder.dns;
    this.dnsSearch = builder.dnsSearch;
    this.extraHosts = builder.extraHosts;
    this.volumesFrom = builder.volumesFrom;
    this.networkMode = builder.networkMode;
    this.securityOpt = builder.securityOpt;
    this.memory = builder.memory;
    this.memorySwap = builder.memorySwap;
    this.cpuShares = builder.cpuShares;
    this.cpusetCpus = builder.cpusetCpus;
    this.cpuQuota = builder.cpuQuota;
    this.cgroupParent = builder.cgroupParent;
    this.restartPolicy = builder.restartPolicy;
  }

  public List<String> binds() {
    return binds;
  }

  public String containerIDFile() {
    return containerIDFile;
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

  public List<String> dnsSearch() {
    return dnsSearch;
  }

  public List<String> extraHosts() {
    return extraHosts;
  }

  public List<String> volumesFrom() {
    return volumesFrom;
  }

  public String networkMode() {
    return networkMode;
  }

  public List<String> securityOpt() {
    return securityOpt;
  }

  public Long memory() {
    return memory;
  }

  public Long memorySwap() {
    return memorySwap;
  }

  public Long cpuShares() {
    return cpuShares;
  }

  public String cpusetCpus() {
    return cpusetCpus;
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final HostConfig that = (HostConfig) o;

    if (binds != null ? !binds.equals(that.binds) : that.binds != null) {
      return false;
    }
    if (containerIDFile != null ? !containerIDFile.equals(that.containerIDFile)
                                : that.containerIDFile != null) {
      return false;
    }
    if (dns != null ? !dns.equals(that.dns) : that.dns != null) {
      return false;
    }
    if (dnsSearch != null ? !dnsSearch.equals(that.dnsSearch) : that.dnsSearch != null) {
      return false;
    }
    if (extraHosts != null ? !extraHosts.equals(that.extraHosts) : that.extraHosts != null) {
        return false;
      }
    if (links != null ? !links.equals(that.links) : that.links != null) {
      return false;
    }
    if (lxcConf != null ? !lxcConf.equals(that.lxcConf) : that.lxcConf != null) {
      return false;
    }
    if (networkMode != null ? !networkMode.equals(that.networkMode) : that.networkMode != null) {
      return false;
    }
    if (portBindings != null ? !portBindings.equals(that.portBindings)
                             : that.portBindings != null) {
      return false;
    }
    if (privileged != null ? !privileged.equals(that.privileged) : that.privileged != null) {
      return false;
    }
    if (publishAllPorts != null ? !publishAllPorts.equals(that.publishAllPorts)
                                : that.publishAllPorts != null) {
      return false;
    }
    if (volumesFrom != null ? !volumesFrom.equals(that.volumesFrom) : that.volumesFrom != null) {
      return false;
    }
    if (securityOpt != null ? !securityOpt.equals(that.securityOpt) : that.securityOpt != null) {
      return false;
    }

    if (memory != null ? !memory.equals(that.memory) : that.memory != null) {
      return false;
    }

    if (memorySwap != null ? !memorySwap.equals(that.memorySwap) : that.memorySwap != null) {
      return false;
    }

    if (cpuShares != null ? !cpuShares.equals(that.cpuShares) : that.cpuShares != null) {
      return false;
    }

    if (cpusetCpus != null ? !cpusetCpus.equals(that.cpusetCpus) : that.cpusetCpus != null) {
      return false;
    }

    if (cpuQuota != null ? !cpuQuota.equals(that.cpuQuota) : that.cpuQuota != null) {
      return false;
    }

    if (cgroupParent != null ? !cgroupParent.equals(that.cgroupParent)
            : that.cgroupParent != null) {
      return false;
    }

    if (restartPolicy != null ? !restartPolicy.equals(that.restartPolicy)
            : that.restartPolicy != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = binds != null ? binds.hashCode() : 0;
    result = 31 * result + (containerIDFile != null ? containerIDFile.hashCode() : 0);
    result = 31 * result + (lxcConf != null ? lxcConf.hashCode() : 0);
    result = 31 * result + (privileged != null ? privileged.hashCode() : 0);
    result = 31 * result + (portBindings != null ? portBindings.hashCode() : 0);
    result = 31 * result + (links != null ? links.hashCode() : 0);
    result = 31 * result + (publishAllPorts != null ? publishAllPorts.hashCode() : 0);
    result = 31 * result + (dns != null ? dns.hashCode() : 0);
    result = 31 * result + (dnsSearch != null ? dnsSearch.hashCode() : 0);
    result = 31 * result + (extraHosts != null ? extraHosts.hashCode() : 0);
    result = 31 * result + (volumesFrom != null ? volumesFrom.hashCode() : 0);
    result = 31 * result + (networkMode != null ? networkMode.hashCode() : 0);
    result = 31 * result + (securityOpt != null ? securityOpt.hashCode() : 0);
    result = 31 * result + (memory != null ? memory.hashCode() : 0);
    result = 31 * result + (memorySwap != null ? memorySwap.hashCode() : 0);
    result = 31 * result + (cpuShares != null ? cpuShares.hashCode() : 0);
    result = 31 * result + (cpusetCpus != null ? cpusetCpus.hashCode() : 0);
    result = 31 * result + (cpuQuota != null ? cpuQuota.hashCode() : 0);
    result = 31 * result + (cgroupParent != null ? cgroupParent.hashCode() : 0);
    result = 31 * result + (restartPolicy != null ? restartPolicy.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("binds", binds)
        .add("containerIDFile", containerIDFile)
        .add("lxcConf", lxcConf)
        .add("privileged", privileged)
        .add("portBindings", portBindings)
        .add("links", links)
        .add("publishAllPorts", publishAllPorts)
        .add("dns", dns)
        .add("dnsSearch", dnsSearch)
        .add("extraHosts", extraHosts)
        .add("volumesFrom", volumesFrom)
        .add("networkMode", networkMode)
        .add("securityOpt", securityOpt)
        .add("memory", memory)
        .add("memorySwap", memorySwap)
        .add("cpuShares", cpuShares)
        .add("cpusetCpus", cpusetCpus)
        .add("cpuQuota", cpuQuota)
        .add("cgroupParent", cgroupParent)
        .add("restartPolicy", restartPolicy)
        .toString();
  }

  public static class LxcConfParameter {

    @JsonProperty("Key") private String key;
    @JsonProperty("Value") private String value;

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
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final LxcConfParameter that = (LxcConfParameter) o;

      if (key != null ? !key.equals(that.key) : that.key != null) {
        return false;
      }
      if (value != null ? !value.equals(that.value) : that.value != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = key != null ? key.hashCode() : 0;
      result = 31 * result + (value != null ? value.hashCode() : 0);
      return result;
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
    @JsonProperty("Name") private String name;
    @JsonProperty("MaximumRetryCount") private Integer maxRetryCount;

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
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      RestartPolicy that = (RestartPolicy) o;

      if (name != null ? !name.equals(that.name) : that.name != null) {
        return false;
      }
      return maxRetryCount != null ?
              maxRetryCount.equals(that.maxRetryCount) : that.maxRetryCount == null;
    }

    @Override
    public int hashCode() {
      int result = name != null ? name.hashCode() : 0;
      result = 31 * result + (maxRetryCount != null ? maxRetryCount.hashCode() : 0);
      return result;
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
    private String containerIDFile;
    private ImmutableList<LxcConfParameter> lxcConf;
    private Boolean privileged;
    private Map<String, List<PortBinding>> portBindings;
    private ImmutableList<String> links;
    private Boolean publishAllPorts;
    private ImmutableList<String> dns;
    private ImmutableList<String> dnsSearch;
    private ImmutableList<String> extraHosts;
    private ImmutableList<String> volumesFrom;
    private String networkMode;
    private ImmutableList<String> securityOpt;
    public Long memory;
    public Long memorySwap;
    public Long cpuShares;
    public String cpusetCpus;
    public Long cpuQuota;
    public String cgroupParent;
    public RestartPolicy restartPolicy;

    private Builder() {
    }

    private Builder(final HostConfig hostConfig) {
      this.binds = hostConfig.binds;
      this.containerIDFile = hostConfig.containerIDFile;
      this.lxcConf = hostConfig.lxcConf;
      this.privileged = hostConfig.privileged;
      this.portBindings = hostConfig.portBindings;
      this.links = hostConfig.links;
      this.publishAllPorts = hostConfig.publishAllPorts;
      this.dns = hostConfig.dns;
      this.dnsSearch = hostConfig.dnsSearch;
      this.extraHosts = hostConfig.extraHosts;
      this.volumesFrom = hostConfig.volumesFrom;
      this.networkMode = hostConfig.networkMode;
      this.securityOpt = hostConfig.securityOpt;
      this.memory = hostConfig.memory;
      this.memorySwap = hostConfig.memorySwap;
      this.cpuShares = hostConfig.cpuShares;
      this.cpusetCpus = hostConfig.cpusetCpus;
      this.cpuQuota = hostConfig.cpuQuota;
      this.cgroupParent = hostConfig.cgroupParent;
      this.restartPolicy = hostConfig.restartPolicy;
    }

    public Builder binds(final List<String> binds) {
      if (binds != null && !binds.isEmpty()) {
        this.binds = ImmutableList.copyOf(binds);
      }

      return this;
    }

    public Builder binds(final String... binds) {
      if (binds != null && binds.length > 0) {
        this.binds = ImmutableList.copyOf(binds);
      }

      return this;
    }

    public List<String> binds() {
      return binds;
    }

    public Builder containerIDFile(final String containerIDFile) {
      this.containerIDFile = containerIDFile;
      return this;
    }

    public String containerIDFile() {
      return containerIDFile;
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

    public HostConfig build() {
      return new HostConfig(this);
    }
  }
}


