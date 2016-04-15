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

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ContainerStats {

  @JsonProperty("read") private String read;
  @JsonProperty("network") private NetworkStats network;
  @JsonProperty("networks") private ImmutableMap<String, NetworkStats> networks;
  @JsonProperty("memory_stats") private MemoryStats memoryStats;
  @JsonProperty("blkio_stats") private BlockIoStats blockIoStats;
  @JsonProperty("cpu_stats") private CpuStats cpuStats;
  @JsonProperty("precpu_stats") private CpuStats precpuStats;

  public String read() {
    return read;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (cpuStats == null ? 0 : cpuStats.hashCode());
    result = prime * result + (memoryStats == null ? 0 : memoryStats.hashCode());
    result = prime * result + (network == null ? 0 : network.hashCode());
    result = prime * result + (networks == null ? 0 : networks.hashCode());
    result = prime * result + (blockIoStats == null ? 0 : blockIoStats.hashCode());
    result = prime * result + (precpuStats == null ? 0 : precpuStats.hashCode());
    result = prime * result + (read == null ? 0 : read.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ContainerStats other = (ContainerStats) obj;
    if (cpuStats == null) {
      if (other.cpuStats != null) {
        return false;
      }
    } else if (!cpuStats.equals(other.cpuStats)) {
      return false;
    }
    if (memoryStats == null) {
      if (other.memoryStats != null) {
        return false;
      }
    } else if (!memoryStats.equals(other.memoryStats)) {
      return false;
    }
    if (network == null) {
      if (other.network != null) {
        return false;
      }
    } else if (!network.equals(other.network)) {
      return false;
    }
    if (networks == null) {
      if (other.networks != null) {
        return false;
      }
    } else if (!networks.equals(other.networks)) {
      return false;
    }
    if (blockIoStats == null) {
      if (other.blockIoStats != null) {
        return false;
      }
    } else if (!blockIoStats.equals(other.blockIoStats)) {
      return false;
    }
    if (precpuStats == null) {
      if (other.precpuStats != null) {
        return false;
      }
    } else if (!precpuStats.equals(other.precpuStats)) {
      return false;
    }
    if (read == null) {
      if (other.read != null) {
        return false;
      }
    } else if (!read.equals(other.read)) {
      return false;
    }
    return true;
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
