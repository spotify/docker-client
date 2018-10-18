/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2018 Davi da Silva Böger
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

package com.spotify.docker.client;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerStats;

public interface StatsHandler {

  /**
   * Handle container stats from the stream.
   *
   * @return {@code true} to continue receiving stats, {@code false} to close the stream.
   */
  boolean stats(ContainerStats stats) throws DockerException;

}
