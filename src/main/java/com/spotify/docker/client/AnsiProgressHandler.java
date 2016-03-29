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

import com.google.common.collect.Maps;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ProgressMessage;

import java.io.PrintStream;
import java.util.Map;

/**
 * Parses ProgressMessage objects and writes the output to a PrintStream. The output includes ANSI
 * escape characters to move the cursor around to nicely print progress bars.
 */
public class AnsiProgressHandler implements ProgressHandler {

  private static final char ESC_CODE = 0x1B;

  private final PrintStream out;
  private final Map<String, Integer> idsToLines;

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public AnsiProgressHandler() {
    this(System.out);
  }

  public AnsiProgressHandler(PrintStream out) {
    this.out = out;
    idsToLines = Maps.newHashMap();
  }

  @Override
  public void progress(ProgressMessage message) throws DockerException {
    if (message.error() != null) {
      throw new DockerException(message.error());
    }

    if (message.progressDetail() != null) {
      printProgress(message);
      return;
    }

    String value = message.stream();
    if (value != null) {
      // trim trailing new lines which are present in streams
      value = value.replaceFirst("\n$", "");
    } else {
      value = message.status();
    }
    // if value is null it's an unknown message type so just print the whole thing out
    if (value == null) {
      value = message.toString();
    }

    out.println(value);
  }

  /**
   * Displays the upload/download status of multiple image layers the same way the docker CLI does.
   * The current status of each layer is show on its own line. As the status updated, we move the
   * cursor to the correct line, and overwrite the old status with the new one.
   *
   * @param message the ProgressMessage to parse
   */
  private void printProgress(final ProgressMessage message) {

    final String id = message.id();
    Integer line = idsToLines.get(id);
    int diff = 0;

    if (line == null) {
      line = idsToLines.size();
      idsToLines.put(id, line);
    } else {
      diff = idsToLines.size() - line;
    }

    if (diff > 0) {
      // move cursor up to the line for this image layer
      out.printf("%c[%dA", ESC_CODE, diff);
      // delete entire line
      out.printf("%c[2K\r", ESC_CODE);
    }

    // The progress bar graphic is in the 'progress' element. Some messages like "Pulling
    // dependent layers" don't have a progress bar, in which case set to empty string.
    String progress = message.progress();
    if (progress == null) {
      progress = "";
    }

    // this will print something like "90b15849fc7e: Downloading [==>] 5.812 MB/117.4 MB 4m12s"
    out.printf("%s: %s %s%n", id, message.status(), progress);

    if (diff > 0) {
      // move cursor back down to bottom
      out.printf("%c[%dB", ESC_CODE, diff - 1);
    }
  }

}
