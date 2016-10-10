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
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Container {

  @JsonProperty("Id")
  private String id;
  @JsonProperty("Names")
  private ImmutableList<String> names;
  @JsonProperty("Image")
  private String image;
  @JsonProperty("ImageID")
  private String imageId;
  @JsonProperty("Command")
  private String command;
  @JsonProperty("Created")
  private Long created;
  @JsonProperty("State")
  private String state;
  @JsonProperty("Status")
  private String status;
  @JsonProperty("Ports")
  private ImmutableList<PortMapping> ports;
  @JsonProperty("Labels")
  private ImmutableMap<String, String> labels;
  @JsonProperty("SizeRw")
  private Long sizeRw;
  @JsonProperty("SizeRootFs")
  private Long sizeRootFs;
  @JsonProperty("NetworkSettings")
  private NetworkSettings networkSettings;
  @JsonProperty("Mounts")
  private ImmutableList<ContainerMount> mounts;

  /**
   * This method returns port information the way that <code>docker ps</code> does:
   * <code>0.0.0.0:5432-&gt;5432/tcp</code> or <code>6379/tcp</code>
   *
   * It should not be used to extract detailed information of ports. To do so, please refer to
   * {@link com.spotify.docker.client.messages.PortBinding}
   *
   * @return port information as docker ps does.
   * @see com.spotify.docker.client.messages.PortBinding
   */
  public String portsAsString() {
    final StringBuilder sb = new StringBuilder();
    if (this.ports != null) {
      for (final PortMapping port : this.ports) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        if (port.ip != null) {
          sb.append(port.ip).append(":");
        }
        if (port.publicPort > 0) {
          sb.append(port.privatePort).append("->").append(port.publicPort);
        } else {
          sb.append(port.privatePort);
        }
        sb.append("/").append(port.type);
      }
    }

    return sb.toString();
  }

  public String id() {
    return id;
  }

  public List<String> names() {
    return names;
  }

  public String image() {
    return image;
  }

  public String imageId() {
    return imageId;
  }

  public String command() {
    return command;
  }

  public Long created() {
    return created;
  }

  public String state() {
    return state;
  }

  public String status() {
    return status;
  }

  public List<PortMapping> ports() {
    return ports;
  }

  public Map<String, String> labels() {
    return labels;
  }

  public Long sizeRw() {
    return sizeRw;
  }

  public Long sizeRootFs() {
    return sizeRootFs;
  }

  public NetworkSettings networkSettings() {
    return networkSettings;
  }

  public List<ContainerMount> mounts() {
    return mounts;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Container that = (Container) o;

    return Objects.equals(this.id, that.id) &&
           Objects.equals(this.names, that.names) &&
           Objects.equals(this.image, that.image) &&
           Objects.equals(this.imageId, that.imageId) &&
           Objects.equals(this.command, that.command) &&
           Objects.equals(this.created, that.created) &&
           Objects.equals(this.state, that.state) &&
           Objects.equals(this.status, that.status) &&
           Objects.equals(this.ports, that.ports) &&
           Objects.equals(this.labels, that.labels) &&
           Objects.equals(this.sizeRw, that.sizeRw) &&
           Objects.equals(this.sizeRootFs, that.sizeRootFs) &&
           Objects.equals(this.networkSettings, that.networkSettings) &&
           Objects.equals(this.mounts, that.mounts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, names, image, command, created, status, ports, labels, sizeRw,
                        sizeRootFs, imageId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("image", image)
        .add("command", command)
        .add("created", created)
        .add("state", state)
        .add("status", status)
        .add("ports", ports)
        .add("labels", labels)
        .add("sizeRw", sizeRw)
        .add("sizeRootFs", sizeRootFs)
        .add("imageId", imageId)
        .add("networkSettings", networkSettings)
        .add("mounts", mounts)
        .toString();
  }

  public static class PortMapping {

    @JsonProperty("PrivatePort")
    private int privatePort;
    @JsonProperty("PublicPort")
    private int publicPort;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("IP")
    private String ip;

    public String getIp() {
      return ip;
    }

    public int getPrivatePort() {
      return privatePort;
    }

    public int getPublicPort() {
      return publicPort;
    }

    public String getType() {
      return type;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final PortMapping that = (PortMapping) o;

      return Objects.equals(this.privatePort, that.privatePort) &&
             Objects.equals(this.publicPort, that.publicPort) &&
             Objects.equals(this.type, that.type) &&
             Objects.equals(this.ip, that.ip);
    }

    @Override
    public int hashCode() {
      return Objects.hash(privatePort, publicPort, type, ip);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("privatePort", privatePort)
          .add("publicPort", publicPort)
          .add("type", type)
          .add("ip", ip)
          .toString();
    }
  }
}
