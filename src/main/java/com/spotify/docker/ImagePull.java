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

package com.spotify.docker;

import com.google.common.base.Throwables;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import static com.spotify.docker.ObjectMapperProvider.objectMapper;

class ImagePull implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(ImagePull.class);
  private final InputStream stream;

  private volatile boolean closed;

  ImagePull(final InputStream stream) {
    this.stream = stream;
  }

  void tail(final String image)
      throws DockerException {
    try {
      final JsonParser parser = objectMapper().getFactory().createParser(stream);
      final MappingIterator<JsonNode> iterator = objectMapper().readValues(parser, JsonNode.class);
      while (iterator.hasNextValue()) {
        final JsonNode message;
        message = iterator.nextValue();
        final JsonNode error = message.get("error");
        if (error != null) {
          if (error.toString().contains("404")) {
            throw new ImageNotFoundException(image, message.toString());
          } else {
            throw new ImagePullFailedException(image, message.toString());
          }
        }
        log.info("pull {}: {}", image, message);
      }
    } catch (IOException e) {
      throw new DockerException(e);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (!closed) {
      log.warn(this + " not closed properly");
      close();
    }
  }

  @Override
  public void close() {
    closed = true;
    try {
      stream.close();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
