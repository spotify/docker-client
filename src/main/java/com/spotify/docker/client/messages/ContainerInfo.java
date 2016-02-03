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
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, setterVisibility = NONE, getterVisibility = NONE)
public class ContainerInfo {

  @JsonProperty("Id") private String id;
  @JsonProperty("Created") private Date created;
  @JsonProperty("Path") private String path;
  @JsonProperty("Args") private ImmutableList<String> args;
  @JsonProperty("Config") private ContainerConfig config;
  @JsonProperty("HostConfig") private HostConfig hostConfig;
  @JsonProperty("State") private ContainerState state;
  @JsonProperty("Image") private String image;
  @JsonProperty("NetworkSettings") private NetworkSettings networkSettings;
  @JsonProperty("ResolvConfPath") private String resolvConfPath;
  @JsonProperty("HostnamePath") private String hostnamePath;
  @JsonProperty("HostsPath") private String hostsPath;
  @JsonProperty("Name") private String name;
  @JsonProperty("Driver") private String driver;
  @JsonProperty("ExecDriver") private String execDriver;
  @JsonProperty("ProcessLabel") private String processLabel;
  @JsonProperty("MountLabel") private String mountLabel;
  @JsonProperty("Volumes") private ImmutableMap<String, String> volumes;
  @JsonProperty("VolumesRW") private ImmutableMap<String, Boolean> volumesRW;
  @JsonProperty("AppArmorProfile") private String appArmorProfile;
  @JsonProperty("ExecIDs") private ImmutableList<String> execId;
  @JsonProperty("LogPath") private String logPath;
  @JsonProperty("RestartCount") private Long restartCount;
  @JsonProperty("Mounts") private ImmutableList<ContainerMount> mountsList;
  
  /**
   * This field is an extension defined by the Docker Swarm API, therefore it will only
   * be populated when communicating with a Swarm cluster.
   */
  @JsonProperty("Node") private Node node;

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

  public Map<String, String> volumes() {
    return volumes;
  }

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

    if (args != null ? !args.equals(that.args) : that.args != null) {
      return false;
    }
    if (config != null ? !config.equals(that.config) : that.config != null) {
      return false;
    }
    if (hostConfig != null ? !hostConfig.equals(that.hostConfig) : that.hostConfig != null) {
      return false;
    }
    if (created != null ? !created.equals(that.created) : that.created != null) {
      return false;
    }
    if (driver != null ? !driver.equals(that.driver) : that.driver != null) {
      return false;
    }
    if (execDriver != null ? !execDriver.equals(that.execDriver) : that.execDriver != null) {
      return false;
    }
    if (hostnamePath != null ? !hostnamePath.equals(that.hostnamePath)
                             : that.hostnamePath != null) {
      return false;
    }
    if (hostsPath != null ? !hostsPath.equals(that.hostsPath) : that.hostsPath != null) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (image != null ? !image.equals(that.image) : that.image != null) {
      return false;
    }
    if (mountLabel != null ? !mountLabel.equals(that.mountLabel) : that.mountLabel != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (networkSettings != null ? !networkSettings.equals(that.networkSettings)
                                : that.networkSettings != null) {
      return false;
    }
    if (path != null ? !path.equals(that.path) : that.path != null) {
      return false;
    }
    if (processLabel != null ? !processLabel.equals(that.processLabel)
                             : that.processLabel != null) {
      return false;
    }
    if (resolvConfPath != null ? !resolvConfPath.equals(that.resolvConfPath)
                               : that.resolvConfPath != null) {
      return false;
    }
    if (state != null ? !state.equals(that.state) : that.state != null) {
      return false;
    }
    if (volumes != null ? !volumes.equals(that.volumes) : that.volumes != null) {
      return false;
    }
    if (volumesRW != null ? !volumesRW.equals(that.volumesRW) : that.volumesRW != null) {
      return false;
    }
    if (node != null ? !node.equals(that.node) : that.node != null) {
      return false;
    }
    if (appArmorProfile != null ? !appArmorProfile.equals(that.appArmorProfile) : 
        that.appArmorProfile != null) {
        return false;
      }
    if (execId != null ? !execId.equals(that.execId) : that.execId != null) {
        return false;
      }
    if (logPath != null ? !logPath.equals(that.logPath) : that.logPath != null) {
        return false;
      }
    if (restartCount != null ? !restartCount.equals(that.restartCount) : 
        that.restartCount != null) {
        return false;
      }
    if (mountsList != null ? !mountsList.equals(that.mountsList) : that.mountsList != null) {
        return false;
      }

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (created != null ? created.hashCode() : 0);
    result = 31 * result + (path != null ? path.hashCode() : 0);
    result = 31 * result + (args != null ? args.hashCode() : 0);
    result = 31 * result + (config != null ? config.hashCode() : 0);
    result = 31 * result + (hostConfig != null ? hostConfig.hashCode() : 0);
    result = 31 * result + (state != null ? state.hashCode() : 0);
    result = 31 * result + (image != null ? image.hashCode() : 0);
    result = 31 * result + (networkSettings != null ? networkSettings.hashCode() : 0);
    result = 31 * result + (resolvConfPath != null ? resolvConfPath.hashCode() : 0);
    result = 31 * result + (hostnamePath != null ? hostnamePath.hashCode() : 0);
    result = 31 * result + (hostsPath != null ? hostsPath.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (driver != null ? driver.hashCode() : 0);
    result = 31 * result + (execDriver != null ? execDriver.hashCode() : 0);
    result = 31 * result + (processLabel != null ? processLabel.hashCode() : 0);
    result = 31 * result + (mountLabel != null ? mountLabel.hashCode() : 0);
    result = 31 * result + (volumes != null ? volumes.hashCode() : 0);
    result = 31 * result + (volumesRW != null ? volumesRW.hashCode() : 0);
    result = 31 * result + (node != null ? node.hashCode() : 0);
    result = 31 * result + (appArmorProfile != null ? appArmorProfile.hashCode() : 0);
    result = 31 * result + (execId != null ? execId.hashCode() : 0);
    result = 31 * result + (logPath != null ? logPath.hashCode() : 0);
    result = 31 * result + (restartCount != null ? restartCount.hashCode() : 0);
    result = 31 * result + (mountsList != null ? mountsList.hashCode() : 0);
    return result;
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
        .add("mounts", mountsList)
        .toString();
  }

  public static class Node {
    @JsonProperty("Id") private String id;
    @JsonProperty("Ip") private String ip;
    @JsonProperty("Addr") private String addr;
    @JsonProperty("Name") private String name;

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
      Node node = (Node) o;
      return Objects.equal(id, node.id) &&
              Objects.equal(ip, node.ip) &&
              Objects.equal(addr, node.addr) &&
              Objects.equal(name, node.name);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(id, ip, addr, name);
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
    return mountsList;
  }
}
