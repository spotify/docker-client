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

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, setterVisibility = NONE, getterVisibility = NONE)
public abstract class ContainerInfo {

  @Nullable
  @JsonProperty("Id")
  public abstract String id();

  @JsonProperty("Created")
  public abstract Date created();

  @JsonProperty("Path")
  public abstract String path();

  @JsonProperty("Args")
  public abstract ImmutableList<String> args();

  @JsonProperty("Config")
  public abstract ContainerConfig config();

  @Nullable
  @JsonProperty("HostConfig")
  public abstract HostConfig hostConfig();

  @JsonProperty("State")
  public abstract ContainerState state();

  @JsonProperty("Image")
  public abstract String image();

  @JsonProperty("NetworkSettings")
  public abstract NetworkSettings networkSettings();

  @JsonProperty("ResolvConfPath")
  public abstract String resolvConfPath();

  @JsonProperty("HostnamePath")
  public abstract String hostnamePath();

  @JsonProperty("HostsPath")
  public abstract String hostsPath();

  @JsonProperty("Name")
  public abstract String name();

  @JsonProperty("Driver")
  public abstract String driver();

  @Nullable
  @JsonProperty("ExecDriver")
  public abstract String execDriver();

  @JsonProperty("ProcessLabel")
  public abstract String processLabel();

  @JsonProperty("MountLabel")
  public abstract String mountLabel();

  /**
   * Volumes returned by execInspect
   *
   * @return A map of volumes where the key is the source path on the local file system, and the key
   *         is the target path on the Docker host.
   * @deprecated Replaced by {@link #mounts()} in API 1.20.
   */
  @Nullable
  @Deprecated
  @JsonProperty("Volumes")
  public abstract ImmutableMap<String, String> volumes();

  /**
   * Volumes returned by execInspect
   *
   * @return A map of volumes where the key is the source path on the local file system, and the key
   *         is the target path on the Docker host.
   * @deprecated Replaced by {@link #mounts()} in API 1.20.
   */
  @Nullable
  @Deprecated
  @JsonProperty("VolumesRW")
  public abstract ImmutableMap<String, Boolean> volumesRw();

  @JsonProperty("AppArmorProfile")
  public abstract String appArmorProfile();

  @Nullable
  @JsonProperty("ExecIDs")
  public abstract ImmutableList<String> execIds();

  @JsonProperty("LogPath")
  public abstract String logPath();

  @JsonProperty("RestartCount")
  public abstract Long restartCount();

  @Nullable
  @JsonProperty("Mounts")
  public abstract ImmutableList<ContainerMount> mounts();

  /**
   * This field is an extension defined by the Docker Swarm API, therefore it will only be populated
   * when communicating with a Swarm cluster.
   */
  @Nullable
  @JsonProperty("Node")
  public abstract Node node();

  @JsonCreator
  static ContainerInfo create(
      @JsonProperty("Id") final String id,
      @JsonProperty("Created") final Date created,
      @JsonProperty("Path") final String path,
      @JsonProperty("Args") final List<String> args,
      @JsonProperty("Config") final ContainerConfig containerConfig,
      @JsonProperty("HostConfig") final HostConfig hostConfig,
      @JsonProperty("State") final ContainerState containerState,
      @JsonProperty("Image") final String image,
      @JsonProperty("NetworkSettings") final NetworkSettings networkSettings,
      @JsonProperty("ResolvConfPath") final String resolvConfPath,
      @JsonProperty("HostnamePath") final String hostnamePath,
      @JsonProperty("HostsPath") final String hostsPath,
      @JsonProperty("Name") final String name,
      @JsonProperty("Driver") final String driver,
      @JsonProperty("ExecDriver") final String execDriver,
      @JsonProperty("ProcessLabel") final String processLabel,
      @JsonProperty("MountLabel") final String mountLabel,
      @JsonProperty("Volumes") final Map<String, String> volumes,
      @JsonProperty("VolumesRW") final Map<String, Boolean> volumesRw,
      @JsonProperty("AppArmorProfile") final String appArmorProfile,
      @JsonProperty("ExecIDs") final List<String> execIds,
      @JsonProperty("LogPath") final String logPath,
      @JsonProperty("RestartCount") final Long restartCount,
      @JsonProperty("Mounts") final List<ContainerMount> mounts,
      @JsonProperty("Node") final Node node) {
    final ImmutableMap<String, String> volumesCopy = volumes == null
                                                     ? null : ImmutableMap.copyOf(volumes);
    final ImmutableMap<String, Boolean> volumesRwCopy = volumesRw == null
                                                        ? null : ImmutableMap.copyOf(volumesRw);
    final ImmutableList<String> execIdsCopy = execIds == null
                                              ? null : ImmutableList.copyOf(execIds);
    final ImmutableList<ContainerMount> mountsCopy = mounts == null
                                                     ? null : ImmutableList.copyOf(mounts);
    return new AutoValue_ContainerInfo(
        id, created, path, ImmutableList.copyOf(args), containerConfig, hostConfig, containerState,
        image, networkSettings, resolvConfPath, hostnamePath, hostsPath, name, driver, execDriver,
        processLabel, mountLabel, volumesCopy, volumesRwCopy,
        appArmorProfile, execIdsCopy, logPath, restartCount, mountsCopy, node);
  }

  @AutoValue
  public abstract static class Node {

    @JsonProperty("ID")
    public abstract String id();

    @JsonProperty("IP")
    public abstract String ip();

    @JsonProperty("Addr")
    public abstract String addr();

    @JsonProperty("Name")
    public abstract String name();

    @JsonCreator
    static Node create(
        @JsonProperty("ID") final String id,
        @JsonProperty("IP") final String ip,
        @JsonProperty("Addr") final String addr,
        @JsonProperty("Name") final String name) {
      return new AutoValue_ContainerInfo_Node(id, ip, addr, name);
    }
  }
}
