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

package com.spotify.docker.test;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import com.spotify.docker.client.ContainerNotFoundException;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * The {@link DockerContainer} is a JUnit {@link MethodRule} which automatically creates, starts,
 * kills and removes Docker containers used in unit tests. The settings for the commands can be
 * provided using the {@link CreateContainer} annotation on the test method.
 * 
 * <p></p>
 * For example:
 * 
 * <pre>
 * {@code
 *   public class DockerAttachTest {
 * 
 *     private static DockerClient dockerClient;
 * 
 *     &#064;Rule
 *     public DockerContainer dockerContainer = new DockerContainer(dockerClient);
 * 
 *     &#064;BeforeClass
 *     public static void setUp() throws DockerCertificateException {
 *       dockerClient = DefaultDockerClient.fromEnv().readTimeoutMillis(120000).build();
 *     }
 * 
 *     &#064;Test
 *     &#064;CreateContainer(image = &quot;busybox&quot;, command = {&quot;sh&quot;, &quot;-c&quot;, &quot;echo \&quot;test\&quot;&quot;}, start = true)
 *     public void testIt() throws IOException, DockerException, InterruptedException {
 * 
 *       String containerId = dockerContainer.getContainerId();
 *       dockerClient.waitContainer(containerId);
 *       LogStream logStream = dockerClient.logs(containerId, LogsParameter.STDOUT);
 *       assertThat(logStream.readFully(), equalTo(&quot;test\n&quot;));
 *     }
 * 
 *     &#064;AfterClass
 *     public static void cleanUp() {
 *       dockerClient.close();
 *     }
 * 
 *   }
 * }
 * </pre>
 * 
 * @author Jan-Willem Gmelig Meyling
 *
 */
public class DockerContainer implements MethodRule {

  private static final Logger log = LoggerFactory.getLogger(DockerContainer.class);
  
  private final DockerClient dockerClient;
  
  private String containerId;
  
  public DockerContainer(final DockerClient dockerClient) {
    Preconditions.checkNotNull(dockerClient);
    this.dockerClient = dockerClient;
  }
  
  @Override
  public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
    final Method javaMethod = method.getMethod();
    final CreateContainer createContainer = javaMethod.getAnnotation(CreateContainer.class);

    if (createContainer == null) {
      return base;
    }

    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        try {
          ContainerCreation containerCreation = createContainer(createContainer);
          containerId = containerCreation.id();
          if (createContainer.start()) {
            startContainer(createContainer);
          }
          base.evaluate();
        } finally {
          cleanup();
        }
      }
    };
  }
  
  /**
   * Create a new Docker container based on the {@link CreateContainer} annotation
   * @param createContainer CreateContainer annotation
   * @return {@link ContainerCreation} response
   * @throws DockerException
   * @throws InterruptedException
   */
  protected ContainerCreation createContainer(final CreateContainer createContainer)
      throws DockerException, InterruptedException {
    Preconditions.checkNotNull(createContainer);

    final com.spotify.docker.test.PortBinding[] portBindings =
        createContainer.portBindings();

    final HostConfig.Builder hostConfigBuilder = HostConfig.builder();
    final ImmutableMap.Builder<String, List<PortBinding>> ports = ImmutableMap.builder();
    for (com.spotify.docker.test.PortBinding portBinding : portBindings) {
      ports.put(String.valueOf(portBinding.containerPort()) + "/" + portBinding.protocol(),
                Collections.singletonList(PortBinding.of(null, portBinding.hostPort())));
    }
    final HostConfig hostConfig = hostConfigBuilder
        .portBindings(ports.build())
        .binds(createContainer.binds())
        .build();

    final ContainerConfig.Builder configBuilder = ContainerConfig.builder()
        .image(createContainer.image())
        .env(createContainer.env())
        .volumes(createContainer.volumes())
        .hostConfig(hostConfig)
        .cmd(createContainer.command());
    final ContainerConfig createContainerConfig = configBuilder.build();

    log.debug("Creating Docker container using config {}", createContainerConfig);
    final ContainerCreation creation = dockerClient.createContainer(
        createContainerConfig, createContainer.name());
    containerId = creation.id();
    log.debug("Created Docker container {}", containerId);
    return creation;
  }

  /**
   * Start a created container
   * @param createContainer CreateContainer annotation
   * @throws DockerException
   * @throws InterruptedException
   */
  protected void startContainer(CreateContainer createContainer)
      throws DockerException, InterruptedException {
    Preconditions.checkNotNull(createContainer);
    Preconditions.checkNotNull(containerId);
    
    log.debug("Starting Docker container {}", containerId);
    dockerClient.startContainer(containerId);
    log.debug("Started Docker container {}", containerId);
  }

  /**
   * Clean up created container
   * @throws InterruptedException
   * @throws DockerException
   */
  protected void cleanup() throws InterruptedException, DockerException {
    if (containerId == null) {
      // The container was never created
      return;
    }
    
    InterruptedException interuptedException = null;
    DockerException dockerException = null;
    
    try {
      while (dockerClient.inspectContainer(containerId).state().running()) {
        log.debug("Killing Docker container {}", containerId);
        dockerClient.killContainer(containerId);
        Thread.sleep(50);
      }
      log.debug("Killed Docker container {}", containerId);
    } catch (ContainerNotFoundException e) {
      // Already shutdown
      log.warn(e.getMessage(), e);
    } catch (DockerException e) {
      dockerException = e;
    } catch (InterruptedException e) {
      interuptedException = e;
    }
    
    try {
      log.debug("Removing Docker container {}", containerId);
      dockerClient.removeContainer(containerId);
      log.debug("Removed Docker container {}", containerId);
    } catch (ContainerNotFoundException e) {
      // Already removed
      log.warn(e.getMessage(), e);
    } catch (DockerException e) {
      dockerException = e;
    } catch (InterruptedException e) {
      interuptedException = e;
    }
    
    // Fail the test if clean up failed
    if (interuptedException != null) {
      throw interuptedException;
    } else if (dockerException != null) {
      throw dockerException;
    }
  }
  
  @SuppressWarnings("unused")
  public DockerClient getDockerClient() {
    return dockerClient;
  }

  public String getContainerId() {
    return containerId;
  }

}
