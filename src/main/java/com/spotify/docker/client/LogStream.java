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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

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
    final StringBuilder stringBuilder = new StringBuilder();
    while (hasNext()) {
      stringBuilder.append(UTF_8.decode(next().content()));
    }
    return stringBuilder.toString();
  }

  /**
   * Attach {@link java.io.OutputStream} to the {@link LogStream}.
   *
   * <p> <b>Example usage:</b> </p>
   *
   * <pre>
   * {@code
   * dockerClient
   *     .attachContainer(containerId,
   *         AttachParameter.LOGS, AttachParameter.STDOUT,
   *         AttachParameter.STDERR, AttachParameter.STREAM)
   *     .attach(System.out, System.err);
   * }
   * </pre>
   *
   * <p> Typically you use {@link java.io.PipedOutputStream} connected to a {@link
   * java.io.PipedInputStream} which are read by - for example - an {@link
   * java.io.InputStreamReader} or a {@link java.util.Scanner}. For small inputs, the {@link
   * java.io.PipedOutputStream} just writes to the buffer of the {@link java.io.PipedInputStream},
   * but you actually want to read and write from separate threads, as it may deadlock the thread.
   * </p>
   *
   * <pre>
   * {@code
   *   final PipedInputStream stdout = new PipedInputStream();
   *   final PipedInputStream stderr = new PipedInputStream();
   *   final PipedOutputStream stdout_pipe = new PipedOutputStream(stdout);
   *   final PipedOutputStream stderr_pipe = new PipedOutputStream(stderr);
   *
   *   executor.submit(new Callable&lt;Void&gt;() {
   *     &#064;Override
   *     public Void call() throws Exception {
   *       dockerClient.attachContainer(containerId,
   *           AttachParameter.LOGS, AttachParameter.STDOUT,
   *           AttachParameter.STDERR, AttachParameter.STREAM
   *         .attach(stdout_pipe, stderr_pipe);
   *       return null;
   *     }
   *   });
   *
   *   try (Scanner sc_stdout = new Scanner(stdout); Scanner sc_stderr = new Scanner(stderr)) {
   *     // ... read here
   *   }
   * }
   * </pre>
   *
   * @param stdout OutputStream for the standard out.
   * @param stderr OutputStream for the standard err
   * @throws IOException if an I/O error occurs.
   * @see java.io.PipedInputStream
   * @see java.io.PipedOutputStream
   */
  public void attach(final OutputStream stdout, final OutputStream stderr) throws IOException {
    try (WritableByteChannel stdoutChannel = Channels.newChannel(stdout);
         WritableByteChannel stderrChannel = Channels.newChannel(stderr)) {
      for (LogMessage message; hasNext(); ) {
        message = next();
        switch (message.stream()) {
          case STDOUT:
            stdoutChannel.write(message.content());
            break;
          case STDERR:
            stderrChannel.write(message.content());
            break;
          case STDIN:
          default:
            break;
        }
      }
    }
  }

}
