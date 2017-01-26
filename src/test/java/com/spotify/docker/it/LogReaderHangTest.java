package com.spotify.docker.it;


import com.google.common.io.Resources;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for issue 552: Obtaining next log message from Docker container hangs
 * indefinitely.
 *
 * @author Ivan Krizsan
 */
public class LogReaderHangTest {
  /* Constant(s): */
  public static final String LOGHANG_DOCKERIMAGE = "loghang:latest";

  /* Class variable(s): */
  protected static DockerClient dockerClient;

  /* Instance variable(s): */
  protected IvanDockerContainer ivanDockerContainer;

  /**
   * Performs setup before all the test methods.
   *
   * @throws DockerCertificateException If creation of Docker client failed.
   */
  @BeforeClass
  public static void setUpOnceBeforeAllTests() throws DockerCertificateException {
    dockerClient = DefaultDockerClient.fromEnv().readTimeoutMillis(10000).build();
  }

  /**
   * Performs setup before one test method.
   *
   * @throws Exception If error occurred creating test Docker image.
   */
  @Before
  public void setupBeforeOneTest() throws Exception {
    createDockerLogReaderHangTestImage();

    ivanDockerContainer =
        new IvanDockerContainer(dockerClient, LOGHANG_DOCKERIMAGE);
  }

  /**
   * Cleans up after one test method.
   */
  @After
  public void cleanupAfterOneTest() {
    ivanDockerContainer.stopIgnoreExceptions();
    try {
      ivanDockerContainer.remove();
    } catch (final Exception theException) {
      /* Ignore exceptions. */
    }
    try {
      dockerClient.removeImage(LOGHANG_DOCKERIMAGE);
    } catch (final Exception theException) {
      /* Ignore exceptions. */
    }
  }

  /**
   * Tests issue 552 obtaining next log message from Docker container hangs
   * indefinitely.
   * Note that if this test fails due to timeout, the Docker container needs
   * to be stopped and removed manually.
   * Expected result: An exception should be thrown when waiting for a certain
   * string in container log output times out.
   *
   * @throws DockerException      If error occurs running Docker container.
   * @throws InterruptedException Expected exception.
   */
  @Test(timeout = 50000L, expected = InterruptedException.class)
  public void testContainerLogHang()
      throws DockerException, InterruptedException {

    try {
      ivanDockerContainer
          .setDockerContainerName("LoghangTestContainer")
          .create()
          .start()
          .attachToLogStreams()
          .waitForLog("Yellow", Duration.standardSeconds(30));
    } finally {
      ivanDockerContainer
          .stopIgnoreExceptions()
          .remove();
    }
  }

  private void createDockerLogReaderHangTestImage()
      throws InterruptedException, DockerException, IOException {
    /* Remove test Docker image if it already exists. */
    try {
      dockerClient.inspectImage(LOGHANG_DOCKERIMAGE);
      dockerClient.removeImage(LOGHANG_DOCKERIMAGE);
    } catch (final ImageNotFoundException theException) {
      /* Ignore exception and just continue to create the Docker image. */
    }

    /* Create the test Docker image. */
    final String theDockerDirectory =
        Resources.getResource("dockerDirectoryLogHang").getPath();

    final String theDockerImageId =
        dockerClient.build(Paths.get(theDockerDirectory), LOGHANG_DOCKERIMAGE);
  }
}
