/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2017 Spotify AB
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

import static java.lang.System.getenv;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.base.Optional;
import com.google.common.io.Resources;
import com.spotify.docker.client.DockerCertificates.SslContextFactory;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DockerCertificatesTest {

  private static final boolean TRAVIS = "true".equals(getenv("TRAVIS"));

  private SslContextFactory factory = mock(SslContextFactory.class);
  private ArgumentCaptor<KeyStore> keyStore = ArgumentCaptor.forClass(KeyStore.class);
  private ArgumentCaptor<KeyStore> trustStore = ArgumentCaptor.forClass(KeyStore.class);
  private ArgumentCaptor<char[]> password = ArgumentCaptor.forClass(char[].class);

  @Test(expected = DockerCertificateException.class)
  public void testBadDockerCertificates() throws Exception {
    // try building a DockerCertificates with specifying a cert path to something that
    // isn't a cert
    DockerCertificates.builder()
        .dockerCertPath(getResourceFile("dockerInvalidSslDirectory"))
        .build();
  }

  @Test
  public void testNoDockerCertificatesInDir() throws Exception {
    final Path certDir = Paths.get(System.getProperty("java.io.tmpdir"));
    final Optional<DockerCertificatesStore> result = DockerCertificates.builder()
        .dockerCertPath(certDir)
        .build();
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void testDefaultDockerCertificates() throws Exception {
    DockerCertificates.builder()
        .dockerCertPath(getCertPath())
        .sslFactory(factory)
        .build();

    verify(factory).newSslContext(keyStore.capture(), password.capture(), trustStore.capture());

    final KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getValue()
        .getEntry("key", new KeyStore.PasswordProtection(password.getValue()));

    final KeyStore caKeyStore = trustStore.getValue();

    assertNotNull(pkEntry);
    assertNotNull(pkEntry.getCertificate());
    assertNotNull(caKeyStore.getCertificate("o=boot2docker"));
  }


  @Test
  public void testDockerCertificatesWithMultiCa() throws Exception {
    DockerCertificates.builder()
        .dockerCertPath(getCertPath())
        .caCertPath(getVariant("ca-multi.pem"))
        .sslFactory(factory)
        .build();

    verify(factory).newSslContext(keyStore.capture(), password.capture(), trustStore.capture());

    final KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getValue()
        .getEntry("key", new KeyStore.PasswordProtection(password.getValue()));

    assertNotNull(pkEntry);
    assertNotNull(pkEntry.getCertificate());
    assertNotNull(trustStore.getValue().getCertificate(
        "cn=ca-test,o=internet widgits pty ltd,st=some-state,c=cr"));
    assertNotNull(trustStore.getValue().getCertificate(
        "cn=ca-test-2,o=internet widgits pty ltd,st=some-state,c=cr"));
  }

  @Test
  public void testReadPrivateKeyPkcs1() throws Exception {
    DockerCertificates.builder()
        .dockerCertPath(getCertPath())
        .clientKeyPath(getVariant("key-pkcs1.pem"))
        .sslFactory(factory)
        .build();

    verify(factory).newSslContext(keyStore.capture(), password.capture(), trustStore.capture());

    final KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getValue()
        .getEntry("key", new KeyStore.PasswordProtection(password.getValue()));

    assertNotNull(pkEntry.getPrivateKey());
  }

  @Test
  public void testReadPrivateKeyPkcs8() throws Exception {
    DockerCertificates.builder()
        .dockerCertPath(getCertPath())
        .clientKeyPath(getVariant("key-pkcs8.pem"))
        .sslFactory(factory)
        .build();

    verify(factory).newSslContext(keyStore.capture(), password.capture(), trustStore.capture());

    final KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getValue()
        .getEntry("key", new KeyStore.PasswordProtection(password.getValue()));

    assertNotNull(pkEntry.getPrivateKey());
  }

  @Test
  public void testReadEllipticCurvePrivateKey() throws Exception {
    assumeFalse("Travis' openjdk7 doesn't support the elliptic curve algorithm", TRAVIS);

    DockerCertificates.builder()
        .dockerCertPath(getResourceFile("dockerSslDirectoryWithEcKey"))
        .sslFactory(factory)
        .build();

    verify(factory).newSslContext(keyStore.capture(), password.capture(), trustStore.capture());

    final KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) keyStore.getValue()
            .getEntry("key", new KeyStore.PasswordProtection(password.getValue()));

    assertNotNull(pkEntry.getPrivateKey());
  }

  private Path getResourceFile(final String path) throws URISyntaxException {
    return Paths.get(Resources.getResource(path).toURI());
  }

  private Path getCertPath() throws URISyntaxException {
    return getResourceFile("dockerSslDirectory");
  }

  private Path getVariant(final String filename) throws URISyntaxException {
    return getResourceFile("dockerSslVariants").resolve(filename);
  }
}
