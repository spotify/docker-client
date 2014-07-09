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

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * A client handler that makes the jersey client interruptible by executing all http requests on a
 * threadpool.
 */
class InterruptibleURLConnectionClientHandler extends TerminatingClientHandler {

  private final ExecutorService executor = MoreExecutors.getExitingExecutorService(
      (ThreadPoolExecutor) Executors.newCachedThreadPool(), 0, SECONDS);

  @Override
  public ClientResponse handle(final ClientRequest cr) throws ClientHandlerException {
    final RequestTask request = new RequestTask(cr);
    final Future<ClientResponse> future = executor.submit(request);
    try {
      return future.get();
    } catch (InterruptedException e) {
      request.close();
      throw new ClientHandlerException(new InterruptedIOException(e.toString()));
    } catch (ExecutionException e) {
      request.close();
      final Throwable cause = e.getCause() == null ? e : e.getCause();
      Throwables.propagateIfInstanceOf(cause, ClientHandlerException.class);
      throw new ClientHandlerException(cause);
    }
  }

  private class RequestTask implements Callable<ClientResponse>, HttpURLConnectionFactory {

    private final List<HttpURLConnection> connections = Lists.newCopyOnWriteArrayList();

    private final ClientRequest cr;

    public RequestTask(final ClientRequest cr) {
      this.cr = cr;
    }

    @Override
    public ClientResponse call() throws Exception {
      final TerminatingClientHandler handler = new URLConnectionClientHandler(this);
      handler.setMessageBodyWorkers(getMessageBodyWorkers());
      return handler.handle(cr);
    }

    @Override
    public HttpURLConnection getHttpURLConnection(final URL url) throws IOException {
      final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connections.add(connection);
      return connection;
    }

    public void close() {
      for (HttpURLConnection connection : connections) {
        connection.disconnect();
      }
    }
  }
}
