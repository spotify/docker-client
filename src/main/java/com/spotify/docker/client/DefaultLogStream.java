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

import static com.google.common.base.Charsets.UTF_8;

import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class DefaultLogStream extends AbstractIterator<LogMessage> implements LogStream {

  private static final Logger log = LoggerFactory.getLogger(DefaultLogStream.class);

  private final LogReader reader;
  private volatile boolean closed;

  private DefaultLogStream(final InputStream stream) {
    this.reader = new LogReader(stream);
  }

  static DefaultLogStream create(final InputStream stream) {
    return new DefaultLogStream(stream);
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
    final StringBuilder stringBuilder = new StringBuilder();
    while (hasNext()) {
      stringBuilder.append(UTF_8.decode(next().content()));
    }
    return stringBuilder.toString();
  }

  public void attach(final OutputStream stdout, final OutputStream stderr) throws IOException {
    attach(stdout, stderr, true);
  }

  public void attach(final OutputStream stdout, final OutputStream stderr, boolean closeAtEOF)
      throws IOException {
    final Closer closer = Closer.create();
    try {
      if (closeAtEOF) {
        closer.register(stdout);
        closer.register(stderr);
      }

      while (this.hasNext()) {
        final LogMessage message = this.next();
        final ByteBuffer content = message.content();

        assert content.hasArray();

        switch (message.stream()) {
          case STDOUT:
            stdout.write(content.array(), content.position(), content.remaining());
            stdout.flush();
            break;
          case STDERR:
            stderr.write(content.array(), content.position(), content.remaining());
            stderr.flush();
            break;
          case STDIN:
          default:
            break;
        }
      }
    } catch (Throwable t) {
      throw closer.rethrow(t);
    } finally {
      closer.close();
    }
  }
}
