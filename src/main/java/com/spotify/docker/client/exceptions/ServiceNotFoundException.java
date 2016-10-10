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

public class ServiceNotFoundException extends NotFoundException {

  private static final long serialVersionUID = 124167900943701078L;

  private final String serviceId;

  public ServiceNotFoundException(final String serviceId, final Throwable cause) {
    super("Service not found: " + serviceId, cause);
    this.serviceId = serviceId;
  }

  public ServiceNotFoundException(final String serviceId) {
    this(serviceId, null);
  }

  public String getServiceId() {
    return serviceId;
  }
}
