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
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class CpuStats {

  @JsonProperty("cpu_usage")
  public abstract CpuUsage cpuUsage();

  @Nullable
  @JsonProperty("system_cpu_usage")
  public abstract Long systemCpuUsage();

  @JsonProperty("throttling_data")
  public abstract ThrottlingData throttlingData();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder cpuUsage(final CpuUsage cpuUsage);

    public abstract Builder systemCpuUsage(final Long systemCpuUsage);

    public abstract Builder throttlingData(final ThrottlingData throttlingData);

    public abstract CpuStats build();
  }

  @JsonCreator
  static CpuStats create(
      @JsonProperty("cpu_usage") final CpuUsage cpuUsage,
      @JsonProperty("system_cpu_usage") final Long systemCpuUsage,
      @JsonProperty("throttling_data") final ThrottlingData throttlingData) {
    return new AutoValue_CpuStats.Builder()
        .cpuUsage(cpuUsage)
        .systemCpuUsage(systemCpuUsage)
        .throttlingData(throttlingData)
        .build();
  }

  @AutoValue
  public abstract static class CpuUsage {

    @JsonProperty("total_usage")
    public abstract Long totalUsage();

    @Nullable
    @JsonProperty("percpu_usage")
    public abstract ImmutableList<Long> percpuUsage();

    @JsonProperty("usage_in_kernelmode")
    public abstract Long usageInKernelmode();

    @JsonProperty("usage_in_usermode")
    public abstract Long usageInUsermode();

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder totalUsage(final Long totalUsage);

      public abstract Builder percpuUsage(final List<Long> percpuUsage);

      public abstract Builder usageInKernelmode(final Long usageInKernelmode);

      public abstract Builder usageInUsermode(final Long usageInUsermode);

      public abstract CpuUsage build();
    }

    @JsonCreator
    static CpuUsage create(
        @JsonProperty("total_usage") final Long totalUsage,
        @JsonProperty("percpu_usage") final List<Long> perCpuUsage,
        @JsonProperty("usage_in_kernelmode") final Long usageInKernelmode,
        @JsonProperty("usage_in_usermode") final Long usageInUsermode) {
      return new AutoValue_CpuStats_CpuUsage.Builder()
          .totalUsage(totalUsage)
          .percpuUsage(perCpuUsage)
          .usageInKernelmode(usageInKernelmode)
          .usageInUsermode(usageInUsermode)
          .build();
    }
  }

  @AutoValue
  public abstract static class ThrottlingData {

    @JsonProperty("periods")
    public abstract Long periods();

    @JsonProperty("throttled_periods")
    public abstract Long throttledPeriods();

    @JsonProperty("throttled_time")
    public abstract Long throttledTime();

    @JsonCreator
    static ThrottlingData create(
        @JsonProperty("periods") final Long periods,
        @JsonProperty("throttled_periods") final Long throttledPeriods,
        @JsonProperty("throttled_time") final Long throttledTime) {
      return new AutoValue_CpuStats_ThrottlingData(periods, throttledPeriods, throttledTime);
    }
  }
}
