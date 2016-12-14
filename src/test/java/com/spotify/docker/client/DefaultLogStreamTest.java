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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.junit.Test;

public class DefaultLogStreamTest {

  private final LogReader reader = mock(LogReader.class);
  private final DefaultLogStream logStream = new DefaultLogStream(reader);

  @Test
  public void testAttach() throws Exception {
    when(reader.nextMessage()).thenReturn(
        logMessage(LogMessage.Stream.STDOUT, "hello\n"),
        logMessage(LogMessage.Stream.STDERR, "oops\n"),
        logMessage(LogMessage.Stream.STDOUT, "world!\n"),
        // need to return null to signal end of stream
        null
    );

    final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    final ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    logStream.attach(stdout, stderr);

    assertThat(stdout.toString(), is("hello\nworld!\n"));
    assertThat(stderr.toString(), is("oops\n"));
  }

  private static LogMessage logMessage(LogMessage.Stream stream, String msg) {
    return new LogMessage(stream, ByteBuffer.wrap(msg.getBytes()));
  }
}
