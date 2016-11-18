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

import com.google.common.collect.ImmutableMap;
import java.util.Date;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ContainerStats {

  @JsonProperty("read")
  public abstract Date read();

  @Nullable
  @JsonProperty("network")
  public abstract NetworkStats network();

  @Nullable
  @JsonProperty("networks")
  public abstract ImmutableMap<String, NetworkStats> networks();

  @JsonProperty("memory_stats")
  public abstract MemoryStats memoryStats();

  @JsonProperty("blkio_stats")
  public abstract BlockIoStats blockIoStats();

  @JsonProperty("cpu_stats")
  public abstract CpuStats cpuStats();

  @JsonProperty("precpu_stats")
  public abstract CpuStats precpuStats();

  @JsonCreator
  static ContainerStats create(
      @JsonProperty("read") final Date read,
      @JsonProperty("network") final NetworkStats networkStats,
      @JsonProperty("networks") final Map<String, NetworkStats> networks,
      @JsonProperty("memory_stats") final MemoryStats memoryStats,
      @JsonProperty("blkio_stats") final BlockIoStats blockIoStats,
      @JsonProperty("cpu_stats") final CpuStats cpuStats,
      @JsonProperty("precpu_stats") final CpuStats precpuStats) {
    final ImmutableMap<String, NetworkStats> networksCopy = networks == null
                                                            ? null : ImmutableMap.copyOf(networks);
    return new AutoValue_ContainerStats(read, networkStats, networksCopy,
        memoryStats, blockIoStats, cpuStats, precpuStats);
  }
}
