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

import static java.lang.Long.toHexString;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks thread safety of DefaultDockerClient implementation.
 *
 * @author Umberto Nicoletti (umberto.nicoletti@gmail.com)
 */
public class ConnectionPoolTest {

  private static final String BUSYBOX = "busybox";
  private static final String BUSYBOX_LATEST = BUSYBOX + ":latest";
  private static final String BUSYBOX_BUILDROOT_2013_08_1 = BUSYBOX + ":buildroot-2013.08.1";

  private static final Logger log = LoggerFactory.getLogger(ConnectionPoolTest.class);

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Rule
  public final TestName testName = new TestName();

  private final String nameTag = toHexString(ThreadLocalRandom.current().nextLong());

  /**
   * Checks that running a parallel operation does not break DefaultDockerClient.
   * Fixes issue #446.
   *
   * @throws Exception on error.
   */
  @Test
  public void testParallelOperation() throws Exception {
    final ExecutorService executor = Executors.newFixedThreadPool(5);
    List<Future<Exception>> tasks = new ArrayList<>(20);
    for (int i = 0; i < 20; i++) {
      tasks.add(executor.submit(
              new Callable<Exception>() {
                @Override
                public Exception call() throws Exception {
                  try (DockerClient docker = DefaultDockerClient.fromEnv().build()) {
                    docker.pull(ConnectionPoolTest.BUSYBOX_LATEST);
                    docker.pull(ConnectionPoolTest.BUSYBOX_BUILDROOT_2013_08_1);
                  } catch (InterruptedException | DockerException | DockerCertificateException e) {
                    ConnectionPoolTest.log.error(
                            "Error running task: {}", e.getMessage(), e
                    );
                    return e;
                  }
                  return null;
                }
              }
              )
      );
    }
    executor.shutdown();
    executor.awaitTermination(30, TimeUnit.SECONDS);
    for (final Future<Exception> task : tasks) {
      MatcherAssert.assertThat(task.get(), Matchers.nullValue());
    }
  }
}
