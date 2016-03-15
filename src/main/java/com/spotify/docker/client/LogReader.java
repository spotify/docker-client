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

import com.google.common.io.ByteStreams;

import com.spotify.docker.client.LogMessage.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static com.google.common.io.ByteStreams.copy;
import static com.google.common.io.ByteStreams.nullOutputStream;

public class LogReader implements Closeable {

  private static final Logger log = LoggerFactory.getLogger(LogReader.class);
  private final InputStream stream;
  public static final int HEADER_SIZE = 8;
  public static final int FRAME_SIZE_OFFSET = 4;

  private volatile boolean closed;

  public LogReader(final InputStream stream) {
    this.stream = stream;
  }

  public LogMessage nextMessage() throws IOException {
    stream.mark(HEADER_SIZE);

    // Read header
    final byte[] headerBytes = new byte[HEADER_SIZE];
    final int n = ByteStreams.read(stream, headerBytes, 0, HEADER_SIZE);
    if (n == 0) {
      return null;
    }
    if (n != HEADER_SIZE) {
      throw new EOFException();
    }
    final ByteBuffer header = ByteBuffer.wrap(headerBytes);
    int streamId = header.get();
    final int idZ = header.getInt(0);

    // Read frame
    final byte[] frame;
    // Header format is : {STREAM_TYPE, 0, 0, 0, SIZE1, SIZE2, SIZE3, SIZE4}
    if (idZ == 0 || idZ == 0x01000000 || idZ == 0x02000000) {
      header.position(FRAME_SIZE_OFFSET);
      final int frameSize = header.getInt();
      frame = new byte[frameSize];
    } else {
      stream.reset();
      streamId = Stream.STDOUT.id();
      frame = new byte[stream.available()];
    }
    ByteStreams.readFully(stream, frame);
    return new LogMessage(streamId, ByteBuffer.wrap(frame));
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
    // Jersey will close the stream and release the connection after we read all the data.
    // We cannot call the stream's close method because it an instance of UncloseableInputStream,
    // where close is a no-op.
    copy(stream, nullOutputStream());
  }

}
