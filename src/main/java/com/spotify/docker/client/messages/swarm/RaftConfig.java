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

package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class RaftConfig {

  @JsonProperty("SnapshotInterval")
  public abstract Integer snapshotInterval();

  @Nullable
  @JsonProperty("KeepOldSnapshots")
  public abstract Integer keepOldSnapshots();

  @JsonProperty("LogEntriesForSlowFollowers")
  public abstract Integer logEntriesForSlowFollowers();

  @JsonProperty("ElectionTick")
  public abstract Integer electionTick();

  @JsonProperty("HeartbeatTick")
  public abstract Integer heartbeatTick();


  @JsonCreator
  static RaftConfig create(
      @JsonProperty("SnapshotInterval") final Integer snapshotInterval,
      @JsonProperty("KeepOldSnapshots") final Integer keepOldSnapshots,
      @JsonProperty("LogEntriesForSlowFollowers") final Integer logEntriesForSlowFollowers,
      @JsonProperty("ElectionTick") final Integer electionTick,
      @JsonProperty("HeartbeatTick") final Integer heartbeatTick) {
    return new AutoValue_RaftConfig(snapshotInterval, keepOldSnapshots, logEntriesForSlowFollowers,
        electionTick, heartbeatTick);
  }
}
