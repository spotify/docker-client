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

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Container {

  @JsonProperty("Id")
  public abstract String id();

  @Nullable
  @JsonProperty("Names")
  public abstract ImmutableList<String> names();

  @JsonProperty("Image")
  public abstract String image();

  @Nullable
  @JsonProperty("ImageID")
  public abstract String imageId();

  @JsonProperty("Command")
  public abstract String command();

  @JsonProperty("Created")
  public abstract Long created();

  @Nullable
  @JsonProperty("State")
  public abstract String state();

  @JsonProperty("Status")
  public abstract String status();

  @Nullable
  @JsonProperty("Ports")
  public abstract ImmutableList<PortMapping> ports();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @Nullable
  @JsonProperty("SizeRw")
  public abstract Long sizeRw();

  @Nullable
  @JsonProperty("SizeRootFs")
  public abstract Long sizeRootFs();

  @Nullable
  @JsonProperty("NetworkSettings")
  public abstract NetworkSettings networkSettings();

  @Nullable
  @JsonProperty("Mounts")
  public abstract ImmutableList<ContainerMount> mounts();

  /**
   * Returns port information the way that <code>docker ps</code> does.
   * <code>0.0.0.0:5432-&gt;5432/tcp</code> or <code>6379/tcp</code>.
   *
   * <p>It should not be used to extract detailed information of ports. To do so, please refer to
   * {@link com.spotify.docker.client.messages.PortBinding}.
   *
   * @return port information as docker ps does.
   * @see com.spotify.docker.client.messages.PortBinding
   */
  public String portsAsString() {
    final StringBuilder sb = new StringBuilder();
    if (ports() != null) {
      for (final PortMapping port : ports()) {
        if (sb.length() > 0) {
          sb.append(", ");
        }
        if (port.ip() != null) {
          sb.append(port.ip()).append(":");
        }
        if (port.publicPort() > 0) {
          sb.append(port.privatePort()).append("->").append(port.publicPort());
        } else {
          sb.append(port.privatePort());
        }
        sb.append("/").append(port.type());
      }
    }

    return sb.toString();
  }

  @JsonCreator
  static Container create(
      @JsonProperty("Id") final String id,
      @JsonProperty("Names") final List<String> names,
      @JsonProperty("Image") final String image,
      @JsonProperty("ImageID") final String imageId,
      @JsonProperty("Command") final String command,
      @JsonProperty("Created") final Long created,
      @JsonProperty("State") final String state,
      @JsonProperty("Status") final String status,
      @JsonProperty("Ports") final List<PortMapping> ports,
      @JsonProperty("Labels") final Map<String, String> labels,
      @JsonProperty("SizeRw") final Long sizeRw,
      @JsonProperty("SizeRootFs") final Long sizeRootFs,
      @JsonProperty("NetworkSettings") final NetworkSettings networkSettings,
      @JsonProperty("Mounts") final List<ContainerMount> mounts) {
    final ImmutableMap<String, String> labelsT = labels == null
                                                 ? null : ImmutableMap.copyOf(labels);
    final ImmutableList<ContainerMount> mountsT = mounts == null
                                                  ? null : ImmutableList.copyOf(mounts);
    final ImmutableList<String> namesT = names == null
            ? null : ImmutableList.copyOf(names);
    final ImmutableList<PortMapping> portsT = ports == null
            ? null : ImmutableList.copyOf(ports);

    return new AutoValue_Container(id, namesT, image, imageId, command,
        created, state, status, portsT, labelsT, sizeRw,
        sizeRootFs, networkSettings, mountsT);
  }

  @AutoValue
  public abstract static class PortMapping {

    @JsonProperty("PrivatePort")
    public abstract Integer privatePort();

    @JsonProperty("PublicPort")
    public abstract Integer publicPort();

    @JsonProperty("Type")
    public abstract String type();

    @Nullable
    @JsonProperty("IP")
    public abstract String ip();

    @JsonCreator
    static PortMapping create(
        @JsonProperty("PrivatePort") final int privatePort,
        @JsonProperty("PublicPort") final int publicPort,
        @JsonProperty("Type") final String type,
        @JsonProperty("IP") final String ip) {
      return new AutoValue_Container_PortMapping(privatePort, publicPort, type, ip);
    }
  }
}
