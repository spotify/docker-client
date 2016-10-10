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

package com.spotify.docker.client.messages;

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class NetworkStats {

  @JsonProperty("rx_bytes")
  private Long rxBytes;
  @JsonProperty("rx_packets")
  private Long rxPackets;
  @JsonProperty("rx_dropped")
  private Long rxDropped;
  @JsonProperty("rx_errors")
  private Long rxErrors;
  @JsonProperty("tx_bytes")
  private Long txBytes;
  @JsonProperty("tx_packets")
  private Long txPackets;
  @JsonProperty("tx_dropped")
  private Long txDropped;
  @JsonProperty("tx_errors")
  private Long txErrors;

  public Long rxBytes() {
    return rxBytes;
  }

  public Long rxPackets() {
    return rxPackets;
  }

  public Long rxDropped() {
    return rxDropped;
  }

  public Long rxErrors() {
    return rxErrors;
  }

  public Long txBytes() {
    return txBytes;
  }

  public Long txPackets() {
    return txPackets;
  }

  public Long txDropped() {
    return txDropped;
  }

  public Long txErrors() {
    return txErrors;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final NetworkStats that = (NetworkStats) o;

    return Objects.equals(this.rxBytes, that.rxBytes) &&
           Objects.equals(this.rxPackets, that.rxPackets) &&
           Objects.equals(this.rxDropped, that.rxDropped) &&
           Objects.equals(this.rxErrors, that.rxErrors) &&
           Objects.equals(this.txBytes, that.txBytes) &&
           Objects.equals(this.txPackets, that.txPackets) &&
           Objects.equals(this.txDropped, that.txDropped) &&
           Objects.equals(this.txErrors, that.txErrors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rxBytes, rxPackets, rxDropped, rxErrors,
                        txBytes, txPackets, txDropped, txErrors);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("rxBytes", rxBytes)
        .add("rxDropped", rxDropped)
        .add("rxErrors", rxErrors)
        .add("rxPackets", rxPackets)
        .add("txBytes", txBytes)
        .add("txDropped", txDropped)
        .add("txErrors", txErrors)
        .add("txPackets", txPackets)
        .toString();
  }
}
