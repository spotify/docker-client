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

import com.spotify.docker.client.exceptions.DockerCertificateException;

import com.google.common.base.Optional;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContexts;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * DockerCertificates holds certificates for connecting to an HTTPS-secured Docker instance with
 * client/server authentication.
 */
public class DockerCertificates {

  public static final String DEFAULT_CA_CERT_NAME = "ca.pem";
  public static final String DEFAULT_CLIENT_CERT_NAME = "cert.pem";
  public static final String DEFAULT_CLIENT_KEY_NAME = "key.pem";

  private static final char[] KEY_STORE_PASSWORD = "docker!!11!!one!".toCharArray();
  private static final Logger log = LoggerFactory.getLogger(DockerCertificates.class);

  private final SSLContext sslContext;

  public DockerCertificates(final Path dockerCertPath) throws DockerCertificateException {
    this(new Builder().dockerCertPath(dockerCertPath));
  }

  private DockerCertificates(final Builder builder) throws DockerCertificateException {
    if ((builder.caCertPath == null) || (builder.clientCertPath == null) ||
        (builder.clientKeyPath == null)) {
      throw new DockerCertificateException(
          "caCertPath, clientCertPath, and clientKeyPath must all be specified");
    }

    try (InputStream caCertStream =
             Files.newInputStream(builder.caCertPath);
         InputStream clientCertStream =
             Files.newInputStream(builder.clientCertPath);
         BufferedReader clientKeyStream =
             Files.newBufferedReader(builder.clientKeyPath, Charset.defaultCharset());
         PEMParser pemParser = new PEMParser(clientKeyStream);
    ) {
      final CertificateFactory cf = CertificateFactory.getInstance("X.509");
      final Certificate caCert = cf.generateCertificate(caCertStream);
      final Certificate clientCert = cf.generateCertificate(clientCertStream);

      final PEMKeyPair clientKeyPair = (PEMKeyPair) pemParser.readObject();

      final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(
          clientKeyPair.getPrivateKeyInfo().getEncoded());
      final KeyFactory kf = KeyFactory.getInstance("RSA");
      final PrivateKey clientKey = kf.generatePrivate(spec);

      final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      trustStore.load(null, null);
      trustStore.setEntry("ca", new KeyStore.TrustedCertificateEntry(caCert), null);

      final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null, null);
      keyStore.setCertificateEntry("client", clientCert);
      keyStore.setKeyEntry("key", clientKey, KEY_STORE_PASSWORD, new Certificate[] {clientCert});

      this.sslContext = SSLContexts.custom()
          .loadTrustMaterial(trustStore, null)
          .loadKeyMaterial(keyStore, KEY_STORE_PASSWORD)
          .build();
    } catch (
        CertificateException |
            IOException |
            NoSuchAlgorithmException |
            InvalidKeySpecException |
            KeyStoreException |
            UnrecoverableKeyException |
            KeyManagementException e) {
      throw new DockerCertificateException(e);
    }
  }

  public SSLContext sslContext() {
    return this.sslContext;
  }

  public HostnameVerifier hostnameVerifier() {
    return NoopHostnameVerifier.INSTANCE;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Path caCertPath;
    private Path clientKeyPath;
    private Path clientCertPath;

    public Builder dockerCertPath(final Path dockerCertPath) {
      this.caCertPath = dockerCertPath.resolve(DEFAULT_CA_CERT_NAME);
      this.clientKeyPath = dockerCertPath.resolve(DEFAULT_CLIENT_KEY_NAME);
      this.clientCertPath = dockerCertPath.resolve(DEFAULT_CLIENT_CERT_NAME);

      return this;
    }

    public Builder caCertPath(final Path caCertPath) {
      this.caCertPath = caCertPath;
      return this;
    }

    public Builder clientKeyPath(final Path clientKeyPath) {
      this.clientKeyPath = clientKeyPath;
      return this;
    }

    public Builder clientCertPath(final Path clientCertPath) {
      this.clientCertPath = clientCertPath;
      return this;
    }

    public Optional<DockerCertificates> build() throws DockerCertificateException {
      if (this.caCertPath == null || this.clientKeyPath == null || this.clientCertPath == null) {
        log.debug("caCertPath, clientKeyPath or clientCertPath not specified, not using SSL");
        return Optional.absent();
      } else if (Files.exists(this.caCertPath) && Files.exists(this.clientKeyPath) &&
                 Files.exists(this.clientCertPath)) {
        return Optional.of(new DockerCertificates(this));
      } else {
        log.debug("{}, {} or {} does not exist, not using SSL", this.caCertPath, this.clientKeyPath,
                  this.clientCertPath);
        return Optional.absent();
      }
    }
  }
}
