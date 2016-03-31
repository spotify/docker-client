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

import com.google.common.collect.Queues;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.Queue;

import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;

/**
 * Provides a socket that wraps an jnr.unixsocket.UnixSocketChannel and delays setting options until
 * the socket is connected. This is necessary because the Apache HTTP client attempts to set options
 * prior to connecting the socket, which doesn't work for Unix sockets since options are being set
 * on the underlying file descriptor. Until the socket is connected, the file descriptor doesn't
 * exist.
 *
 * This class also noop's any calls to setReuseAddress, which is called by the Apache client but
 * isn't supported by AFUnixSocket.
 */
public class ApacheUnixSocket extends Socket {

  private final UnixSocketChannel inner;
  private SocketAddress addr;
  private int lingerTime;

  private final Queue<SocketOptionSetter> optionsToSet = Queues.newArrayDeque();

  public ApacheUnixSocket() throws IOException {
    this.inner = UnixSocketChannel.open();
    this.addr = null;
  }

  @Override
  public void connect(final SocketAddress endpoint) throws IOException {
    if (endpoint instanceof UnixSocketAddress) {
      addr = endpoint;
      inner.connect((UnixSocketAddress) endpoint);
      setAllSocketOptions();
    }
  }

  @Override
  public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
    if (endpoint instanceof UnixSocketAddress) {
      addr = endpoint;
      inner.connect((UnixSocketAddress) endpoint);
      setAllSocketOptions();
    }
  }

  @Override
  public void bind(final SocketAddress bindpoint) throws IOException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public InetAddress getInetAddress() {
    if (inner.isConnected()) {
      try {
        return InetAddress.getByName("localhost");
      } catch (UnknownHostException e) {
        return null;
      }
    }
    return null;
  }

  @Override
  public InetAddress getLocalAddress() {
    try {
      return InetAddress.getByAddress(new byte[] {0, 0, 0, 0}); // not bound
    } catch (UnknownHostException e) {
      return null;
    }
  }

  @Override
  public int getPort() {
    return -1; // meaningless for UNIX sockets
  }

  @Override
  public int getLocalPort() {
    return -1; // not bound
  }

  @Override
  public SocketAddress getRemoteSocketAddress() {
    return addr;
  }

  @Override
  public SocketAddress getLocalSocketAddress() {
    return null; // not bound
  }

  @Override
  public SocketChannel getChannel() {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return Channels.newInputStream(inner);
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return Channels.newOutputStream(inner);
  }

  private void setSocketOption(final SocketOptionSetter s) throws SocketException {
    if (inner.isConnected()) {
      s.run();
    } else {
      if (!optionsToSet.offer(s)) {
        throw new SocketException("Failed to queue option");
      }
    }
  }

  private void setAllSocketOptions() throws SocketException {
    for (final SocketOptionSetter s : optionsToSet) {
      s.run();
    }
  }

  @Override
  public void setTcpNoDelay(final boolean on) throws SocketException {
  }

  @Override
  public boolean getTcpNoDelay() throws SocketException {
    return false;
  }

  @Override
  public void setSoLinger(final boolean on, final int linger) throws SocketException {
    if (on) {
      lingerTime = linger;
    }
  }

  @Override
  public int getSoLinger() throws SocketException {
    return lingerTime;
  }

  @Override
  public void sendUrgentData(final int data) throws IOException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public void setOOBInline(final boolean on) throws SocketException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public boolean getOOBInline() throws SocketException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public synchronized void setSoTimeout(final int timeout) throws SocketException {
    setSocketOption(new SocketOptionSetter() {
      @Override
      public void run() throws SocketException {
        inner.setSoTimeout(timeout);
      }
    });
  }

  @Override
  public synchronized int getSoTimeout() throws SocketException {
    return inner.getSoTimeout();
  }

  @Override
  public synchronized void setSendBufferSize(final int size) throws SocketException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public synchronized int getSendBufferSize() throws SocketException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public synchronized void setReceiveBufferSize(final int size) throws SocketException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public synchronized int getReceiveBufferSize() throws SocketException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public void setKeepAlive(final boolean on) throws SocketException {
    setSocketOption(new SocketOptionSetter() {
      @Override
      public void run() throws SocketException {
        inner.setKeepAlive(on);
      }
    });
  }

  @Override
  public boolean getKeepAlive() throws SocketException {
    return inner.getKeepAlive();
  }

  @Override
  public void setTrafficClass(final int tc) throws SocketException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public int getTrafficClass() throws SocketException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public void setReuseAddress(final boolean on) throws SocketException {
    // not supported: Apache client tries to set it, but we want to just ignore it
  }

  @Override
  public boolean getReuseAddress() throws SocketException {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public synchronized void close() throws IOException {
    if (lingerTime > 0) {
      boolean sleeping = true;
      while (sleeping) {
        try {
          wait(lingerTime * (long) 1000);
        } catch (InterruptedException e) {
        }
        sleeping = false;
      }
    }

    try {
      shutdownInput();
    } finally {
      try {
        shutdownOutput();
      } finally {
        inner.close();
      }
    }
  }

  @Override
  public void shutdownInput() throws IOException {
    inner.shutdownInput();
  }

  @Override
  public void shutdownOutput() throws IOException {
    inner.shutdownOutput();
  }

  @Override
  public String toString() {
    if (addr != null) {
      return ((UnixSocketAddress) addr).toString();
    }
    return inner.toString();
  }

  @Override
  public boolean isConnected() {
    return inner.isConnected();
  }

  @Override
  public boolean isBound() {
    return false;
  }

  @Override
  public boolean isClosed() {
    return !inner.isOpen();
  }

  @Override
  public boolean isInputShutdown() {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public boolean isOutputShutdown() {
    throw new UnsupportedOperationException("Unimplemented");
  }

  @Override
  public void setPerformancePreferences(final int connectionTime, final int latency,
                                        final int bandwidth) {
    throw new UnsupportedOperationException("Unimplemented");
  }

  interface SocketOptionSetter {

    void run() throws SocketException;
  }
}
