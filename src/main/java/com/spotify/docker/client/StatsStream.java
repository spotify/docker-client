/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2018 Davi da Silva Böger
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

import static com.spotify.docker.client.ObjectMapperProvider.objectMapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MappingIterator;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.messages.ContainerStats;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.message.internal.ReaderInterceptorExecutor;


class StatsStream implements Closeable {

  private final InputStream stream;
  private final MappingIterator<ContainerStats> iterator;

  StatsStream(final InputStream stream) throws IOException {
    this.stream = stream;
    final JsonParser parser = objectMapper().getFactory().createParser(stream);
    iterator = objectMapper().readValues(parser, ContainerStats.class);
  }

  public boolean hasNextMessage(final String method, final URI uri) throws DockerException {
    try {
      return iterator.hasNextValue();
    } catch (SocketTimeoutException e) {
      throw new DockerTimeoutException(method, uri, e);
    } catch (IOException e) {
      throw new DockerException(e);
    }
  }

  public ContainerStats nextMessage(final String method, final URI uri) throws DockerException {
    try {
      return iterator.nextValue();
    } catch (SocketTimeoutException e) {
      throw new DockerTimeoutException(method, uri, e);
    } catch (IOException e) {
      throw new DockerException(e);
    }
  }

  public void tail(StatsHandler handler, final String method, final URI uri)
      throws DockerException, InterruptedException {
    try {
      while (hasNextMessage(method, uri)) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
        if (!handler.stats(nextMessage(method, uri))) {
          return;
        }
      }
    } finally {
      IOUtils.closeQuietly(this);
    }
  }

  @Override
  public void close() throws IOException {
    // We need to close the stream, else the Docker daemon will never stop sending data
    ReaderInterceptorExecutor.closeableInputStream(stream).close();
  }
}
