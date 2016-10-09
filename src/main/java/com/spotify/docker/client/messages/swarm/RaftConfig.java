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
package com.spotify.docker.client.messages.swarm;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class RaftConfig {

  @JsonProperty("SnapshotInterval")
  private Integer snapshotInterval;

  @JsonProperty("KeepOldSnapshots")
  private Integer keepOldSnapshots;

  @JsonProperty("LogEntriesForSlowFollowers")
  private Integer logEntriesForSlowFollowers;

  @JsonProperty("ElectionTick")
  private Integer electionTick;

  @JsonProperty("HeartbeatTick")
  private Integer heartbeatTick;

  public Integer snapshotInterval() {
    return snapshotInterval;
  }

  public Integer keepOldSnapshots() {
    return keepOldSnapshots;
  }

  public Integer logEntriesForSlowFollowers() {
    return logEntriesForSlowFollowers;
  }

  public Integer electionTick() {
    return electionTick;
  }

  public Integer heartbeatTick() {
    return heartbeatTick;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final RaftConfig that = (RaftConfig) o;

    return Objects.equals(this.snapshotInterval, that.snapshotInterval)
           && Objects.equals(this.keepOldSnapshots, that.keepOldSnapshots)
           && Objects.equals(this.logEntriesForSlowFollowers, that.logEntriesForSlowFollowers)
           && Objects.equals(this.electionTick, that.electionTick)
           && Objects.equals(this.heartbeatTick, that.heartbeatTick);
  }

  @Override
  public int hashCode() {
    return Objects.hash(snapshotInterval, keepOldSnapshots, logEntriesForSlowFollowers,
                        electionTick, heartbeatTick);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("snapshotInterval", snapshotInterval)
        .add("keepOldSnapshots", keepOldSnapshots)
        .add("logEntriesForSlowFollowers", logEntriesForSlowFollowers)
        .add("electionTick", electionTick).add("heartbeatTick", heartbeatTick).toString();
  }
}
