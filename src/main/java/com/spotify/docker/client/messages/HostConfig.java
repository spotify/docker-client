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
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class HostConfig {

  @Nullable
  @JsonProperty("Binds")
  public abstract ImmutableList<String> binds();

  @Nullable
  @JsonProperty("BlkioWeight")
  public abstract Integer blkioWeight();

  @Nullable
  @JsonProperty("BlkioWeightDevice")
  public abstract ImmutableList<BlkioWeightDevice> blkioWeightDevice();

  @Nullable
  @JsonProperty("BlkioDeviceReadBps")
  public abstract ImmutableList<BlkioDeviceRate> blkioDeviceReadBps();

  @Nullable
  @JsonProperty("BlkioDeviceWriteBps")
  public abstract ImmutableList<BlkioDeviceRate> blkioDeviceWriteBps();

  @Nullable
  @JsonProperty("BlkioDeviceReadIOps")
  public abstract ImmutableList<BlkioDeviceRate> blkioDeviceReadIOps();

  @Nullable
  @JsonProperty("BlkioDeviceWriteIOps")
  public abstract ImmutableList<BlkioDeviceRate> blkioDeviceWriteIOps();

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
  @JsonProperty("DnsOptions")
  public abstract ImmutableList<String> dnsOptions();

  @Nullable
  @JsonProperty("DnsSearch")
  public abstract ImmutableList<String> dnsSearch();

  @Nullable
  @JsonProperty("ExtraHosts")
  public abstract ImmutableList<String> extraHosts();
  
  @Nullable
  @JsonProperty("GroupAdd")
  public abstract ImmutableList<String> groupAdd();
  
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
  @JsonProperty("KernelMemory")
  public abstract Long kernelMemory();

  @Nullable
  @JsonProperty("MemorySwappiness")
  public abstract Integer memorySwappiness();

  @Nullable
  @JsonProperty("MemoryReservation")
  public abstract Long memoryReservation();

  @Nullable
  @JsonProperty("NanoCpus")
  public abstract Long nanoCpus();

  @Nullable
  @JsonProperty("CpuPeriod")
  public abstract Long cpuPeriod();

  @Nullable
  @JsonProperty("CpuShares")
  public abstract Long cpuShares();

  @Nullable
  @JsonProperty("CpusetCpus")
  public abstract String cpusetCpus();

  @Nullable
  @JsonProperty("CpusetMems")
  public abstract String cpusetMems();

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
   * Only works for kernels &gt;= 4.3
   * @return An integer indicating the pids limit.
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
  
  @Nullable
  @JsonProperty("StorageOpt")
  public abstract ImmutableMap<String, String> storageOpt();

  @Nullable
  @JsonProperty("Runtime")
  public abstract String runtime();


  @JsonCreator
  static HostConfig create(
      @JsonProperty("Binds") final List<String> binds,
      @JsonProperty("BlkioWeight") final Integer blkioWeight,
      @JsonProperty("BlkioWeightDevice") final List<BlkioWeightDevice> blkioWeightDevice,
      @JsonProperty("BlkioDeviceReadBps") final List<BlkioDeviceRate> blkioDeviceReadBps,
      @JsonProperty("BlkioDeviceWriteBps") final List<BlkioDeviceRate> blkioDeviceWriteBps,
      @JsonProperty("BlkioDeviceReadIOps") final List<BlkioDeviceRate> blkioDeviceReadIOps,
      @JsonProperty("BlkioDeviceWriteIOps") final List<BlkioDeviceRate> blkioDeviceWriteIOps,
      @JsonProperty("ContainerIDFile") final String containerIdFile,
      @JsonProperty("LxcConf") final List<LxcConfParameter> lxcConf,
      @JsonProperty("Privileged") final Boolean privileged,
      @JsonProperty("PortBindings") final Map<String, List<PortBinding>> portBindings,
      @JsonProperty("Links") final List<String> links,
      @JsonProperty("PublishAllPorts") final Boolean publishAllPorts,
      @JsonProperty("Dns") final List<String> dns,
      @JsonProperty("DnsOptions") final List<String> dnsOptions,
      @JsonProperty("DnsSearch") final List<String> dnsSearch,
      @JsonProperty("ExtraHosts") final List<String> extraHosts,
      @JsonProperty("GroupAdd") final List<String> groupAdd,
      @JsonProperty("VolumesFrom") final List<String> volumesFrom,
      @JsonProperty("CapAdd") final List<String> capAdd,
      @JsonProperty("CapDrop") final List<String> capDrop,
      @JsonProperty("NetworkMode") final String networkMode,
      @JsonProperty("SecurityOpt") final List<String> securityOpt,
      @JsonProperty("Devices") final List<Device> devices,
      @JsonProperty("Memory") final Long memory,
      @JsonProperty("MemorySwap") final Long memorySwap,
      @JsonProperty("MemorySwappiness") final Integer memorySwappiness,
      @JsonProperty("MemoryReservation") final Long memoryReservation,
      @JsonProperty("KernelMemory") final Long kernelMemory,
      @JsonProperty("NanoCpus") final Long nanoCpus,
      @JsonProperty("CpuPeriod") final Long cpuPeriod,
      @JsonProperty("CpuShares") final Long cpuShares,
      @JsonProperty("CpusetCpus") final String cpusetCpus,
      @JsonProperty("CpusetMems") final String cpusetMems,
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
      @JsonProperty("ReadonlyRootfs") final Boolean readonlyRootfs,
      @JsonProperty("Runtime") final String runtime,
      @JsonProperty("StorageOpt") final Map<String, String> storageOpt) {
    return builder()
        .binds(binds)
        .blkioWeight(blkioWeight)
        .blkioWeightDevice(blkioWeightDevice)
        .blkioDeviceReadBps(blkioDeviceReadBps)
        .blkioDeviceWriteBps(blkioDeviceWriteBps)
        .blkioDeviceReadIOps(blkioDeviceReadIOps)
        .blkioDeviceWriteIOps(blkioDeviceWriteIOps)
        .containerIdFile(containerIdFile)
        .lxcConf(lxcConf)
        .privileged(privileged)
        .portBindings(portBindings)
        .links(links)
        .publishAllPorts(publishAllPorts)
        .dns(dns)
        .dnsOptions(dnsOptions)
        .dnsSearch(dnsSearch)
        .extraHosts(extraHosts)
        .groupAdd( groupAdd )
        .volumesFrom(volumesFrom)
        .capAdd(capAdd)
        .capDrop(capDrop)
        .networkMode(networkMode)
        .securityOpt(securityOpt)
        .devices(devices)
        .memory(memory)
        .memorySwap(memorySwap)
        .memorySwappiness(memorySwappiness)
        .memoryReservation(memoryReservation)
        .kernelMemory(kernelMemory)
        .nanoCpus(nanoCpus)
        .cpuPeriod(cpuPeriod)
        .cpuShares(cpuShares)
        .cpusetCpus(cpusetCpus)
        .cpusetMems(cpusetMems)
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
        .storageOpt(storageOpt)
        .runtime(runtime)
        .build();
  }

  @AutoValue
  public abstract static class LxcConfParameter {

    @JsonProperty("Key")
    public abstract String key();

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
     * @return {@link Builder}
     */
    public abstract Builder binds(List<String> binds);

    /**
     * Set the list of binds to the parameter, replacing any existing value.
     * <p>To append to the list instead, use one of the appendBinds() methods.</p>
     *
     * @param binds An array of volume bindings for this container. Each volume binding is a
     *              string.
     * @return {@link Builder}
     */
    public abstract Builder binds(String... binds);

    /**
     * Set the list of binds to the parameter, replacing any existing value.
     * <p>To append to the list instead, use one of the appendBinds() methods.</p>
     *
     * @param binds An array of volume bindings for this container. Each volume binding is a {@link
     *              Bind} object.
     * @return {@link Builder}
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
     * @return {@link Builder}
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
     * @return {@link Builder}
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
     * @return {@link Builder}
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

    public abstract Builder blkioWeight(Integer blkioWeight);

    public abstract Builder blkioWeightDevice(List<BlkioWeightDevice> blkioWeightDevice);

    public abstract Builder blkioDeviceReadBps(List<BlkioDeviceRate> blkioDeviceReadBps);

    public abstract Builder blkioDeviceWriteBps(List<BlkioDeviceRate> blkioDeviceWriteBps);

    public abstract Builder blkioDeviceReadIOps(List<BlkioDeviceRate> blkioDeviceReadIOps);

    public abstract Builder blkioDeviceWriteIOps(List<BlkioDeviceRate> blkioDeviceWriteIOps);

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

    public abstract Builder dnsOptions(List<String> dnsOptions);

    public abstract Builder dnsOptions(String... dnsOptions);

    public abstract Builder dnsSearch(List<String> dnsSearch);

    public abstract Builder dnsSearch(String... dnsSearch);

    public abstract Builder extraHosts(List<String> extraHosts);

    public abstract Builder extraHosts(String... extraHosts);
    
    public abstract Builder groupAdd(List<String> groupAdd);

    public abstract Builder groupAdd(String... groupAdd);

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

    public abstract Builder memorySwappiness(Integer memorySwappiness);

    public abstract Builder kernelMemory(Long kernelMemory);

    public abstract Builder memoryReservation(Long memoryReservation);

    public abstract Builder nanoCpus(Long nanoCpus);

    public abstract Builder cpuPeriod(Long cpuPeriod);

    public abstract Builder cpuShares(Long cpuShares);

    public abstract Builder cpusetCpus(String cpusetCpus);

    public abstract Builder cpusetMems(String cpusetMems);

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
     * @return {@link Builder}
     */
    public Builder hostPidMode() {
      pidMode("host");
      return this;
    }

    public abstract Builder shmSize(Long shmSize);

    public abstract Builder oomKillDisable(Boolean oomKillDisable);

    public abstract Builder oomScoreAdj(Integer oomScoreAdj);

    /**
     * Only works for Docker API version &gt;= 1.25.
     * @param autoRemove Whether to automatically remove the container when it exits
     * @return {@link Builder}
     */
    public abstract Builder autoRemove(Boolean autoRemove);

    public abstract Builder pidsLimit(Integer pidsLimit);

    public abstract Builder tmpfs(Map<String, String> tmpfs);

    public abstract Builder readonlyRootfs(Boolean readonlyRootfs);
    
    public abstract Builder storageOpt(Map<String, String> tmpfs);

    public abstract Builder runtime(String runtime);

    // Validation of property values using AutoValue requires we split the build method into two.
    // AutoValue implements this package-private method.
    // See https://github.com/google/auto/blob/master/value/userguide/builders-howto.md#validate.
    abstract HostConfig autoBuild();

    public HostConfig build() {
      final HostConfig hostConfig = autoBuild();
      validateExtraHosts(hostConfig.extraHosts());
      return hostConfig;
    }
  }

  private static void validateExtraHosts(final List<String> extraHosts) {
    if (extraHosts != null) {
      for (final String extraHost : extraHosts) {
        checkArgument(extraHost.contains(":"),
            "extra host arg '%s' must contain a ':'", extraHost);
      }
    }
  }

  @AutoValue
  public abstract static class Bind {

    public abstract String to();

    public static BuilderTo to(final String to) {
      return BuilderTo.create(to);
    }

    public abstract String from();

    public static BuilderFrom from(final String from) {
      return BuilderFrom.create(from);
    }

    public static BuilderFrom from(final Volume volumeFrom) {
      return BuilderFrom.create(volumeFrom);
    }

    public abstract Boolean readOnly();

    @Nullable
    public abstract Boolean noCopy();

    @Nullable
    public abstract Boolean selinuxLabeling();

    public static Builder builder() {
      return new AutoValue_HostConfig_Bind.Builder().readOnly(false);
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public String toString() {
      if (isNullOrEmpty(to())) {
        return "";
      } else if (isNullOrEmpty(from())) {
        return to();
      }

      final String bind = from() + ":" + to();

      final List<String> options = new ArrayList<>();
      if (readOnly()) {
        options.add("ro");
      }
      //noinspection ConstantConditions
      if (noCopy() != null && noCopy()) {
        options.add("nocopy");
      }

      if (selinuxLabeling() != null) {
        // shared
        if (Boolean.TRUE.equals(selinuxLabeling())) {
          options.add("z");
        } else {
          options.add("Z");
        }
      }

      final String optionsValue = Joiner.on(',').join(options);

      return (optionsValue.isEmpty()) ? bind : bind + ":" + optionsValue;
    }

    @AutoValue
    public abstract static class BuilderTo {

      public abstract String to();

      public static BuilderTo create(final String to) {
        return new AutoValue_HostConfig_Bind_BuilderTo(to);
      }

      public Builder from(final String from) {
        return builder().to(to()).from(from);
      }

      public Builder from(final Volume volumeFrom) {
        return builder().to(to()).from(volumeFrom);
      }
    }

    @AutoValue
    public abstract static class BuilderFrom {

      public abstract String from();

      public static BuilderFrom create(final String from) {
        return new AutoValue_HostConfig_Bind_BuilderFrom(from);
      }

      public static BuilderFrom create(final Volume volumeFrom) {
        return new AutoValue_HostConfig_Bind_BuilderFrom(
            checkNotNull(volumeFrom.name(), "Volume name"));
      }

      public Builder to(final String to) {
        return builder().to(to).from(from());
      }
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder to(String to);

      public abstract Builder from(String from);

      public Builder from(final Volume volumeFrom) {
        from(checkNotNull(volumeFrom.name(), "Volume name"));
        return this;
      }

      public abstract Builder readOnly(Boolean readOnly);

      public abstract Builder noCopy(Boolean noCopy);

      /**
       * Turn on automatic SELinux labeling of the host file or directory being
       * mounted into the container.
       * @param sharedContent True if this bind mount content is shared among multiple 
       *     containers (mount option "z"); false if private and unshared (mount option "Z")
       * @return {@link Builder}
       */
      public abstract Builder selinuxLabeling(Boolean sharedContent);

      public abstract Bind build();
    }
  }

  @AutoValue
  public abstract static class Ulimit {

    @JsonProperty("Name")
    public abstract String name();

    @JsonProperty("Soft")
    public abstract Long soft();

    @JsonProperty("Hard")
    public abstract Long hard();

    public static Builder builder() {
      return new AutoValue_HostConfig_Ulimit.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder name(String name);

      public abstract Builder soft(Long soft);

      public abstract Builder hard(Long hard);

      public abstract Ulimit build();
    }

    @JsonCreator
    public static Ulimit create(
        @JsonProperty("Name") final String name,
        @JsonProperty("Soft") final Long soft,
        @JsonProperty("Hard") final Long hard) {
      return builder()
          .name(name)
          .soft(soft)
          .hard(hard)
          .build();
    }
  }

  @AutoValue
  public abstract static class BlkioWeightDevice {

    @JsonProperty("Path")
    public abstract String path();

    @JsonProperty("Weight")
    public abstract Integer weight();

    public static Builder builder() {
      return new AutoValue_HostConfig_BlkioWeightDevice.Builder();
    }

    @JsonCreator
    static BlkioWeightDevice create(
        @JsonProperty("Path") final String path,
        @JsonProperty("Weight") final Integer weight) {
      return builder().path(path).weight(weight).build();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder path(final String path);

      public abstract Builder weight(final Integer weight);

      public abstract BlkioWeightDevice build();
    }
  }

  @AutoValue
  public abstract static class BlkioDeviceRate {

    @JsonProperty("Path")
    public abstract String path();

    @JsonProperty("Rate")
    public abstract Integer rate();

    @JsonCreator
    static BlkioDeviceRate create(
        @JsonProperty("Path") final String path,
        @JsonProperty("Rate") final Integer rate) {
      return builder().path(path).rate(rate).build();
    }

    public static Builder builder() {
      return new AutoValue_HostConfig_BlkioDeviceRate.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder path(final String path);

      public abstract Builder rate(final Integer rate);

      public abstract BlkioDeviceRate build();
    }
  }
}
