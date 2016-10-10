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


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

public class Device {

  @JsonProperty("PathOnHost")
  private String pathOnHost;
  @JsonProperty("PathInContainer")
  private String pathInContainer;
  @JsonProperty("CgroupPermissions")
  private String cgroupPermissions;

  public Device() {
  }

  public Device(final String pathOnHost, final String pathInContainer,
                final String cgroupPermissions) {
    this.pathOnHost = pathOnHost;
    this.pathInContainer = pathInContainer;
    this.cgroupPermissions = cgroupPermissions;
  }

  public String pathOnHost() {
    return pathOnHost;
  }

  public String pathInContainer() {
    return pathInContainer;
  }

  public String cgroupPermissions() {
    return cgroupPermissions;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final Device that = (Device) obj;

    return Objects.equals(this.pathOnHost, that.pathOnHost)
           && Objects.equals(this.pathInContainer, that.pathInContainer)
           && Objects.equals(this.cgroupPermissions, that.cgroupPermissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pathOnHost, pathInContainer, cgroupPermissions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("pathOnHost", pathOnHost)
        .add("pathInContainer", pathInContainer)
        .add("cgroupPermissions", cgroupPermissions)
        .toString();
  }
}
