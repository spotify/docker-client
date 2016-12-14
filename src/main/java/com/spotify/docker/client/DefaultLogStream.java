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

import static com.google.common.base.Charsets.UTF_8;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.io.Closer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class DefaultLogStream extends AbstractIterator<LogMessage> implements LogStream {

  private final LogReader reader;

  private DefaultLogStream(final InputStream stream) {
    this(new LogReader(stream));
  }

  @VisibleForTesting
  DefaultLogStream(final LogReader reader) {
    this.reader = reader;
  }

  static DefaultLogStream create(final InputStream stream) {
    return new DefaultLogStream(stream);
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

  public void attach(final OutputStream stdout, final OutputStream stderr, boolean closeAtEof)
      throws IOException {
    final Closer closer = Closer.create();
    try {
      if (closeAtEof) {
        closer.register(stdout);
        closer.register(stderr);
      }

      while (this.hasNext()) {
        final LogMessage message = this.next();
        final ByteBuffer content = message.content();

        switch (message.stream()) {
          case STDOUT:
            writeAndFlush(content, stdout);
            break;
          case STDERR:
            writeAndFlush(content, stderr);
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

  /** Write the contents of the given ByteBuffer to the OutputStream and flush the stream. */
  private static void writeAndFlush(
      final ByteBuffer buffer, final OutputStream outputStream) throws IOException {

    if (buffer.hasArray()) {
      outputStream.write(buffer.array(), buffer.position(), buffer.remaining());
    } else {
      // cannot access underlying byte array, need to copy into a temporary array
      while (buffer.hasRemaining()) {
        // figure out how much to read, but use an upper limit of 8kb. LogMessages should be rather
        // small so we don't expect this to get hit but avoid large temporary buffers, just in case.
        final int size = Math.min(buffer.remaining(), 8 * 1024);
        final byte[] chunk = new byte[size];
        buffer.get(chunk);
        outputStream.write(chunk);
      }
    }
    outputStream.flush();
  }

}
