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
public abstract class AttachedNetwork {

  @Nullable
  @JsonProperty("Aliases")
  public abstract ImmutableList<String> aliases();

  @Nullable
  @JsonProperty("NetworkID")
  public abstract String networkId();

  @JsonProperty("EndpointID")
  public abstract String endpointId();

  @JsonProperty("Gateway")
  public abstract String gateway();

  @JsonProperty("IPAddress")
  public abstract String ipAddress();

  @JsonProperty("IPPrefixLen")
  public abstract Integer ipPrefixLen();

  @JsonProperty("IPv6Gateway")
  public abstract String ipv6Gateway();

  @JsonProperty("GlobalIPv6Address")
  public abstract String globalIPv6Address();

  @JsonProperty("GlobalIPv6PrefixLen")
  public abstract Integer globalIPv6PrefixLen();

  @JsonProperty("MacAddress")
  public abstract String macAddress();

  @JsonCreator
  static AttachedNetwork create(
      @JsonProperty("Aliases") final List<String> aliases,
      @JsonProperty("NetworkID") final String networkId,
      @JsonProperty("EndpointID") final String endpointId,
      @JsonProperty("Gateway") final String gateway,
      @JsonProperty("IPAddress") final String ipAddress,
      @JsonProperty("IPPrefixLen") final Integer ipPrefixLen,
      @JsonProperty("IPv6Gateway") final String ipv6Gateway,
      @JsonProperty("GlobalIPv6Address") final String globalIPv6Address,
      @JsonProperty("GlobalIPv6PrefixLen") final Integer globalIPv6PrefixLen,
      @JsonProperty("MacAddress") final String macAddress) {
    return new AutoValue_AttachedNetwork(
            aliases == null ? null : ImmutableList.copyOf(aliases),
            networkId, endpointId, gateway, ipAddress, ipPrefixLen,
            ipv6Gateway, globalIPv6Address, globalIPv6PrefixLen, macAddress);
  }
}
