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
import com.google.common.collect.ImmutableList;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class CpuStats {

  @JsonProperty("cpu_usage")
  private CpuUsage cpuUsage;
  @JsonProperty("system_cpu_usage")
  private Long systemCpuUsage;
  @JsonProperty("throttling_data")
  private ThrottlingData throttlingData;

  public CpuUsage cpuUsage() {
    return cpuUsage;
  }

  public Long systemCpuUsage() {
    return systemCpuUsage;
  }

  public ThrottlingData throttlingData() {
    return throttlingData;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final CpuStats that = (CpuStats) o;

    return Objects.equals(this.cpuUsage, that.cpuUsage) &&
           Objects.equals(this.systemCpuUsage, that.systemCpuUsage) &&
           Objects.equals(this.throttlingData, that.throttlingData);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cpuUsage, systemCpuUsage, throttlingData);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("cpuUsage", cpuUsage)
        .add("systemCpuUsage", systemCpuUsage)
        .add("throttlingData", throttlingData)
        .toString();
  }

  public static class CpuUsage {

    @JsonProperty("total_usage")
    private Long totalUsage;
    @JsonProperty("percpu_usage")
    private ImmutableList<Long> percpuUsage;
    @JsonProperty("usage_in_kernelmode")
    private Long usageInKernelmode;
    @JsonProperty("usage_in_usermode")
    private Long usageInUsermode;

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
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final CpuUsage that = (CpuUsage) o;

      return Objects.equals(this.totalUsage, that.totalUsage) &&
             Objects.equals(this.percpuUsage, that.percpuUsage) &&
             Objects.equals(this.usageInKernelmode, that.usageInKernelmode) &&
             Objects.equals(this.usageInUsermode, that.usageInUsermode);
    }

    @Override
    public int hashCode() {
      return Objects.hash(totalUsage, percpuUsage, usageInKernelmode, usageInUsermode);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("percpuUsage", percpuUsage)
          .add("totalUsage", totalUsage)
          .add("usageInKernelmode", usageInKernelmode)
          .add("usageInUsermode", usageInUsermode)
          .toString();
    }
  }

  public static class ThrottlingData {

    @JsonProperty("periods")
    private Long periods;
    @JsonProperty("throttled_periods")
    private Long throttledPeriods;
    @JsonProperty("throttled_time")
    private Long throttledTime;

    public Long throttledTime() {
      return throttledTime;
    }

    public Long throttledPeriods() {
      return throttledPeriods;
    }

    public Long periods() {
      return periods;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      final ThrottlingData that = (ThrottlingData) o;

      return Objects.equals(this.periods, that.periods) &&
             Objects.equals(this.throttledPeriods, that.throttledPeriods) &&
             Objects.equals(this.throttledTime, that.throttledTime);
    }

    @Override
    public int hashCode() {
      return Objects.hash(periods, throttledPeriods, throttledTime);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("periods", periods)
          .add("throttledPeriods", throttledPeriods)
          .add("throttledTime", throttledTime)
          .toString();
    }
  }
}
