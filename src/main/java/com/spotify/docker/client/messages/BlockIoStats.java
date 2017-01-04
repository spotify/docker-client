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
import static com.google.common.collect.ImmutableList.copyOf;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class BlockIoStats {

  @JsonProperty("io_service_bytes_recursive")
  public abstract ImmutableList<Object> ioServiceBytesRecursive();

  @JsonProperty("io_serviced_recursive")
  public abstract ImmutableList<Object> ioServicedRecursive();

  @JsonProperty("io_queue_recursive")
  public abstract ImmutableList<Object> ioQueueRecursive();

  @JsonProperty("io_service_time_recursive")
  public abstract ImmutableList<Object> ioServiceTimeRecursive();

  @JsonProperty("io_wait_time_recursive")
  public abstract ImmutableList<Object> ioWaitTimeRecursive();

  @JsonProperty("io_merged_recursive")
  public abstract ImmutableList<Object> ioMergedRecursive();

  @JsonProperty("io_time_recursive")
  public abstract ImmutableList<Object> ioTimeRecursive();

  @JsonProperty("sectors_recursive")
  public abstract ImmutableList<Object> sectorsRecursive();

  @JsonCreator
  static BlockIoStats create(
      @JsonProperty("io_service_bytes_recursive") final List<Object> ioServiceBytesRecursive,
      @JsonProperty("io_serviced_recursive") final List<Object> ioServicedRecursive,
      @JsonProperty("io_queue_recursive") final List<Object> ioQueueRecursive,
      @JsonProperty("io_service_time_recursive") final List<Object> ioServiceTimeRecursive,
      @JsonProperty("io_wait_time_recursive") final List<Object> ioWaitTimeRecursive,
      @JsonProperty("io_merged_recursive") final List<Object> ioMergedRecursive,
      @JsonProperty("io_time_recursive") final List<Object> ioTimeRecursive,
      @JsonProperty("sectors_recursive") final List<Object> sectorsRecursive) {
    return new AutoValue_BlockIoStats(
        copyOf(ioServiceBytesRecursive), copyOf(ioServicedRecursive), copyOf(ioQueueRecursive),
        copyOf(ioServiceTimeRecursive), copyOf(ioWaitTimeRecursive), copyOf(ioMergedRecursive),
        copyOf(ioTimeRecursive), copyOf(sectorsRecursive));
  }
}
