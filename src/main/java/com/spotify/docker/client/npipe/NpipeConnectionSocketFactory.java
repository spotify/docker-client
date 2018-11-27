/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2018 Spotify AB
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

package com.spotify.docker.client.npipe;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

/**
 * Provides a ConnectionSocketFactory for connecting Apache HTTP clients to windows named pipe.
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE_CONDITIONAL)
public class NpipeConnectionSocketFactory implements ConnectionSocketFactory {

  private File socketFile;

  public NpipeConnectionSocketFactory(final URI socketUri) {
    super();

    final String filename = socketUri.toString()
        .replaceAll("^npipe:///", "npipe://localhost/")
        .replaceAll("^npipe://localhost", "");

    this.socketFile = new File(filename);
  }

  public static URI sanitizeUri(final URI uri) {
    if (uri.getScheme().equals("npipe")) {
      return URI.create("npipe://localhost:80");
    } else {
      return uri;
    }
  }

  @Override
  public Socket createSocket(final HttpContext context) throws IOException {
    return new NamedPipeSocket();
  }

  @Override
  public Socket connectSocket(final int connectTimeout,
                              final Socket socket,
                              final HttpHost host,
                              final InetSocketAddress remoteAddress,
                              final InetSocketAddress localAddress,
                              final HttpContext context) throws IOException {
    if (!(socket instanceof NamedPipeSocket)) {
      throw new AssertionError("Unexpected socket: " + socket);
    }
    socket.connect(new NpipeSocketAddress(socketFile), connectTimeout);
    return socket;
  }
}
