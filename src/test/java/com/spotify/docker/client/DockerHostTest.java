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

import com.spotify.docker.client.DockerHost.SystemDelegate;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DockerHostTest {

  private SystemDelegate systemDelegate;

  @Before
  public void before() {
    systemDelegate = mock(SystemDelegate.class);
  }

  @AfterClass
  public static void afterClass() {
    DockerHost.restoreSystemDelegate();
  }

  @Test
  public void testDefaultDockerEndpoint() throws Exception {
    when(systemDelegate.getProperty("os.name")).thenReturn("linux", "mac", "other");
    DockerHost.setSystemDelegate(systemDelegate);

    assertThat(DockerHost.defaultDockerEndpoint(), equalTo("unix:///var/run/docker.sock"));
    assertThat(DockerHost.defaultDockerEndpoint(), equalTo("unix:///var/run/docker.sock"));
    assertThat(DockerHost.defaultDockerEndpoint(), equalTo("localhost:2375"));
  }

  @Test
  public void testEndpointFromEnv() throws Exception {
    when(systemDelegate.getenv("DOCKER_HOST")).thenReturn("foo", (String) null);
    when(systemDelegate.getProperty("os.name")).thenReturn("linux");
    DockerHost.setSystemDelegate(systemDelegate);

    assertThat(DockerHost.endpointFromEnv(), equalTo("foo"));
    assertThat(DockerHost.endpointFromEnv(), equalTo("unix:///var/run/docker.sock"));
  }

  @Test
  public void testDefaultUnixEndpoint() throws Exception {
    assertThat(DockerHost.defaultUnixEndpoint(), equalTo("unix:///var/run/docker.sock"));
  }

  @Test
  public void testDefaultAddress() throws Exception {
    assertThat(DockerHost.defaultAddress(), equalTo("localhost"));
  }

  @Test
  public void testDefaultPort() throws Exception {
    assertThat(DockerHost.defaultPort(), equalTo(2375));
  }

  @Test
  public void testPortFromEnv() throws Exception {
    when(systemDelegate.getenv("DOCKER_PORT")).thenReturn("1234", (String) null);
    DockerHost.setSystemDelegate(systemDelegate);

    assertThat(DockerHost.portFromEnv(), equalTo(1234));
    assertThat(DockerHost.portFromEnv(), equalTo(2375));
  }

  @Test
  public void testDefaultCertPath() throws Exception {
    when(systemDelegate.getProperty("user.home")).thenReturn("foobar");
    DockerHost.setSystemDelegate(systemDelegate);

    assertThat(DockerHost.defaultCertPath(), equalTo("foobar/.docker"));
  }

  @Test
  public void testCertPathFromEnv() throws Exception {
    when(systemDelegate.getenv("DOCKER_CERT_PATH")).thenReturn("foo", (String) null);
    when(systemDelegate.getProperty("user.home")).thenReturn("bar");
    DockerHost.setSystemDelegate(systemDelegate);

    assertThat(DockerHost.certPathFromEnv(), equalTo("foo"));
    assertThat(DockerHost.certPathFromEnv(), nullValue());
  }

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
    assertThat(dockerHost.dockerCertPath(), nullValue());
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
    when(systemDelegate.getProperty("os.name")).thenReturn("linux");
    DockerHost.setSystemDelegate(systemDelegate);

    final String dockerHostEnvVar = DockerHost.defaultDockerEndpoint();
    final boolean isUnixSocket = dockerHostEnvVar.startsWith("unix://");
    final URI dockerHostUri = new URI(dockerHostEnvVar);

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
      dockerHostHttpUri = new URI("http://" + dockerHostAndPort);
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
    assertThat(dockerHost.dockerCertPath(), nullValue());
  }
}
