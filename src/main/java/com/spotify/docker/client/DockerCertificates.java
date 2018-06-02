/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.spotify.docker.client.exceptions.DockerCertificateException;

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
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContexts;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DockerCertificates holds certificates for connecting to an HTTPS-secured Docker instance with
 * client/server authentication.
 */
public class DockerCertificates implements DockerCertificatesStore {

  public static final String DEFAULT_CA_CERT_NAME = "ca.pem";
  public static final String DEFAULT_CLIENT_CERT_NAME = "cert.pem";
  public static final String DEFAULT_CLIENT_KEY_NAME = "key.pem";

  private static final char[] KEY_STORE_PASSWORD = "docker!!11!!one!".toCharArray();
  private static final Set<String> PRIVATE_KEY_ALGS = ImmutableSet.of("RSA", "EC");
  private static final Logger log = LoggerFactory.getLogger(DockerCertificates.class);

  private final SSLContext sslContext;

  public DockerCertificates(final Path dockerCertPath) throws DockerCertificateException {
    this(new Builder().dockerCertPath(dockerCertPath));
  }

  private DockerCertificates(final Builder builder) throws DockerCertificateException {
    if ((builder.caCertPath == null) || (builder.clientCertPath == null)
        || (builder.clientKeyPath == null)) {
      throw new DockerCertificateException(
          "caCertPath, clientCertPath, and clientKeyPath must all be specified");
    }

    try {

      final PrivateKey clientKey = readPrivateKey(builder.clientKeyPath);
      final List<Certificate> clientCerts = readCertificates(builder.clientCertPath);

      final KeyStore keyStore = newKeyStore();
      keyStore.setKeyEntry("key", clientKey, KEY_STORE_PASSWORD,
              clientCerts.toArray(new Certificate[clientCerts.size()]));

      final List<Certificate> caCerts = readCertificates(builder.caCertPath);

      final KeyStore trustStore = newKeyStore();
      for (Certificate caCert : caCerts) {
        X509Certificate crt = (X509Certificate) caCert;
        String alias = crt.getSubjectX500Principal()
                .getName();
        trustStore.setCertificateEntry(alias, caCert);
      }

      this.sslContext = builder.sslContextFactory
          .newSslContext(keyStore, KEY_STORE_PASSWORD, trustStore);
    } catch (DockerCertificateException e) {
      throw e;
    } catch (CertificateException
        | IOException
        | NoSuchAlgorithmException
        | InvalidKeySpecException
        | KeyStoreException
        | UnrecoverableKeyException
        | KeyManagementException e) {
      throw new DockerCertificateException(e);
    }
  }

  private KeyStore newKeyStore() throws CertificateException, NoSuchAlgorithmException,
      IOException, KeyStoreException {
    final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(null);
    return keyStore;
  }

  private PrivateKey readPrivateKey(final Path file)
      throws IOException, InvalidKeySpecException, DockerCertificateException {
    try (final BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset());
         final PEMParser pemParser = new PEMParser(reader)) {

      final Object readObject = pemParser.readObject();

      if (readObject instanceof PEMKeyPair) {
        final PEMKeyPair clientKeyPair = (PEMKeyPair) readObject;
        return generatePrivateKey(clientKeyPair.getPrivateKeyInfo());
      } else if (readObject instanceof PrivateKeyInfo) {
        return generatePrivateKey((PrivateKeyInfo) readObject);
      }

      throw new DockerCertificateException("Can not generate private key from file: "
          + file.toString());
    }
  }

  private static PrivateKey generatePrivateKey(final PrivateKeyInfo privateKeyInfo)
      throws IOException, InvalidKeySpecException {
    final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded());
    return tryGeneratePrivateKey(spec, PRIVATE_KEY_ALGS);
  }

  private static PrivateKey tryGeneratePrivateKey(final PKCS8EncodedKeySpec spec,
                                                  final Set<String> algorithms)
          throws InvalidKeySpecException {

    KeyFactory kf;
    PrivateKey key;
    for (final String algorithm : algorithms) {
      try {
        kf = KeyFactory.getInstance(algorithm);
        key = kf.generatePrivate(spec);
        log.debug("Generated private key from spec using the '{}' algorithm", algorithm);
        return key;
      } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
        log.debug("Tried generating private key from spec using the '{}' algorithm", algorithm, e);
      }
    }

    final String error = String.format("Could not generate private key from spec. Tried using %s",
        Joiner.on(", ").join(algorithms));
    throw new InvalidKeySpecException(error);
  }

  private List<Certificate> readCertificates(Path file) throws CertificateException, IOException {
    try (InputStream inputStream = Files.newInputStream(file)) {
      final CertificateFactory cf = CertificateFactory.getInstance("X.509");
      return new ArrayList<>(cf.generateCertificates(inputStream));
    }
  }

  public SSLContext sslContext() {
    return this.sslContext;
  }

  public HostnameVerifier hostnameVerifier() {
    return NoopHostnameVerifier.INSTANCE;
  }

  public interface SslContextFactory {
    SSLContext newSslContext(KeyStore keyStore, char[] keyPassword, KeyStore trustStore)
        throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException,
        KeyManagementException;
  }

  private static class DefaultSslContextFactory implements SslContextFactory {
    @Override
    public SSLContext newSslContext(KeyStore keyStore, char[] keyPassword, KeyStore trustStore)
        throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException,
        KeyManagementException {
      return SSLContexts.custom()
              .loadTrustMaterial(trustStore, null)
              .loadKeyMaterial(keyStore, keyPassword)
              .build();
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private SslContextFactory sslContextFactory = new DefaultSslContextFactory();
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

    public Builder sslFactory(final SslContextFactory sslContextFactory) {
      this.sslContextFactory = sslContextFactory;
      return this;
    }

    public Optional<DockerCertificatesStore> build() throws DockerCertificateException {
      if (this.caCertPath == null || this.clientKeyPath == null || this.clientCertPath == null) {
        log.debug("caCertPath, clientKeyPath or clientCertPath not specified, not using SSL");
        return Optional.absent();
      } else if (Files.exists(this.caCertPath) && Files.exists(this.clientKeyPath)
                 && Files.exists(this.clientCertPath)) {
        return Optional.of((DockerCertificatesStore) new DockerCertificates(this));
      } else {
        log.debug("{}, {} or {} does not exist, not using SSL", this.caCertPath, this.clientKeyPath,
                  this.clientCertPath);
        return Optional.absent();
      }
    }
  }
}
