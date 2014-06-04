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
import com.google.common.collect.AbstractIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Charsets.UTF_8;

public class LogStream extends AbstractIterator<LogMessage> implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(LogStream.class);

  private final LogReader reader;
  private volatile boolean closed;

  LogStream(final InputStream stream) {
    this.reader = new LogReader(stream);
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
  protected LogMessage computeNext() {
    final LogMessage message;
    try {
      message = reader.nextMessage();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    if (message == null) {
      return endOfData();
    }
    return message;
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

  public String readFully() {
    StringBuilder stringBuilder = new StringBuilder();
    while (hasNext()) {
      stringBuilder.append(UTF_8.decode(next().content()));
    }
    return stringBuilder.toString();
  }
}
