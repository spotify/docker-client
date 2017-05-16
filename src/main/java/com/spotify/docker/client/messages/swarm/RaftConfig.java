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

  @Nullable
  @JsonProperty("SnapshotInterval")
  public abstract Integer snapshotInterval();

  @Nullable
  @JsonProperty("KeepOldSnapshots")
  public abstract Integer keepOldSnapshots();

  @Nullable
  @JsonProperty("LogEntriesForSlowFollowers")
  public abstract Integer logEntriesForSlowFollowers();

  @Nullable
  @JsonProperty("ElectionTick")
  public abstract Integer electionTick();

  @Nullable
  @JsonProperty("HeartbeatTick")
  public abstract Integer heartbeatTick();

  @JsonCreator
  static RaftConfig create(
      @JsonProperty("SnapshotInterval") final Integer snapshotInterval,
      @JsonProperty("KeepOldSnapshots") final Integer keepOldSnapshots,
      @JsonProperty("LogEntriesForSlowFollowers") final Integer logEntriesForSlowFollowers,
      @JsonProperty("ElectionTick") final Integer electionTick,
      @JsonProperty("HeartbeatTick") final Integer heartbeatTick) {
    return builder()
        .snapshotInterval(snapshotInterval)
        .keepOldSnapshots(keepOldSnapshots)
        .logEntriesForSlowFollowers(logEntriesForSlowFollowers)
        .electionTick(electionTick)
        .heartbeatTick(heartbeatTick)
        .build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder snapshotInterval(Integer snapshotInterval);

    public abstract Builder keepOldSnapshots(Integer keepOldSnapshots);

    public abstract Builder logEntriesForSlowFollowers(Integer logEntriesForSlowFollowers);

    public abstract Builder electionTick(Integer electionTick);

    public abstract Builder heartbeatTick(Integer heartbeatTick);

    public abstract RaftConfig build();
  }

  public static RaftConfig.Builder builder() {
    return new AutoValue_RaftConfig.Builder();
  }
}
