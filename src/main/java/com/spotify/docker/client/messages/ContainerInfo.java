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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, setterVisibility = NONE, getterVisibility = NONE)
public class ContainerInfo {

  @JsonProperty("Id")
  private String id;
  @JsonProperty("Created")
  private Date created;
  @JsonProperty("Path")
  private String path;
  @JsonProperty("Args")
  private ImmutableList<String> args;
  @JsonProperty("Config")
  private ContainerConfig config;
  @JsonProperty("HostConfig")
  private HostConfig hostConfig;
  @JsonProperty("State")
  private ContainerState state;
  @JsonProperty("Image")
  private String image;
  @JsonProperty("NetworkSettings")
  private NetworkSettings networkSettings;
  @JsonProperty("ResolvConfPath")
  private String resolvConfPath;
  @JsonProperty("HostnamePath")
  private String hostnamePath;
  @JsonProperty("HostsPath")
  private String hostsPath;
  @JsonProperty("Name")
  private String name;
  @JsonProperty("Driver")
  private String driver;
  @JsonProperty("ExecDriver")
  private String execDriver;
  @JsonProperty("ProcessLabel")
  private String processLabel;
  @JsonProperty("MountLabel")
  private String mountLabel;
  @JsonProperty("Volumes")
  private ImmutableMap<String, String> volumes;
  @JsonProperty("VolumesRW")
  private ImmutableMap<String, Boolean> volumesRW;
  @JsonProperty("AppArmorProfile")
  private String appArmorProfile;
  @JsonProperty("ExecIDs")
  private ImmutableList<String> execId;
  @JsonProperty("LogPath")
  private String logPath;
  @JsonProperty("RestartCount")
  private Long restartCount;
  @JsonProperty("Mounts")
  private ImmutableList<ContainerMount> mounts;

  /**
   * This field is an extension defined by the Docker Swarm API, therefore it will only be populated
   * when communicating with a Swarm cluster.
   */
  @JsonProperty("Node")
  private Node node;

  public String id() {
    return id;
  }

  public Date created() {
    return created == null ? null : new Date(created.getTime());
  }

  public String path() {
    return path;
  }

  public List<String> args() {
    return args;
  }

  public ContainerConfig config() {
    return config;
  }

  public HostConfig hostConfig() {
    return hostConfig;
  }

  public ContainerState state() {
    return state;
  }

  public String image() {
    return image;
  }

  public NetworkSettings networkSettings() {
    return networkSettings;
  }

  public String resolvConfPath() {
    return resolvConfPath;
  }

  public String hostnamePath() {
    return hostnamePath;
  }

  public String hostsPath() {
    return hostsPath;
  }

  public String name() {
    return name;
  }

  public String driver() {
    return driver;
  }

  public String execDriver() {
    return execDriver;
  }

  public String processLabel() {
    return processLabel;
  }

  public String mountLabel() {
    return mountLabel;
  }

  /**
   * Volumes returned by execInspect
   *
   * @return A map of volumes where the key is the source path on the local file system, and the key
   * is the target path on the Docker host.
   * @deprecated Replaced by {@link #mounts()} in API 1.20.
   */
  @Deprecated
  public Map<String, String> volumes() {
    return volumes;
  }

  /**
   * Volumes returned by execInspect
   *
   * @return A map of volumes where the key is the source path on the local file system, and the key
   * is the target path on the Docker host.
   * @deprecated Replaced by {@link #mounts()} in API 1.20.
   */
  @Deprecated
  public Map<String, Boolean> volumesRW() {
    return volumesRW;
  }

  public Node node() {
    return node;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ContainerInfo that = (ContainerInfo) o;

    return Objects.equals(this.id, that.id) &&
           Objects.equals(this.created, that.created) &&
           Objects.equals(this.path, that.path) &&
           Objects.equals(this.args, that.args) &&
           Objects.equals(this.config, that.config) &&
           Objects.equals(this.hostConfig, that.hostConfig) &&
           Objects.equals(this.state, that.state) &&
           Objects.equals(this.image, that.image) &&
           Objects.equals(this.networkSettings, that.networkSettings) &&
           Objects.equals(this.resolvConfPath, that.resolvConfPath) &&
           Objects.equals(this.hostnamePath, that.hostnamePath) &&
           Objects.equals(this.hostsPath, that.hostsPath) &&
           Objects.equals(this.name, that.name) &&
           Objects.equals(this.driver, that.driver) &&
           Objects.equals(this.execDriver, that.execDriver) &&
           Objects.equals(this.processLabel, that.processLabel) &&
           Objects.equals(this.mountLabel, that.mountLabel) &&
           Objects.equals(this.volumes, that.volumes) &&
           Objects.equals(this.volumesRW, that.volumesRW) &&
           Objects.equals(this.appArmorProfile, that.appArmorProfile) &&
           Objects.equals(this.execId, that.execId) &&
           Objects.equals(this.logPath, that.logPath) &&
           Objects.equals(this.restartCount, that.restartCount) &&
           Objects.equals(this.mounts, that.mounts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id, created, path, args, config, hostConfig, state, image,
        networkSettings, resolvConfPath, hostnamePath, hostsPath, name, driver, execDriver,
        processLabel, mountLabel, volumes, volumesRW, node, appArmorProfile,
        execId, logPath, restartCount, mounts);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("created", created)
        .add("path", path)
        .add("args", args)
        .add("config", config)
        .add("hostConfig", hostConfig)
        .add("state", state)
        .add("image", image)
        .add("networkSettings", networkSettings)
        .add("resolvConfPath", resolvConfPath)
        .add("hostnamePath", hostnamePath)
        .add("hostsPath", hostsPath)
        .add("name", name)
        .add("driver", driver)
        .add("execDriver", execDriver)
        .add("processLabel", processLabel)
        .add("mountLabel", mountLabel)
        .add("volumes", volumes)
        .add("volumesRW", volumesRW)
        .add("node", node)
        .add("appArmorProfile", appArmorProfile)
        .add("execIDs", execId)
        .add("logPath", logPath)
        .add("restartCount", restartCount)
        .add("mounts", mounts)
        .toString();
  }

  public static class Node {

    @JsonProperty("Id")
    private String id;
    @JsonProperty("Ip")
    private String ip;
    @JsonProperty("Addr")
    private String addr;
    @JsonProperty("Name")
    private String name;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getIp() {
      return ip;
    }

    public void setIp(String ip) {
      this.ip = ip;
    }

    public String getAddr() {
      return addr;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      final Node that = (Node) o;

      return Objects.equals(this.id, that.id) &&
             Objects.equals(this.ip, that.ip) &&
             Objects.equals(this.addr, that.addr) &&
             Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, ip, addr, name);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("ip", ip)
          .add("addr", addr)
          .add("name", name)
          .toString();
    }
  }

  public String appArmorProfile() {
    return appArmorProfile;
  }

  public List<String> execId() {
    return execId;
  }

  public String logPath() {
    return logPath;
  }

  public Long restartCount() {
    return restartCount;
  }

  public List<ContainerMount> mounts() {
    return mounts;
  }
}
