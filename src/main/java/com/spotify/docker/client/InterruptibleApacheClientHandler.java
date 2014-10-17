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

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;

import de.gesellix.socketfactory.unix.UnixSocketFactory;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.HttpConnectionParams;

import java.io.InterruptedIOException;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import socketfactory.SocketFactoryService;
import socketfactory.spi.SocketFactory;

/**
 * A client handler that makes the Apache client interruptible by executing all http requests on a
 * threadpool.
 */
class InterruptibleApacheClientHandler extends TerminatingClientHandler {

  private final URI baseUri;

  private final ExecutorService executor;

  InterruptibleApacheClientHandler(final URI baseUri, final ExecutorService executor) {
    this.baseUri = baseUri;
    this.executor = executor;
  }

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

  private class RequestTask implements Callable<ClientResponse> {

    private final DefaultHttpClient httpClient;

    private final ClientRequest cr;

    public RequestTask(final ClientRequest cr) {
      this.cr = cr;

      this.httpClient = new DefaultHttpClient();
      this.httpClient.getParams()
          .setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT,
                           (Integer) cr.getProperties().get(ClientConfig.PROPERTY_CONNECT_TIMEOUT))
          .setIntParameter(HttpConnectionParams.SO_TIMEOUT,
                           (Integer) cr.getProperties().get(ClientConfig.PROPERTY_READ_TIMEOUT));
      this.httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));

      if (cr.getURI().getScheme().equalsIgnoreCase("unix")) {
        SocketFactory sf = new UnixSocketFactory();
        sf.configure(httpClient, (String) sf.sanitize(baseUri.toString()));
      }
    }

    @Override
    public ClientResponse call() throws Exception {
      final TerminatingClientHandler handler = new ApacheHttpClient4Handler(httpClient,
                                                                            null,
                                                                            false);
      handler.setMessageBodyWorkers(getMessageBodyWorkers());
      return handler.handle(cr);
    }

    public void close() {
      httpClient.getConnectionManager().shutdown();
    }
  }
}
