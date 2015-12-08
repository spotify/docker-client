/*
 * Copyright (c) 2014 Spotify AB.
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
public class PortBinding {

  public static final String RANDOM_AVAILABLE_PORT = "";

  @JsonProperty("HostIp") private String hostIp;
  @JsonProperty("HostPort") private String hostPort;

  public String hostIp() {
    return hostIp;
  }

  public void hostIp(final String hostIp) {
    this.hostIp = hostIp;
  }

  public String hostPort() {
    return hostPort;
  }

  public void hostPort(final String hostPort) {
    this.hostPort = hostPort;
  }

  public static PortBinding of(final String ip, final String port) {
    final PortBinding binding = new PortBinding();
    binding.hostIp(ip);
    binding.hostPort(port);
    return binding;
  }

  public static PortBinding of(final String ip, final int port) {
    return of(ip, String.valueOf(port));
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final PortBinding that = (PortBinding) o;

    if (hostIp != null ? !hostIp.equals(that.hostIp) : that.hostIp != null) {
      return false;
    }
    if (hostPort != null ? !hostPort.equals(that.hostPort) : that.hostPort != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = hostIp != null ? hostIp.hashCode() : 0;
    result = 31 * result + (hostPort != null ? hostPort.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hostIp", hostIp)
        .add("hostPort", hostPort)
        .toString();
  }
}
