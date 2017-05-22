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

import com.google.common.base.Optional;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
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
public class DockerStringCertificates implements DockerCertificatesStore {

  private static final char[] KEY_STORE_PASSWORD = "docker!!11!!one!".toCharArray();
  private static final Logger log = LoggerFactory.getLogger(DockerCertificates.class);

  private final SSLContext sslContext;

  public DockerStringCertificates(final String caContent, final String clientCertContent, 
      final String clientKeyContent) 
      throws DockerCertificateException {
    this(new Builder()
        .caCertContent(caContent)
        .clientCertContent(clientCertContent)
        .clientKeyContent(clientKeyContent));
  }

  private DockerStringCertificates(final Builder builder) throws DockerCertificateException {
    if (((builder.caCertContent == null) || (builder.clientCertContent == null)
            || (builder.clientKeyContent == null))) {
      throw new DockerCertificateException(
          "caCertContent, clientCertContent, and clientKeyContent must all be specified");
    }
    
    try {
      final PrivateKey clientKey = readPrivateKey(builder.clientKeyContent);
      final List<Certificate> clientCerts = readCertificates(builder.clientCertContent);
      final List<Certificate> caCerts = readCertificates(builder.caCertContent);
      
      final KeyStore keyStore = newKeyStore();
      keyStore.setKeyEntry("key", clientKey, KEY_STORE_PASSWORD,
              clientCerts.toArray(new Certificate[clientCerts.size()]));

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
  
  private PrivateKey readPrivateKey(String content) throws IOException, InvalidKeySpecException,
      NoSuchAlgorithmException, DockerCertificateException {
    try (Reader reader = new StringReader(content)) {
      return readPrivateKey(reader);
    }
  }

  private PrivateKey readPrivateKey(Reader reader) throws IOException, InvalidKeySpecException,
      NoSuchAlgorithmException, DockerCertificateException {
    try (PEMParser pemParser = new PEMParser(reader)) {

      final Object readObject = pemParser.readObject();

      if (readObject instanceof PEMKeyPair) {
        PEMKeyPair clientKeyPair = (PEMKeyPair) readObject;
        return generatePrivateKey(clientKeyPair.getPrivateKeyInfo());
      } else if (readObject instanceof PrivateKeyInfo) {
        return generatePrivateKey((PrivateKeyInfo) readObject);
      }

      throw new DockerCertificateException("Can not generate private key reader");
    }
  }

  private static PrivateKey generatePrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException,
          InvalidKeySpecException, NoSuchAlgorithmException {
    final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded());
    final KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(spec);
  }
  
  private List<Certificate> readCertificates(String content) 
      throws CertificateException, IOException {
    try (InputStream inputStream = new ByteArrayInputStream(content.getBytes())) {
      return readCertificates(inputStream);
    }
  }
  
  private List<Certificate> readCertificates(InputStream inputStream) 
      throws CertificateException, IOException {
    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
    return new ArrayList<>(cf.generateCertificates(inputStream));
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
    private String caCertContent;
    private String clientKeyContent;
    private String clientCertContent;
    
    public Builder caCertContent(final String caCertContent) {
      this.caCertContent = caCertContent;
      return this;
    }

    public Builder clientKeyContent(final String clientKeyContent) {
      this.clientKeyContent = clientKeyContent;
      return this;
    }

    public Builder clientCertContent(final String clientCertContent) {
      this.clientCertContent = clientCertContent;
      return this;
    }

    public Builder sslFactory(final SslContextFactory sslContextFactory) {
      this.sslContextFactory = sslContextFactory;
      return this;
    }

    public Optional<DockerCertificatesStore> build() throws DockerCertificateException {
      if (this.caCertContent != null && this.clientKeyContent != null
          && this.clientCertContent != null) {
        return Optional.of((DockerCertificatesStore) new DockerStringCertificates(this));
      }
      
      log.debug("{}, {} or {} are nulls, not using SSL", this.caCertContent, this.clientKeyContent,
                  this.clientCertContent);
      return Optional.absent();
    }
  }
}
