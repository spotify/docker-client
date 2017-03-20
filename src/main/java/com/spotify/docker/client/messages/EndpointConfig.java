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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class EndpointConfig {

  @Nullable
  @JsonProperty("IPAMConfig")
  public abstract EndpointIpamConfig ipamConfig();

  @Nullable
  @JsonProperty("Links")
  public abstract ImmutableList<String> links();

  @Nullable
  @JsonProperty("Aliases")
  public abstract ImmutableList<String> aliases();

  @Nullable
  @JsonProperty("Gateway")
  public abstract String gateway();

  @Nullable
  @JsonProperty("IPAddress")
  public abstract String ipAddress();

  @Nullable
  @JsonProperty("IPPrefixLen")
  public abstract Integer ipPrefixLen();

  @Nullable
  @JsonProperty("IPv6Gateway")
  public abstract String ipv6Gateway();

  @Nullable
  @JsonProperty("GlobalIPv6Address")
  public abstract String globalIPv6Address();

  @Nullable
  @JsonProperty("GlobalIPv6PrefixLen")
  public abstract Integer globalIPv6PrefixLen();

  @Nullable
  @JsonProperty("MacAddress")
  public abstract String macAddress();

  public static Builder builder() {
    return new AutoValue_EndpointConfig.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder ipamConfig(EndpointIpamConfig ipamConfig);

    public abstract Builder links(List<String> links);

    public abstract Builder aliases(ImmutableList<String> aliases);

    public abstract Builder gateway(String gateway);

    public abstract Builder ipAddress(String ipAddress);

    public abstract Builder ipPrefixLen(Integer ipPrefixLen);

    public abstract Builder ipv6Gateway(String ipv6Gateway);

    public abstract Builder globalIPv6Address(String globalIPv6Address);

    public abstract Builder globalIPv6PrefixLen(Integer globalIPv6PrefixLen);

    public abstract Builder macAddress(String macAddress);

    public abstract EndpointConfig build();
  }

  @AutoValue
  public abstract static class EndpointIpamConfig {

    @Nullable
    @JsonProperty("IPv4Address")
    public abstract String ipv4Address();

    @Nullable
    @JsonProperty("IPv6Address")
    public abstract String ipv6Address();

    @Nullable
    @JsonProperty("LinkLocalIPs")
    public abstract ImmutableList<String> linkLocalIPs();

    public static Builder builder() {
      return new AutoValue_EndpointConfig_EndpointIpamConfig.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

      public abstract Builder ipv4Address(String ipv4Address);

      public abstract Builder ipv6Address(String ipv6Address);

      public abstract Builder linkLocalIPs(List<String> linkLocalIPs);

      public abstract EndpointIpamConfig build();
    }
  }
}
