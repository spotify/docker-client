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

import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.spotify.docker.client.messages.Event;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public class EventStream extends AbstractIterator<Event> implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(EventStream.class);

  private final EventReader reader;
  private volatile boolean closed;

  EventStream(final CloseableHttpResponse response, final ObjectMapper objectMapper) {
    this.reader = new EventReader(response, objectMapper);
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
  protected Event computeNext() {
    final Event event;
    try {
      event = reader.nextMessage();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    if (event == null) {
      return endOfData();
    }
    return event;
  }

  @Override
  public void close() {
    closed = true;
    try {
      reader.close();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

}
