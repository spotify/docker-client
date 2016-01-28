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

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class Container {

  @JsonProperty("Id") private String id;
  @JsonProperty("Names") private ImmutableList<String> names;
  @JsonProperty("Image") private String image;
  @JsonProperty("ImageID") private String imageId;
  @JsonProperty("Command") private String command;
  @JsonProperty("Created") private Long created;
  @JsonProperty("Status") private String status;
  @JsonProperty("Ports") private ImmutableList<PortMapping> ports;
  @JsonProperty("Labels") private ImmutableMap<String, String> labels;
  @JsonProperty("SizeRw") private Long sizeRw;
  @JsonProperty("SizeRootFs") private Long sizeRootFs;  

  /**
   * This method returns port information the way that <code>docker ps</code> does:
   * <code>0.0.0.0:5432-&gt;5432/tcp</code> or <code>6379/tcp</code>
   *
   * It should not be used to extract detailed information of ports. To do so,
   * please refer to {@link com.spotify.docker.client.messages.PortBinding}
   *
   * @see com.spotify.docker.client.messages.PortBinding
   *
   * @return port information as docker ps does.
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

  public String command() {
    return command;
  }

  public Long created() {
    return created;
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
  
  public String imageId() {
    return imageId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Container container = (Container) o;

    if (command != null ? !command.equals(container.command) : container.command != null) {
      return false;
    }
    if (created != null ? !created.equals(container.created) : container.created != null) {
      return false;
    }
    if (id != null ? !id.equals(container.id) : container.id != null) {
      return false;
    }
    if (image != null ? !image.equals(container.image) : container.image != null) {
      return false;
    }
    if (names != null ? !names.equals(container.names) : container.names != null) {
      return false;
    }
    if (ports != null ? !ports.equals(container.ports) : container.ports != null) {
      return false;
    }
    if (labels != null ? !labels.equals(container.labels) : container.labels != null) {
      return false;
    }
    if (sizeRootFs != null ? !sizeRootFs.equals(container.sizeRootFs)
                           : container.sizeRootFs != null) {
      return false;
    }
    if (sizeRw != null ? !sizeRw.equals(container.sizeRw) : container.sizeRw != null) {
      return false;
    }
    if (status != null ? !status.equals(container.status) : container.status != null) {
      return false;
    }
    if (imageId != null ? !imageId.equals(container.imageId) : container.imageId != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (names != null ? names.hashCode() : 0);
    result = 31 * result + (image != null ? image.hashCode() : 0);
    result = 31 * result + (command != null ? command.hashCode() : 0);
    result = 31 * result + (created != null ? created.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (ports != null ? ports.hashCode() : 0);
    result = 31 * result + (labels != null ? labels.hashCode() : 0);
    result = 31 * result + (sizeRw != null ? sizeRw.hashCode() : 0);
    result = 31 * result + (sizeRootFs != null ? sizeRootFs.hashCode() : 0);
    result = 31 * result + (imageId != null ? imageId.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("image", image)
        .add("command", command)
        .add("created", created)
        .add("status", status)
        .add("ports", ports)
        .add("labels", labels)
        .add("sizeRw", sizeRw)
        .add("sizeRootFs", sizeRootFs)
        .add("imageId", imageId)
        .toString();
  }

  public static class PortMapping {

    @JsonProperty("PrivatePort") private int privatePort;
    @JsonProperty("PublicPort") private int publicPort;
    @JsonProperty("Type") private String type;
    @JsonProperty("IP") private String ip;

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

      if (privatePort != that.privatePort) {
        return false;
      }
      if (publicPort != that.publicPort) {
        return false;
      }
      if (ip != null ? !ip.equals(that.ip) : that.ip != null) {
        return false;
      }
      if (type != null ? !type.equals(that.type) : that.type != null) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = privatePort;
      result = 31 * result + publicPort;
      result = 31 * result + (type != null ? type.hashCode() : 0);
      result = 31 * result + (ip != null ? ip.hashCode() : 0);
      return result;
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
