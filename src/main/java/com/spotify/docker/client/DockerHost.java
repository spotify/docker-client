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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.HostAndPort;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Locale;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents a dockerd endpoint. A codified DOCKER_HOST.
 */
public class DockerHost {

  /**
   * An interface to be mocked during testing.
   */
  @VisibleForTesting
  interface SystemDelegate {

    String getProperty(String key);

    String getenv(String name);
  }

  private static final SystemDelegate defaultSystemDelegate = new SystemDelegate() {
    @Override
    public String getProperty(final String key) {
      return System.getProperty(key);
    }

    @Override
    public String getenv(final String name) {
      return System.getenv(name);
    }
  };
  private static SystemDelegate systemDelegate = defaultSystemDelegate;

  private static final String DEFAULT_UNIX_ENDPOINT = "unix:///var/run/docker.sock";
  private static final String DEFAULT_ADDRESS = "localhost";
  private static final int DEFAULT_PORT = 2375;

  private final String host;
  private final URI uri;
  private final URI bindURI;
  private final String address;
  private final int port;
  private final String certPath;

  private DockerHost(final String endpoint, final String certPath) {
    if (endpoint.startsWith("unix://")) {
      this.port = 0;
      this.address = DEFAULT_ADDRESS;
      this.host = endpoint;
      this.uri = URI.create(endpoint);
      this.bindURI = URI.create(endpoint);
    } else {
      final String stripped = endpoint.replaceAll(".*://", "");
      final HostAndPort hostAndPort = HostAndPort.fromString(stripped);
      final String hostText = hostAndPort.getHostText();
      final String scheme = isNullOrEmpty(certPath) ? "http" : "https";

      this.port = hostAndPort.getPortOrDefault(defaultPort());
      this.address = isNullOrEmpty(hostText) ? DEFAULT_ADDRESS : hostText;
      this.host = address + ":" + port;
      this.uri = URI.create(scheme + "://" + address + ":" + port);
      this.bindURI = URI.create("tcp://" + address + ":" + port);
    }

    this.certPath = certPath;
  }

  /**
   * Get a Docker endpoint usable for instantiating a new DockerHost with
   * DockerHost.from(endpoint).
   *
   * @return A unix socket path or, in the case of a TCP socket, the hostname and port which
   * represents a Docker endpoint.
   */
  public String host() {
    return host;
  }

  /**
   * Get the Docker rest uri.
   *
   * @return The uri of the Docker endpoint.
   */
  public URI uri() {
    return uri;
  }

  /**
   * Get the Docker rest bind uri.
   *
   * @return The uri of the host for binding ports (or setting $DOCKER_HOST).
   */
  public URI bindURI() {
    return bindURI;
  }

  /**
   * Get the Docker endpoint port.
   *
   * @return The port.
   */
  public int port() {
    return port;
  }

  /**
   * Get the Docker ip address or hostname.
   *
   * @return The ip address or hostname.
   */
  public String address() {
    return address;
  }

  /**
   * Get the path to certificate and key for connecting to Docker via HTTPS.
   *
   * @return The path to the certificate.
   */
  public String dockerCertPath() {
    return certPath;
  }

  @VisibleForTesting
  static void setSystemDelegate(final SystemDelegate delegate) {
    systemDelegate = delegate;
  }

  @VisibleForTesting
  static void restoreSystemDelegate() {
    systemDelegate = defaultSystemDelegate;
  }

  /**
   * Create a {@link DockerHost} from DOCKER_HOST and DOCKER_PORT env vars.
   *
   * @return The DockerHost object.
   */
  public static DockerHost fromEnv() {
    final String host = endpointFromEnv();
    final String certPath = certPathFromEnv();
    return new DockerHost(host, certPath);
  }

  /**
   * Create a {@link DockerHost} from an explicit address or uri.
   *
   * @param endpoint The Docker endpoint.
   * @param certPath The certificate path.
   * @return The DockerHost object.
   */
  public static DockerHost from(final String endpoint, final String certPath) {
    return new DockerHost(endpoint, certPath);
  }

  static String defaultDockerEndpoint() {
    final String osName = systemDelegate.getProperty("os.name");
    final String os = osName.toLowerCase(Locale.ENGLISH);
    if (os.equalsIgnoreCase("linux") || os.contains("mac")) {
      return DEFAULT_UNIX_ENDPOINT;
    } else {
      return DEFAULT_ADDRESS + ":" + defaultPort();
    }
  }

  static String endpointFromEnv() {
    return firstNonNull(systemDelegate.getenv("DOCKER_HOST"), defaultDockerEndpoint());
  }

  public static String defaultUnixEndpoint() {
    return DEFAULT_UNIX_ENDPOINT;
  }

  public static String defaultAddress() {
    return DEFAULT_ADDRESS;
  }

  public static int defaultPort() {
    return DEFAULT_PORT;
  }

  static int portFromEnv() {
    final String port = systemDelegate.getenv("DOCKER_PORT");
    if (port == null) {
      return defaultPort();
    }
    try {
      return Integer.parseInt(port);
    } catch (NumberFormatException e) {
      return defaultPort();
    }
  }

  static String defaultCertPath() {
    final String userHome = systemDelegate.getProperty("user.home");
    return Paths.get(userHome, ".docker").toString();
  }

  static String certPathFromEnv() {
    return systemDelegate.getenv("DOCKER_CERT_PATH");
  }

  @Override
  public String toString() {
    return "DockerHost{" +
           "host='" + host + '\'' +
           ", uri=" + uri +
           ", bindURI=" + bindURI +
           ", address='" + address + '\'' +
           ", port=" + port +
           ", certPath='" + certPath + '\'' +
           '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final DockerHost that = (DockerHost) o;

    if (port != that.port) {
      return false;
    }
    if (host != null ? !host.equals(that.host) : that.host != null) {
      return false;
    }
    if (uri != null ? !uri.equals(that.uri) : that.uri != null) {
      return false;
    }
    if (bindURI != null ? !bindURI.equals(that.bindURI) : that.bindURI != null) {
      return false;
    }
    if (address != null ? !address.equals(that.address) : that.address != null) {
      return false;
    }
    return certPath != null ? certPath.equals(that.certPath) : that.certPath == null;

  }

  @Override
  public int hashCode() {
    int result = host != null ? host.hashCode() : 0;
    result = 31 * result + (uri != null ? uri.hashCode() : 0);
    result = 31 * result + (bindURI != null ? bindURI.hashCode() : 0);
    result = 31 * result + (address != null ? address.hashCode() : 0);
    result = 31 * result + port;
    result = 31 * result + (certPath != null ? certPath.hashCode() : 0);
    return result;
  }
}
