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

import com.google.common.io.ByteStreams;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class LogReader implements Closeable {

  private final InputStream stream;
  public static final int HEADER_SIZE = 8;
  public static final int FRAME_SIZE_OFFSET = 4;

  public LogReader(final InputStream stream) {
    this.stream = stream;
  }

  @Override
  public void close() throws IOException {
    stream.close();
  }

  public LogMessage nextMessage() throws IOException {

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
    final int streamId = header.get();
    header.position(FRAME_SIZE_OFFSET);
    final int frameSize = header.getInt();

    // Read frame
    final byte[] frame = new byte[frameSize];
    ByteStreams.readFully(stream, frame);
    return new LogMessage(streamId, ByteBuffer.wrap(frame));
  }
}
