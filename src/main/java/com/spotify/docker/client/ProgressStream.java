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

import com.google.common.base.Throwables;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.spotify.docker.client.messages.ProgressMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import static com.spotify.docker.client.ObjectMapperProvider.objectMapper;

class ProgressStream implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(ProgressStream.class);
  private final InputStream stream;
  private final MappingIterator<ProgressMessage> iterator;

  private volatile boolean closed;

  ProgressStream(final InputStream stream) throws IOException {
    this.stream = stream;
    final JsonParser parser = objectMapper().getFactory().createParser(stream);
    iterator = objectMapper().readValues(parser, ProgressMessage.class);
  }

  public boolean hasNextMessage() throws DockerException {
    try {
      return iterator.hasNextValue();
    } catch (IOException e) {
      throw new DockerException(e);
    }
  }

  public ProgressMessage nextMessage() throws DockerException {
    try {
      return iterator.nextValue();
    } catch (IOException e) {
      throw new DockerException(e);
    }
  }

  public void tail(ProgressHandler handler) throws DockerException {
    while (hasNextMessage()) {
      handler.progress(nextMessage());
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
