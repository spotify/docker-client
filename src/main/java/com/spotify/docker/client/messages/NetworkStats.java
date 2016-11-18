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


@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class NetworkStats {

  @JsonProperty("rx_bytes")
  public abstract Long rxBytes();

  @JsonProperty("rx_packets")
  public abstract Long rxPackets();

  @JsonProperty("rx_dropped")
  public abstract Long rxDropped();

  @JsonProperty("rx_errors")
  public abstract Long rxErrors();

  @JsonProperty("tx_bytes")
  public abstract Long txBytes();

  @JsonProperty("tx_packets")
  public abstract Long txPackets();

  @JsonProperty("tx_dropped")
  public abstract Long txDropped();

  @JsonProperty("tx_errors")
  public abstract Long txErrors();

  @JsonCreator
  static NetworkStats create(
      @JsonProperty("rx_bytes") final Long rxBytes,
      @JsonProperty("rx_packets") final Long rxPackets,
      @JsonProperty("rx_dropped") final Long rxDropped,
      @JsonProperty("rx_errors") final Long rxErrors,
      @JsonProperty("tx_bytes") final Long txBytes,
      @JsonProperty("tx_packets") final Long txPackets,
      @JsonProperty("tx_dropped") final Long txDropped,
      @JsonProperty("tx_errors") final Long txErrors) {
    return new AutoValue_NetworkStats(rxBytes, rxPackets, rxDropped, rxErrors, txBytes, txPackets,
        txDropped, txErrors);
  }
}
