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
import java.util.List;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Endpoint {

  @JsonProperty("Spec")
  public abstract EndpointSpec spec();

  @Nullable
  @JsonProperty("ExposedPorts")
  public abstract ImmutableList<PortConfig> exposedPorts();

  @Nullable
  @JsonProperty("Ports")
  public abstract ImmutableList<PortConfig> ports();

  @Nullable
  @JsonProperty("VirtualIPs")
  public abstract ImmutableList<EndpointVirtualIp> virtualIps();

  @JsonCreator
  static Endpoint create(
      @JsonProperty("Spec") final EndpointSpec spec,
      @JsonProperty("ExposedPorts") final List<PortConfig> exposedPorts,
      @JsonProperty("Ports") final List<PortConfig> ports,
      @JsonProperty("VirtualIPs") final List<EndpointVirtualIp> virtualIps) {
    final ImmutableList<PortConfig> exposedPortsT = exposedPorts == null
                                                    ? null : ImmutableList.copyOf(exposedPorts);
    final ImmutableList<PortConfig> portsT = ports == null ? null : ImmutableList.copyOf(ports);
    final ImmutableList<EndpointVirtualIp> virtualIpsT = virtualIps == null
                                                         ? null : ImmutableList.copyOf(virtualIps);
    return new AutoValue_Endpoint(spec, exposedPortsT, portsT, virtualIpsT);
  }
}
