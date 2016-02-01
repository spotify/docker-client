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

import org.junit.Test;

import java.net.URI;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.spotify.docker.client.DockerHost.DEFAULT_PORT;
import static com.spotify.docker.client.DockerHost.DEFAULT_HOST;
import static com.spotify.docker.client.DockerHost.DEFAULT_UNIX_ENDPOINT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DockerHostTest {

  @Test
  public void testFromUnixSocket() throws Exception {
    final String unixSocket = "unix:///var/run/docker.sock";
    final String certPath = "/path/to/cert";
    final URI unixSocketUri = new URI(unixSocket);

    final DockerHost dockerHost = DockerHost.from(unixSocket, certPath);
    assertThat(dockerHost.host(), equalTo(unixSocket));
    assertThat(dockerHost.uri(), equalTo(unixSocketUri));
    assertThat(dockerHost.bindURI(), equalTo(unixSocketUri));
    assertThat(dockerHost.port(), equalTo(0));
    assertThat(dockerHost.address(), equalTo("localhost"));
    assertThat(dockerHost.dockerCertPath(), equalTo(certPath));
  }

  @Test
  public void testFromTcpSocketNoCert() throws Exception {
    final String tcpSocket = "tcp://127.0.0.1:2375";
    final DockerHost dockerHost = DockerHost.from(tcpSocket, null);

    assertThat(dockerHost.host(), equalTo("127.0.0.1:2375"));
    assertThat(dockerHost.uri(), equalTo(new URI("http://127.0.0.1:2375")));
    assertThat(dockerHost.bindURI(), equalTo(new URI(tcpSocket)));
    assertThat(dockerHost.port(), equalTo(2375));
    assertThat(dockerHost.address(), equalTo("127.0.0.1"));
    assertThat(dockerHost.dockerCertPath(), equalTo(null));
  }

  @Test
  public void testFromTcpSocketWithCert() throws Exception {
    final String tcpSocket = "tcp://127.0.0.1:2375";
    final String certPath = "/path/to/cert";

    final DockerHost dockerHost = DockerHost.from(tcpSocket, certPath);
    assertThat(dockerHost.host(), equalTo("127.0.0.1:2375"));
    assertThat(dockerHost.uri(), equalTo(new URI("https://127.0.0.1:2375")));
    assertThat(dockerHost.bindURI(), equalTo(new URI(tcpSocket)));
    assertThat(dockerHost.port(), equalTo(2375));
    assertThat(dockerHost.address(), equalTo("127.0.0.1"));
    assertThat(dockerHost.dockerCertPath(), equalTo(certPath));
  }

  @Test
  public void testFromEnv() throws Exception {
    final String dockerHostEnvVar =
        fromNullable(System.getenv("DOCKER_HOST")).or(defaultEndpoint());
    final boolean isUnixSocket = dockerHostEnvVar.startsWith("unix://");
    final URI dockerHostUri = new URI(dockerHostEnvVar);
    final String dockerCertPathEnvVar = System.getenv("DOCKER_CERT_PATH");

    final String dockerHostAndPort;
    final URI dockerHostHttpUri;
    final URI dockerTcpUri;
    final int dockerHostPort;
    final String dockerHostHost;
    if (isUnixSocket) {
      dockerHostAndPort = dockerHostEnvVar;
      dockerHostHttpUri = dockerHostUri;
      dockerTcpUri = dockerHostUri;
      dockerHostPort = 0;
      dockerHostHost = "localhost";
    } else {
      dockerHostAndPort = dockerHostUri.getHost() + ":" + dockerHostUri.getPort();
      dockerHostHttpUri = isNullOrEmpty(dockerCertPathEnvVar) ?
                          new URI("http://" + dockerHostAndPort) :
                          new URI("https://" + dockerHostAndPort);
      dockerTcpUri = new URI("tcp://" + dockerHostAndPort);
      dockerHostPort = dockerHostUri.getPort();
      dockerHostHost = dockerHostUri.getHost();
    }

    final DockerHost dockerHost = DockerHost.fromEnv();
    assertThat(dockerHost.host(), equalTo(dockerHostAndPort));
    assertThat(dockerHost.uri(), equalTo(dockerHostHttpUri));
    assertThat(dockerHost.bindURI(), equalTo(dockerTcpUri));
    assertThat(dockerHost.port(), equalTo(dockerHostPort));
    assertThat(dockerHost.address(), equalTo(dockerHostHost));
    assertThat(dockerHost.dockerCertPath(), equalTo(dockerCertPathEnvVar));
  }

  private static String defaultEndpoint() {
    if (OSUtils.isLinux()) {
      return DEFAULT_UNIX_ENDPOINT;
    }
    return "http://" + DEFAULT_HOST + ":" + DEFAULT_PORT;
  }
}
