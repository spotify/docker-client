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

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Network {

  @JsonProperty("Name")
  public abstract String name();

  @JsonProperty("Id")
  public abstract String id();

  @JsonProperty("Scope")
  public abstract String scope();

  @JsonProperty("Driver")
  public abstract String driver();

  @JsonProperty("IPAM")
  public abstract Ipam ipam();

  @Nullable
  @JsonProperty("Containers")
  public abstract ImmutableMap<String, Container> containers();

  @JsonProperty("Options")
  public abstract ImmutableMap<String, String> options();
  
  @Nullable
  @JsonProperty("Internal")
  public abstract Boolean internal();
  
  @Nullable
  @JsonProperty("EnableIPv6")
  public abstract Boolean enableIPv6();

  @JsonCreator
  static Network create(
      @JsonProperty("Name") final String name,
      @JsonProperty("Id") final String id,
      @JsonProperty("Scope") final String scope,
      @JsonProperty("Driver") final String driver,
      @JsonProperty("IPAM") final Ipam ipam,
      @JsonProperty("Containers") final Map<String, Container> containers,
      @JsonProperty("Options") final Map<String, String> options,
      @JsonProperty("Internal") final Boolean internal,
      @JsonProperty("EnableIPv6") final Boolean enableIPv6) {
    final ImmutableMap<String, Container> containersCopy = containers == null
                                                           ? null : ImmutableMap.copyOf(containers);
    final ImmutableMap<String, String> optionsCopy = options == null
                                                     ? null : ImmutableMap.copyOf(options);
    return new AutoValue_Network(name, id, scope, driver, ipam, containersCopy, optionsCopy,
            internal, enableIPv6);
  }

  @AutoValue
  @JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
  public abstract static class Container {

    @JsonProperty("EndpointID")
    public abstract String endpointId();

    @JsonProperty("MacAddress")
    public abstract String macAddress();

    @JsonProperty("IPv4Address")
    public abstract String ipv4Address();

    @JsonProperty("IPv6Address")
    public abstract String ipv6Address();

    @JsonCreator
    static Container create(
        @JsonProperty("EndpointID") final String endpointId,
        @JsonProperty("MacAddress") final String macAddress,
        @JsonProperty("IPv4Address") final String ipv4Address,
        @JsonProperty("IPv6Address") final String ipv6Address) {
      return new AutoValue_Network_Container(endpointId, macAddress, ipv4Address, ipv6Address);
    }
  }
}
