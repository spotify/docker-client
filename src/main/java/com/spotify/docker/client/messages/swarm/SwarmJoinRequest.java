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

package com.spotify.docker.client.messages.swarm;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.google.common.base.Strings.isNullOrEmpty;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class SwarmJoinRequest {

  private static final String DEFAULT_LISTEN_ADDR = "0.0.0.0:2377";

  @JsonProperty("ListenAddr")
  private String listenAddr;

  @JsonProperty("AdvertiseAddr")
  private String advertiseAddr;

  @JsonProperty("RemoteAddrs")
  private ImmutableList<String> remoteAddrs;

  @JsonProperty("JoinToken")
  private String joinToken;

  private SwarmJoinRequest(final SwarmJoinRequest.Builder builder) {
    this.listenAddr = isNullOrEmpty(builder.listenAddr)
            ? DEFAULT_LISTEN_ADDR : builder.listenAddr;
    this.advertiseAddr = builder.advertiseAddr;
    this.remoteAddrs = builder.remoteAddrs;
    this.joinToken = builder.joinToken;
  }

  public String listenAddr() {
    return listenAddr;
  }

  public String advertiseAddr() {
    return advertiseAddr;
  }

  public ImmutableList<String> remoteAddrs() {
    return remoteAddrs;
  }

  public String joinToken() {
    return joinToken;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final SwarmJoinRequest that = (SwarmJoinRequest) obj;

    return Objects.equals(this.listenAddr, that.listenAddr)
            && Objects.equals(this.advertiseAddr, that.advertiseAddr)
            && Objects.equals(this.remoteAddrs, that.remoteAddrs)
            && Objects.equals(this.joinToken, that.joinToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(listenAddr, advertiseAddr, remoteAddrs, joinToken);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
            .add("listenAddr", listenAddr)
            .add("advertiseAddr", advertiseAddr)
            .add("remoteAddrs", remoteAddrs)
            .add("joinToken", joinToken)
            .toString();
  }

  public static SwarmJoinRequest.Builder builder() {
    return new SwarmJoinRequest.Builder();
  }

  public static class Builder {
    private String listenAddr;
    private String advertiseAddr;
    private ImmutableList<String> remoteAddrs;
    private String joinToken;

    public SwarmJoinRequest.Builder listenAddr(final String listenAddr) {
      this.listenAddr = listenAddr;
      return this;
    }

    public SwarmJoinRequest.Builder advertiseAddr(final String advertiseAddr) {
      this.advertiseAddr = advertiseAddr;
      return this;
    }

    public SwarmJoinRequest.Builder remoteAddrs(final ImmutableList<String> remoteAddrs) {
      this.remoteAddrs = remoteAddrs;
      return this;
    }

    public SwarmJoinRequest.Builder joinToken(final String joinToken) {
      this.joinToken = joinToken;
      return this;
    }

    public SwarmJoinRequest build() {
      return new SwarmJoinRequest(this);
    }
  }

}
