/*
 * Copyright (c) 2016 Spotify AB.
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

public class ContainerRenameConflictException extends ConflictException {

  private final String containerId;
  private final String newName;

  public ContainerRenameConflictException(final String containerId, final String newName,
                                          final Throwable cause) {
    super("Container " + containerId + " could not be renamed to " + newName, cause);
    this.containerId = containerId;
    this.newName = newName;
  }

  public ContainerRenameConflictException(final String containerId, final String newName) {
    this(containerId, newName, null);
  }

  public String getContainerId() {
    return containerId;
  }

  public String getNewName() {
    return newName;
  }
}
