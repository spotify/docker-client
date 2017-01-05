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

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class PortBinding {

  @Nullable
  @JsonProperty("HostIp")
  public abstract String hostIp();

  @JsonProperty("HostPort")
  public abstract String hostPort();

  /**
   * @deprecated  As of release 7.0.0, replaced by {@link #of(String, String)}.
   */
  @Deprecated
  public static PortBinding hostPort(final String port) {
    return new AutoValue_PortBinding(null, port);
  }

  public static PortBinding of(final String ip, final String port) {
    return new AutoValue_PortBinding(ip, port);
  }

  public static PortBinding of(final String ip, final int port) {
    return new AutoValue_PortBinding(ip, String.valueOf(port));
  }

  public static PortBinding randomPort(final String ip) {
    return new AutoValue_PortBinding(ip, "");
  }

  @JsonCreator
  public static PortBinding create(
      @JsonProperty("HostIp") final String hostIp,
      @JsonProperty("HostPort") final String hostPort) {
    return new AutoValue_PortBinding(hostIp, hostPort);
  }
}
