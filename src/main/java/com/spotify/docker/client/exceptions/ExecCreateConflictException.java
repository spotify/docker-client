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

public class ExecCreateConflictException extends ConflictException {

  private final String containerId;

  public ExecCreateConflictException(final String containerId,
                                     final Throwable cause) {
    super("Could not create exec. Container " + containerId + " is paused.", cause);
    this.containerId = containerId;
  }

  public ExecCreateConflictException(final String containerId) {
    this(containerId, null);
  }

  public String getContainerId() {
    return containerId;
  }
}
