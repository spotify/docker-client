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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.spotify.docker.client.messages.Event;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import static com.google.common.base.Charsets.*;

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
