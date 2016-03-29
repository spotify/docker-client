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

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ProgressMessage;

/**
 * Handler for processing progress messages received from Docker during pull, push and build
 * operations.
 */
public interface ProgressHandler {

  /**
   * This method will be called for each progress message received from Docker.
   *
   * @param message the message to process
   * @throws DockerException if a server error occurred (500)
   */
  void progress(ProgressMessage message) throws DockerException;

}
