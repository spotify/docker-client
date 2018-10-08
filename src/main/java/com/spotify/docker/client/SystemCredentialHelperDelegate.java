/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 - 2018 Spotify AB
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.spotify.docker.client.DockerCredentialHelper.CredentialHelperDelegate;
import com.spotify.docker.client.messages.DockerCredentialHelperAuth;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default credential helper delegate.
 * Executes each credential helper operation on the system.
 */
@SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC_ANON")
class SystemCredentialHelperDelegate implements CredentialHelperDelegate {

  private static final Logger log = LoggerFactory.getLogger(DockerConfigReader.class);
  private static final ObjectMapper mapper = ObjectMapperProvider.objectMapper();

  @Override
  public int store(final String credsStore, final DockerCredentialHelperAuth auth)
      throws IOException, InterruptedException {
    final Process process = exec("store", credsStore);

    try (final Writer outStreamWriter =
             new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
      try (final BufferedWriter writer = new BufferedWriter(outStreamWriter)) {
        writer.write(mapper.writeValueAsString(auth));
        writer.newLine();
        writer.flush();
      }
    }

    return process.waitFor();
  }

  @Override
  public int erase(final String credsStore, final String registry)
      throws IOException, InterruptedException {
    final Process process = exec("erase", credsStore);

    try (final Writer outStreamWriter =
             new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
      try (final BufferedWriter writer = new BufferedWriter(outStreamWriter)) {
        writer.write(registry);
        writer.newLine();
        writer.flush();
      }
    }

    return process.waitFor();
  }

  @Override
  public DockerCredentialHelperAuth get(final String credsStore, final String registry)
      throws IOException {
    final Process process = exec("get", credsStore);

    try (final Writer outStreamWriter =
             new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) {
      try (final BufferedWriter writer = new BufferedWriter(outStreamWriter)) {
        writer.write(registry);
        writer.newLine();
        writer.flush();
      }
    }

    try (final InputStreamReader reader =
             new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)) {
      try (BufferedReader input = new BufferedReader(reader)) {
        return readServerAuthDetails(input);
      }
    }
  }

  @Override
  public Map<String, String> list(final String credsStore) throws IOException {
    final Process process = exec("list", credsStore);

    try (final InputStreamReader reader =
             new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)) {
      try (BufferedReader input = new BufferedReader(reader)) {
        final String serverAuthDetails = input.readLine();
        if ("The specified item could not be found in the keychain.".equals(serverAuthDetails)) {
          return null;
        }
        return mapper.readValue(serverAuthDetails, new TypeReference<Map<String, String>>() {});
      }
    }
  }

  @VisibleForTesting
  static DockerCredentialHelperAuth readServerAuthDetails(final BufferedReader input)
      throws IOException {
    final String serverAuthDetails = input.lines().collect(Collectors.joining());

    // ErrCredentialsNotFound standardizes the not found error, so every helper returns
    // the same message and docker can handle it properly.
    // https://github.com/docker/docker-credential-helpers/blob/19b711cc92fbaa47533646fa8adb457d199c99e1/credentials/error.go#L4-L6
    if ("credentials not found in native keychain".equals(serverAuthDetails)) {
      return null;
    }
    return mapper.readValue(serverAuthDetails, DockerCredentialHelperAuth.class);
  }

  private Process exec(final String subcommand, final String credsStore) throws IOException {
    final String cmd = "docker-credential-" + credsStore + " " + subcommand;
    log.debug("Executing \"{}\"", cmd);
    return Runtime.getRuntime().exec(cmd);
  }
}
