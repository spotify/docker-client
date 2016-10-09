/*
 * Copyright (c) 2015 Spotify AB.
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
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ContainerStats {

  @JsonProperty("read")
  private Date read;
  @JsonProperty("network")
  private NetworkStats network;
  @JsonProperty("networks")
  private ImmutableMap<String, NetworkStats> networks;
  @JsonProperty("memory_stats")
  private MemoryStats memoryStats;
  @JsonProperty("blkio_stats")
  private BlockIoStats blockIoStats;
  @JsonProperty("cpu_stats")
  private CpuStats cpuStats;
  @JsonProperty("precpu_stats")
  private CpuStats precpuStats;

  public Date read() {
    return new Date(read.getTime());
  }

  public NetworkStats network() {
    return network;
  }

  public Map<String, NetworkStats> networks() {
    return networks;
  }

  public MemoryStats memoryStats() {
    return memoryStats;
  }

  public BlockIoStats blockIoStats() {
    return blockIoStats;
  }

  public CpuStats cpuStats() {
    return cpuStats;
  }

  public CpuStats precpuStats() {
    return precpuStats;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ContainerStats that = (ContainerStats) o;

    return Objects.equals(this.read, that.read) &&
           Objects.equals(this.network, that.network) &&
           Objects.equals(this.networks, that.networks) &&
           Objects.equals(this.memoryStats, that.memoryStats) &&
           Objects.equals(this.blockIoStats, that.blockIoStats) &&
           Objects.equals(this.cpuStats, that.cpuStats) &&
           Objects.equals(this.precpuStats, that.precpuStats);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cpuStats, memoryStats, network, networks,
                        blockIoStats, precpuStats, read);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("cpuStats", cpuStats)
        .add("memoryStats", memoryStats)
        .add("network", network)
        .add("networks", networks)
        .add("blkioStats", blockIoStats)
        .add("precpuStats", precpuStats)
        .add("read", read)
        .toString();
  }
}
