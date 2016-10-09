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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.spotify.docker.client.messages.Event;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public class EventReader implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(LogReader.class);
  private final ObjectMapper objectMapper;
  private final CloseableHttpResponse response;
  private JsonParser parser;

  private volatile boolean closed;

  public EventReader(final CloseableHttpResponse response, final ObjectMapper objectMapper) {
    this.response = response;
    this.objectMapper = objectMapper;
  }

  public Event nextMessage() throws IOException {
    if (this.parser == null) {
      this.parser = objectMapper.getFactory().createParser(response.getEntity().getContent());
    }

    // If the parser is closed, there's no new event
    if (this.parser.isClosed()) {
      return null;
    }

    // Read tokens until we get a start object
    if (parser.nextToken() == null) {
      return null;
    }

    return parser.readValueAs(Event.class);
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
  public void close() throws IOException {
    closed = true;
    response.close();
  }

}
