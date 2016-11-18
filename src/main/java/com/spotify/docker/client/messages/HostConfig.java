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
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class HostConfig {

  @Nullable
  @JsonProperty("Binds")
  public abstract ImmutableList<String> binds();

  @Nullable
  @JsonProperty("ContainerIDFile")
  public abstract String containerIdFile();

  @Nullable
  @JsonProperty("LxcConf")
  public abstract ImmutableList<LxcConfParameter> lxcConf();

  @Nullable
  @JsonProperty("Privileged")
  public abstract Boolean privileged();

  @Nullable
  @JsonProperty("PortBindings")
  public abstract ImmutableMap<String, List<PortBinding>> portBindings();

  @Nullable
  @JsonProperty("Links")
  public abstract ImmutableList<String> links();

  @Nullable
  @JsonProperty("PublishAllPorts")
  public abstract Boolean publishAllPorts();

  @Nullable
  @JsonProperty("Dns")
  public abstract ImmutableList<String> dns();

  @Nullable
  @JsonProperty("DnsSearch")
  public abstract ImmutableList<String> dnsSearch();

  @Nullable
  @JsonProperty("ExtraHosts")
  public abstract ImmutableList<String> extraHosts();

  @Nullable
  @JsonProperty("VolumesFrom")
  public abstract ImmutableList<String> volumesFrom();

  @Nullable
  @JsonProperty("CapAdd")
  public abstract ImmutableList<String> capAdd();

  @Nullable
  @JsonProperty("CapDrop")
  public abstract ImmutableList<String> capDrop();

  @Nullable
  @JsonProperty("NetworkMode")
  public abstract String networkMode();

  @Nullable
  @JsonProperty("SecurityOpt")
  public abstract ImmutableList<String> securityOpt();

  @Nullable
  @JsonProperty("Devices")
  public abstract ImmutableList<Device> devices();

  @Nullable
  @JsonProperty("Memory")
  public abstract Long memory();

  @Nullable
  @JsonProperty("MemorySwap")
  public abstract Long memorySwap();

  @Nullable
  @JsonProperty("MemoryReservation")
  public abstract Long memoryReservation();

  @Nullable
  @JsonProperty("CpuShares")
  public abstract Long cpuShares();

  @Nullable
  @JsonProperty("CpusetCpus")
  public abstract String cpusetCpus();

  @Nullable
  @JsonProperty("CpuQuota")
  public abstract Long cpuQuota();

  @Nullable
  @JsonProperty("CgroupParent")
  public abstract String cgroupParent();

  @Nullable
  @JsonProperty("RestartPolicy")
  public abstract RestartPolicy restartPolicy();

  @Nullable
  @JsonProperty("LogConfig")
  public abstract LogConfig logConfig();

  @Nullable
  @JsonProperty("IpcMode")
  public abstract String ipcMode();

  @Nullable
  @JsonProperty("Ulimits")
  public abstract ImmutableList<Ulimit> ulimits();

  @Nullable
  @JsonProperty("PidMode")
  public abstract String pidMode();

  @Nullable
  @JsonProperty("ShmSize")
  public abstract Long shmSize();

  @Nullable
  @JsonProperty("OomKillDisable")
  public abstract Boolean oomKillDisable();

  @Nullable
  @JsonProperty("OomScoreAdj")
  public abstract Integer oomScoreAdj();

  @Nullable
  @JsonProperty("AutoRemove")
  public abstract Boolean autoRemove();

  /**
   * Tune container pids limit (set -1 for unlimited).
   * Only works for kernels >= 4.3
   */
  @Nullable
  @JsonProperty("PidsLimit")
  public abstract Integer pidsLimit();

  @Nullable
  @JsonProperty("Tmpfs")
  public abstract ImmutableMap<String, String> tmpfs();

  @Nullable
  @JsonProperty("ReadonlyRootfs")
  public abstract Boolean readonlyRootfs();

  @JsonCreator
  static HostConfig create(
      @JsonProperty("Binds") final List<String> binds,
      @JsonProperty("ContainerIDFile") final String containerIdFile,
      @JsonProperty("LxcConf") final List<LxcConfParameter> lxcConf,
      @JsonProperty("Privileged") final Boolean privileged,
      @JsonProperty("PortBindings") final Map<String, List<PortBinding>> portBindings,
      @JsonProperty("Links") final List<String> links,
      @JsonProperty("PublishAllPorts") final Boolean publishAllPorts,
      @JsonProperty("Dns") final List<String> dns,
      @JsonProperty("DnsSearch") final List<String> dnsSearch,
      @JsonProperty("ExtraHosts") final List<String> extraHosts,
      @JsonProperty("VolumesFrom") final List<String> volumesFrom,
      @JsonProperty("CapAdd") final List<String> capAdd,
      @JsonProperty("CapDrop") final List<String> capDrop,
      @JsonProperty("NetworkMode") final String networkMode,
      @JsonProperty("SecurityOpt") final List<String> securityOpt,
      @JsonProperty("Devices") final List<Device> devices,
      @JsonProperty("Memory") final Long memory,
      @JsonProperty("MemorySwap") final Long memorySwap,
      @JsonProperty("MemoryReservation") final Long memoryReservation,
      @JsonProperty("CpuShares") final Long cpuShares,
      @JsonProperty("CpusetCpus") final String cpusetCpus,
      @JsonProperty("CpuQuota") final Long cpuQuota,
      @JsonProperty("CgroupParent") final String cgroupParent,
      @JsonProperty("RestartPolicy") final RestartPolicy restartPolicy,
      @JsonProperty("LogConfig") final LogConfig logConfig,
      @JsonProperty("IpcMode") final String ipcMode,
      @JsonProperty("Ulimits") final List<Ulimit> ulimits,
      @JsonProperty("PidMode") final String pidMode,
      @JsonProperty("ShmSize") final Long shmSize,
      @JsonProperty("OomKillDisable") final Boolean oomKillDisable,
      @JsonProperty("OomScoreAdj") final Integer oomScoreAdj,
      @JsonProperty("AutoRemove") final Boolean autoRemove,
      @JsonProperty("PidsLimit") final Integer pidsLimit,
      @JsonProperty("Tmpfs") final Map<String, String> tmpfs,
      @JsonProperty("ReadonlyRootfs") final Boolean readonlyRootfs) {
    return builder()
        .binds(binds)
        .containerIdFile(containerIdFile)
        .lxcConf(lxcConf)
        .privileged(privileged)
        .portBindings(portBindings)
        .links(links)
        .publishAllPorts(publishAllPorts)
        .dns(dns)
        .dnsSearch(dnsSearch)
        .extraHosts(extraHosts)
        .volumesFrom(volumesFrom)
        .capAdd(capAdd)
        .capDrop(capDrop)
        .networkMode(networkMode)
        .securityOpt(securityOpt)
        .devices(devices)
        .memory(memory)
        .memorySwap(memorySwap)
        .memoryReservation(memoryReservation)
        .cpuShares(cpuShares)
        .cpusetCpus(cpusetCpus)
        .cpuQuota(cpuQuota)
        .cgroupParent(cgroupParent)
        .restartPolicy(restartPolicy)
        .logConfig(logConfig)
        .ipcMode(ipcMode)
        .ulimits(ulimits)
        .pidMode(pidMode)
        .shmSize(shmSize)
        .oomKillDisable(oomKillDisable)
        .oomScoreAdj(oomScoreAdj)
        .autoRemove(autoRemove)
        .pidsLimit(pidsLimit)
        .tmpfs(tmpfs)
        .readonlyRootfs(readonlyRootfs)
        .build();
  }

  @AutoValue
  public abstract static class LxcConfParameter {

    @NotNull
    @JsonProperty("Key")
    public abstract String key();

    @NotNull
    @JsonProperty("Value")
    public abstract String value();

    @JsonCreator
    static LxcConfParameter create(
        @JsonProperty("Key") final String key,
        @JsonProperty("Value") final String value) {
      return new AutoValue_HostConfig_LxcConfParameter(key, value);
    }
  }

  @AutoValue
  public abstract static class RestartPolicy {

    @NotNull
    @JsonProperty("Name")
    public abstract String name();

    @Nullable
    @JsonProperty("MaximumRetryCount")
    public abstract Integer maxRetryCount();

    public static RestartPolicy always() {
      return new AutoValue_HostConfig_RestartPolicy("always", null);
    }

    public static RestartPolicy unlessStopped() {
      return new AutoValue_HostConfig_RestartPolicy("unless-stopped", null);
    }

    public static RestartPolicy onFailure(Integer maxRetryCount) {
      return new AutoValue_HostConfig_RestartPolicy("on-failure", maxRetryCount);
    }

    @JsonCreator
    static RestartPolicy create(
        @JsonProperty("Name") final String name,
        @JsonProperty("MaximumRetryCount") final Integer maxRetryCount) {
      return new AutoValue_HostConfig_RestartPolicy(name, maxRetryCount);
    }
  }

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_HostConfig.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    /**
     * Set the list of binds to the parameter, replacing any existing value.
     * <p>To append to the list instead, use one of the appendBinds() methods.</p>
     *
     * @param binds A list of volume bindings for this container. Each volume binding is a string.
     * @return The builder
     */
    public abstract Builder binds(List<String> binds);

    /**
     * Set the list of binds to the parameter, replacing any existing value.
     * <p>To append to the list instead, use one of the appendBinds() methods.</p>
     *
     * @param binds An array of volume bindings for this container. Each volume binding is a
     *              string.
     * @return The builder
     */
    public abstract Builder binds(String... binds);

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

    abstract ImmutableList<String> binds();

    private static List<String> toStringList(final Bind[] binds) {
      final List<String> bindStrings = Lists.newArrayList();
      for (final Bind bind : binds) {
        bindStrings.add(bind.toString());
      }
      return bindStrings;
    }

    /**
     * Append binds to the existing list in this builder. Duplicates are discarded.
     *
     * @param newBinds An iterable of volume bindings for this container. Each volume binding is a
     *                 String.
     * @return The builder
     */
    public Builder appendBinds(final Iterable<String> newBinds) {
      final List<String> list = new ArrayList<>();
      if (binds() != null) {
        list.addAll(binds());
      }
      list.addAll(Lists.newArrayList(newBinds));
      binds(copyWithoutDuplicates(list));
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

    public abstract Builder containerIdFile(String containerIdFile);

    public abstract Builder lxcConf(List<LxcConfParameter> lxcConf);

    public abstract Builder lxcConf(LxcConfParameter... lxcConf);

    public abstract Builder privileged(Boolean privileged);

    public abstract Builder portBindings(Map<String, List<PortBinding>> portBindings);

    public abstract Builder links(List<String> links);

    public abstract Builder links(String... links);

    public abstract Builder publishAllPorts(Boolean publishAllPorts);

    public abstract Builder dns(List<String> dns);

    public abstract Builder dns(String... dns);

    public abstract Builder dnsSearch(List<String> dnsSearch);

    public abstract Builder dnsSearch(String... dnsSearch);

    public abstract Builder extraHosts(List<String> extraHosts);

    public abstract Builder extraHosts(String... extraHosts);

    public abstract Builder volumesFrom(List<String> volumesFrom);

    public abstract Builder volumesFrom(String... volumesFrom);

    public abstract Builder capAdd(List<String> capAdd);

    public abstract Builder capAdd(String... capAdd);

    public abstract Builder capDrop(List<String> capDrop);

    public abstract Builder capDrop(String... capDrop);

    public abstract Builder networkMode(String networkMode);

    public abstract Builder securityOpt(List<String> securityOpt);

    public abstract Builder securityOpt(String... securityOpt);

    public abstract Builder devices(List<Device> devices);

    public abstract Builder devices(Device... devices);

    public abstract Builder memory(Long memory);

    public abstract Builder memorySwap(Long memorySwap);

    public abstract Builder memoryReservation(Long memoryReservation);

    public abstract Builder cpuShares(Long cpuShares);

    public abstract Builder cpusetCpus(String cpusetCpus);

    public abstract Builder cpuQuota(Long cpuQuota);

    public abstract Builder cgroupParent(String cgroupParent);

    public abstract Builder restartPolicy(RestartPolicy restartPolicy);

    public abstract Builder logConfig(LogConfig logConfig);

    public abstract Builder ipcMode(String ipcMode);

    public abstract Builder ulimits(List<Ulimit> ulimits);

    public abstract Builder pidMode(String pidMode);

    /**
     * Set the PID (Process) Namespace mode for the container.
     * Use this method to join another container's PID namespace. To use the host
     * PID namespace, use {@link #hostPidMode()}.
     *
     * @param container Join the namespace of this container (Name or ID)
     * @return Builder
     */
    public Builder containerPidMode(final String container) {
      pidMode("container:" + container);
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
      pidMode("host");
      return this;
    }

    public abstract Builder shmSize(Long shmSize);

    public abstract Builder oomKillDisable(Boolean oomKillDisable);

    public abstract Builder oomScoreAdj(Integer oomScoreAdj);

    /**
     * Only works for Docker API version >= 1.25.
     */
    public abstract Builder autoRemove(Boolean autoRemove);

    public abstract Builder pidsLimit(Integer pidsLimit);

    public abstract Builder tmpfs(Map<String, String> tmpfs);

    public abstract Builder readonlyRootfs(Boolean readonlyRootfs);

    public abstract HostConfig build();
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

}
