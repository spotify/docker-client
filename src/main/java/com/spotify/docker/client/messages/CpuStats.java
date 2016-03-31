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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class CpuStats {

  @JsonProperty("cpu_usage") private CpuUsage cpuUsage;
  @JsonProperty("system_cpu_usage") Long systemCpuUsage;

  public CpuUsage cpuUsage() {
    return cpuUsage;
  }

  public Long systemCpuUsage() {
    return systemCpuUsage;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (cpuUsage == null ? 0 : cpuUsage.hashCode());
    result = prime * result + (systemCpuUsage == null ? 0 : systemCpuUsage.hashCode());
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
    final CpuStats other = (CpuStats) obj;
    if (cpuUsage == null) {
      if (other.cpuUsage != null) {
        return false;
      }
    } else if (!cpuUsage.equals(other.cpuUsage)) {
      return false;
    }
    if (systemCpuUsage == null) {
      if (other.systemCpuUsage != null) {
        return false;
      }
    } else if (!systemCpuUsage.equals(other.systemCpuUsage)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("cpuUsage", cpuUsage)
        .add("systemCpuUsage", systemCpuUsage)
        .toString();
  }
}
