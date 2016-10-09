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
import com.google.common.collect.ImmutableList;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class BlockIoStats {

  @JsonProperty("io_service_bytes_recursive")
  private ImmutableList<Object> ioServiceBytesRecursive;
  @JsonProperty("io_serviced_recursive")
  private ImmutableList<Object> ioServicedRecursive;
  @JsonProperty("io_queue_recursive")
  private ImmutableList<Object> ioQueueRecursive;
  @JsonProperty("io_service_time_recursive")
  private ImmutableList<Object> ioServiceTimeRecursive;
  @JsonProperty("io_wait_time_recursive")
  private ImmutableList<Object> ioWaitTimeRecursive;
  @JsonProperty("io_merged_recursive")
  private ImmutableList<Object> ioMergedRecursive;
  @JsonProperty("io_time_recursive")
  private ImmutableList<Object> ioTimeRecursive;
  @JsonProperty("sectors_recursive")
  private ImmutableList<Object> sectorsRecursive;

  public List<Object> ioServiceBytesRecursive() {
    return ioServiceBytesRecursive;
  }

  public List<Object> ioServicedRecursive() {
    return ioServicedRecursive;
  }

  public List<Object> ioQueueRecursive() {
    return ioQueueRecursive;
  }

  public List<Object> ioServiceTimeRecursive() {
    return ioServiceTimeRecursive;
  }

  public List<Object> ioWaitTimeRecursive() {
    return ioWaitTimeRecursive;
  }

  public List<Object> ioMergedRecursive() {
    return ioMergedRecursive;
  }

  public List<Object> ioTimeRecursive() {
    return ioTimeRecursive;
  }

  public List<Object> sectorsRecursive() {
    return sectorsRecursive;
  }

  @Override
  public int hashCode() {
    return Objects.hash(ioServiceBytesRecursive,
                        ioServicedRecursive,
                        ioQueueRecursive,
                        ioServiceTimeRecursive,
                        ioWaitTimeRecursive,
                        ioMergedRecursive,
                        ioTimeRecursive,
                        sectorsRecursive);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final BlockIoStats that = (BlockIoStats) o;

    return Objects.equals(this.ioServiceBytesRecursive, that.ioServiceBytesRecursive) &&
           Objects.equals(this.ioServicedRecursive, that.ioServicedRecursive) &&
           Objects.equals(this.ioQueueRecursive, that.ioQueueRecursive) &&
           Objects.equals(this.ioServiceTimeRecursive, that.ioServiceTimeRecursive) &&
           Objects.equals(this.ioWaitTimeRecursive, that.ioWaitTimeRecursive) &&
           Objects.equals(this.ioMergedRecursive, that.ioMergedRecursive) &&
           Objects.equals(this.ioTimeRecursive, that.ioTimeRecursive) &&
           Objects.equals(this.sectorsRecursive, that.sectorsRecursive);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("ioServiceBytesRecursive", ioServiceBytesRecursive)
        .add("ioServicedRecursive", ioServicedRecursive)
        .add("ioQueueRecursive", ioQueueRecursive)
        .add("ioServiceTimeRecursive", ioServiceTimeRecursive)
        .add("ioWaitTimeRecursive", ioWaitTimeRecursive)
        .add("ioMergedRecursive", ioMergedRecursive)
        .add("ioTimeRecursive", ioTimeRecursive)
        .add("sectorsRecursive", sectorsRecursive)
        .toString();
  }
}
