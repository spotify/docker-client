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

package com.spotify.docker.client;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ProgressMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingLoadHandler implements ProgressHandler {

  private static final Logger log = LoggerFactory.getLogger(LoggingLoadHandler.class);

  @Override
  public void progress(ProgressMessage message) throws DockerException {
    if (message.error() != null) {
      throw new DockerException(message.toString());
    }

    log.info("load: {}", message);
  }

}
