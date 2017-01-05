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
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;

import java.util.List;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class EndpointSpec {

  public enum Mode {
    RESOLUTION_MODE_VIP("vip"),
    RESOLUTION_MODE_DNSRR("dnsrr");

    private final String value;

    Mode(final String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }
  }

  @Nullable
  @JsonProperty("Mode")
  public abstract Mode mode();

  @JsonProperty("Ports")
  public abstract ImmutableList<PortConfig> ports();

  abstract Builder toBuilder();

  public EndpointSpec withVipMode() {
    return toBuilder().mode(Mode.RESOLUTION_MODE_VIP).build();
  }

  public EndpointSpec withDnsrrMode() {
    return toBuilder().mode(Mode.RESOLUTION_MODE_DNSRR).build();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder mode(Mode mode);

    abstract ImmutableList.Builder<PortConfig> portsBuilder();

    public Builder addPort(final PortConfig portConfig) {
      portsBuilder().add(portConfig);
      return this;
    }

    public abstract Builder ports(PortConfig... ports);

    public abstract Builder ports(List<PortConfig> ports);

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #ports(PortConfig...)}.
     */
    @Deprecated
    public Builder withPorts(final PortConfig... ports) {
      if (ports != null && ports.length > 0) {
        ports(ImmutableList.copyOf(ports));
      }
      return this;
    }

    /**
     * @deprecated  As of release 7.0.0, replaced by {@link #ports(List)}.
     */
    @Deprecated
    public Builder withPorts(final List<PortConfig> ports) {
      if (ports != null && !ports.isEmpty()) {
        ports(ports);
      }
      return this;
    }

    public abstract EndpointSpec build();
  }

  public static EndpointSpec.Builder builder() {
    return new AutoValue_EndpointSpec.Builder();
  }

  @JsonCreator
  static EndpointSpec create(
      @JsonProperty("Mode") final Mode mode,
      @JsonProperty("Ports") final List<PortConfig> ports) {
    final Builder builder = builder()
        .mode(mode);

    if (ports != null) {
      builder.ports(ports);
    }

    return builder.build();
  }
}
