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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.spotify.docker.client.messages.Event;

import java.io.Closeable;
import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;

public class EventStream extends AbstractIterator<Event> implements Closeable {

  private final EventReader reader;

  EventStream(final CloseableHttpResponse response, final ObjectMapper objectMapper) {
    this.reader = new EventReader(response, objectMapper);
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
    try {
      reader.close();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
