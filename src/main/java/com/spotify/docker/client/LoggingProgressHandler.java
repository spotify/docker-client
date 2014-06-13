/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import com.spotify.docker.client.messages.ProgressMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProgressHandler implements ProgressHandler {

  private static final Logger log = LoggerFactory.getLogger(LoggingProgressHandler.class);

  private final String image;

  public LoggingProgressHandler(String image) {
    this.image = image;
  }

  @Override
  public void progress(ProgressMessage message) throws DockerException {
    if (message.error() != null) {
      if (message.error().contains("404")) {
        throw new ImageNotFoundException(image, message.toString());
      } else {
        throw new ImagePullFailedException(image, message.toString());
      }
    }

    log.info("pull {}: {}", image, message);
  }

}
