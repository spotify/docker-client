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

package com.spotify.docker.client;

public class NetworkDriverNotFoundException extends DockerException {

  private final String driverName;

  public NetworkDriverNotFoundException(final String driverName, final Throwable cause) {
    super("Network driver not found: " + driverName, cause);
    this.driverName = driverName;
  }

  public NetworkDriverNotFoundException(final String driverName) {
    this(driverName, null);
  }

  public String getDriverName() {
    return driverName;
  }
}
