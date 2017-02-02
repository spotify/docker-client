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


package com.spotify.docker.it;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Preconditions;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.test.DockerContainer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Docker container.
 * Stripped down version for the {@link LogReaderHangTest}.
 *
 * @author Ivan Krizsan
 */
public class IvanDockerContainer {
  private static final Logger LOGGER = LoggerFactory.getLogger(DockerContainer.class);

  /* Instance variable(s): */
  protected DockerClient dockerClient;
  protected String dockerImageName;
  protected String dockerContainerName;
  protected String dockerContainerId;
  protected LogStream containerLogStream;

  public IvanDockerContainer(final DockerClient inDockerClient, final String inDockerImageName) {
    Preconditions.checkNotNull(inDockerClient);
    Preconditions.checkArgument(StringUtils.isNotEmpty(inDockerImageName));

    dockerClient = inDockerClient;
    dockerImageName = inDockerImageName;
  }

  public IvanDockerContainer create() throws DockerException,
      InterruptedException {
    Preconditions.checkNotNull(dockerClient);
    Preconditions.checkArgument(StringUtils.isNotEmpty(dockerImageName));

    final HostConfig.Builder theHostConfigBuilder = HostConfig.builder();
    final HostConfig theHostConfig =
        theHostConfigBuilder.portBindings(new HashMap<String, List<PortBinding>>()).build();

    final ContainerConfig theContainerConfig =
        ContainerConfig.builder()
            .hostConfig(theHostConfig)
            .image(dockerImageName)
            .exposedPorts(new HashSet<String>())
            .env(new ArrayList<String>())
            .cmd(new ArrayList<String>())
            .entrypoint(new ArrayList<String>())
            .build();

    if (StringUtils.isEmpty(dockerContainerName)) {
      dockerContainerName = UUID.randomUUID().toString();
    }

    final ContainerCreation theContainerCreation =
        dockerClient.createContainer(
            theContainerConfig, dockerContainerName);
    dockerContainerId = theContainerCreation.id();

    return this;
  }

  public IvanDockerContainer start() throws DockerException, InterruptedException {
    if (StringUtils.isEmpty(dockerContainerId)) {
      throw new IllegalStateException(
          "A Docker container must be created before it can be started");
    }
    dockerClient.startContainer(dockerContainerId);
    LOGGER.debug("Started Docker container with id {}", dockerContainerId);

    return this;
  }

  public IvanDockerContainer stopIgnoreExceptions() {
    try {
      dockerClient.killContainer(dockerContainerId);
    } catch (final Exception theException) {
      LOGGER.error("An error occurred trying to stop Docker container"
          + " with id: " + dockerContainerId, theException);
    }

    return this;
  }

  public IvanDockerContainer remove()
      throws DockerException, InterruptedException {
    dockerClient.removeContainer(dockerContainerId);
    return this;
  }

  public IvanDockerContainer attachToLogStreams()
      throws DockerException, InterruptedException {
    containerLogStream = dockerClient.logs(
        dockerContainerId,
        DockerClient.LogsParam.stdout(),
        DockerClient.LogsParam.stderr(),
        DockerClient.LogsParam.timestamps(),
        DockerClient.LogsParam.follow());

    containerLogStream.setReadTimeoutInMilliseconds(1000L);

    return this;
  }

  public IvanDockerContainer waitForLog(final String inStringToMatch,
                                        final Duration inTimeout) throws InterruptedException {
    if (containerLogStream == null) {
      throw new IllegalStateException(
          "Not attached to Docker container's log streams");
    }

    boolean theLogStringMatched = false;
    final DateTime theStartTime = new DateTime();

    LOGGER.debug("Waiting for log string '{}' from container with id {}...",
        inStringToMatch, dockerContainerId);

    String theContainerLogString = "";

    do {
      final DateTime theCurrentTime = new DateTime();
      final Duration theCurrentWaitDuration =
          new Interval(theStartTime, theCurrentTime).toDuration();
      if (theCurrentWaitDuration.isLongerThan(inTimeout)) {
        LOGGER.debug(
            "Matching string '{}' in log from container with id {} timed out",
            inStringToMatch,
            dockerContainerId);
        throw new InterruptedException(
            "Timeout waiting for log from Docker container with id "
                + dockerContainerId);
      }

      if (containerLogStream.hasNext()) {
        final LogMessage theContainerLogMessage =
            containerLogStream.next();
        theContainerLogString =
            String.valueOf(UTF_8.decode(theContainerLogMessage.content()));
        LOGGER.debug("Log string from container {}: {}", dockerContainerId,
            theContainerLogString);

        theLogStringMatched =
            theContainerLogString.contains(inStringToMatch);
      }
    }
    while (!theLogStringMatched);

    LOGGER.debug("Found string '{}' in Docker container log '{}'",
        inStringToMatch, theContainerLogString);

    return this;
  }

  public IvanDockerContainer setDockerContainerName(final String inDockerContainerName) {
    dockerContainerName = inDockerContainerName;
    return this;
  }
}
