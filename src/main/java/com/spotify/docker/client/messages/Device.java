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


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

public class Device {

  @JsonProperty("PathOnHost")
  private String pathOnHost;
  @JsonProperty("PathInContainer")
  private String pathInContainer;
  @JsonProperty("CgroupPermissions")
  private String cGroupPermissions;

  public Device() {
  }

  public Device(final String pathOnHost, final String pathInContainer,
                final String cGroupPermissions) {
    this.pathOnHost = pathOnHost;
    this.pathInContainer = pathInContainer;
    this.cGroupPermissions = cGroupPermissions;
  }

  public String pathOnHost() {
    return pathOnHost;
  }

  public String pathInContainer() {
    return pathInContainer;
  }

  public String cGroupPermissions() {
    return cGroupPermissions;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Device that = (Device) o;

    return Objects.equals(this.pathOnHost, that.pathOnHost) &&
           Objects.equals(this.pathInContainer, that.pathInContainer) &&
           Objects.equals(this.cGroupPermissions, that.cGroupPermissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pathOnHost, pathInContainer, cGroupPermissions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("pathOnHost", pathOnHost)
        .add("pathInContainer", pathInContainer)
        .add("cGroupPermissions", cGroupPermissions)
        .toString();
  }
}
