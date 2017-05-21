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
import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class SwarmInfo {

  @Nullable
  @JsonProperty("Cluster")
  public abstract SwarmCluster cluster();
  
  @JsonProperty("ControlAvailable")
  public abstract boolean controlAvailable();

  @JsonProperty("Error")
  public abstract String error();

  @JsonProperty("LocalNodeState")
  public abstract String localNodeState();

  @JsonProperty("NodeAddr")
  public abstract String nodeAddr();

  @JsonProperty("NodeID")
  public abstract String nodeId();

  @JsonProperty("Nodes")
  public abstract int nodes();
  
  @Nullable
  @JsonProperty("RemoteManagers")
  public abstract ImmutableList<RemoteManager> remoteManagers();

  @JsonCreator
  static SwarmInfo create(
      @JsonProperty("Cluster") final SwarmCluster cluster,
      @JsonProperty("ControlAvailable") final boolean controlAvailable,
      @JsonProperty("Error") final String error,
      @JsonProperty("LocalNodeState") final String localNodeState,
      @JsonProperty("NodeAddr") final String nodeAddr,
      @JsonProperty("NodeID") final String nodeId,
      @JsonProperty("Nodes") final int nodes,
      @JsonProperty("RemoteManagers") final ImmutableList<RemoteManager> remoteManagers) {
    return new AutoValue_SwarmInfo(cluster, controlAvailable, error, localNodeState, nodeAddr, nodeId, nodes, remoteManagers);
  }
}
