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

package com.spotify.docker.client.exceptions;

public class VolumeNotFoundException extends DockerException {

  private final String volumeName;

  public VolumeNotFoundException(final String name, final Throwable cause) {
    super(String.format("Cannot remove volume %s. No such volume or volume driver.", name), cause);
    this.volumeName = name;
  }

  public VolumeNotFoundException(final String volumeName) {
    this(volumeName, null);
  }

  public String getVolumeName() {
    return volumeName;
  }
}
