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

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class NetworkStats {
  @JsonProperty("rx_bytes") private Long rxBytes;
  @JsonProperty("rx_packets") private Long rxPackets;
  @JsonProperty("rx_dropped") private Long rxDropped;
  @JsonProperty("rx_errors") private Long rxErrors;
  @JsonProperty("tx_bytes") private Long txBytes;
  @JsonProperty("tx_packets") private Long txPackets;
  @JsonProperty("tx_dropped") private Long txDropped;
  @JsonProperty("tx_errors") private Long txErrors;

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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (rxBytes == null ? 0 : rxBytes.hashCode());
    result = prime * result + (rxDropped == null ? 0 : rxDropped.hashCode());
    result = prime * result + (rxErrors == null ? 0 : rxErrors.hashCode());
    result = prime * result + (rxPackets == null ? 0 : rxPackets.hashCode());
    result = prime * result + (txBytes == null ? 0 : txBytes.hashCode());
    result = prime * result + (txDropped == null ? 0 : txDropped.hashCode());
    result = prime * result + (txErrors == null ? 0 : txErrors.hashCode());
    result = prime * result + (txPackets == null ? 0 : txPackets.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NetworkStats other = (NetworkStats) obj;
    if (rxBytes == null) {
      if (other.rxBytes != null) {
        return false;
      }
    } else if (!rxBytes.equals(other.rxBytes)) {
      return false;
    }
    if (rxDropped == null) {
      if (other.rxDropped != null) {
        return false;
      }
    } else if (!rxDropped.equals(other.rxDropped)) {
      return false;
    }
    if (rxErrors == null) {
      if (other.rxErrors != null) {
        return false;
      }
    } else if (!rxErrors.equals(other.rxErrors)) {
      return false;
    }
    if (rxPackets == null) {
      if (other.rxPackets != null) {
        return false;
      }
    } else if (!rxPackets.equals(other.rxPackets)) {
      return false;
    }
    if (txBytes == null) {
      if (other.txBytes != null) {
        return false;
      }
    } else if (!txBytes.equals(other.txBytes)) {
      return false;
    }
    if (txDropped == null) {
      if (other.txDropped != null) {
        return false;
      }
    } else if (!txDropped.equals(other.txDropped)) {
      return false;
    }
    if (txErrors == null) {
      if (other.txErrors != null) {
        return false;
      }
    } else if (!txErrors.equals(other.txErrors)) {
      return false;
    }
    if (txPackets == null) {
      if (other.txPackets != null) {
        return false;
      }
    } else if (!txPackets.equals(other.txPackets)) {
      return false;
    }
    return true;
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
