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

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class CpuUsage {
  @JsonProperty("total_usage") private Long totalUsage;
  @JsonProperty("percpu_usage") private ImmutableList<Long> percpuUsage;
  @JsonProperty("usage_in_kernelmode") private Long usageInKernelmode;
  @JsonProperty("usage_in_usermode") private Long usageInUsermode;

  public Long totalUsage() {
    return totalUsage;
  }

  public ImmutableList<Long> percpuUsage() {
    return percpuUsage;
  }

  public Long usageInKernelmode() {
    return usageInKernelmode;
  }

  public Long usageInUsermode() {
    return usageInUsermode;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (percpuUsage == null ? 0 : percpuUsage.hashCode());
    result = prime * result + (totalUsage == null ? 0 : totalUsage.hashCode());
    result = prime * result + (usageInKernelmode == null ? 0 : usageInKernelmode.hashCode());
    result = prime * result + (usageInUsermode == null ? 0 : usageInUsermode.hashCode());
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
    CpuUsage other = (CpuUsage) obj;
    if (percpuUsage == null) {
      if (other.percpuUsage != null) {
        return false;
      }
    } else if (!percpuUsage.equals(other.percpuUsage)) {
      return false;
    }
    if (totalUsage == null) {
      if (other.totalUsage != null) {
        return false;
      }
    } else if (!totalUsage.equals(other.totalUsage)) {
      return false;
    }
    if (usageInKernelmode == null) {
      if (other.usageInKernelmode != null) {
        return false;
      }
    } else if (!usageInKernelmode.equals(other.usageInKernelmode)) {
      return false;
    }
    if (usageInUsermode == null) {
      if (other.usageInUsermode != null) {
        return false;
      }
    } else if (!usageInUsermode.equals(other.usageInUsermode)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("percpuUsage", percpuUsage)
        .add("totalUsage", totalUsage)
        .add("usageInKernelmode", usageInKernelmode)
        .add("usageInUsermode", usageInUsermode)
        .toString();
  }
}
