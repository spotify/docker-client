package com.spotify.docker.it;


import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.joda.time.Duration;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for issue 552: Obtaining next log message from Docker container hangs
 * indefinitely.
 * Note that prior to running this test, the tomcat:latest Docker image needs
 * to be pulled.
 *
 * @author Ivan Krizsan
 */
public class LogStreamHangTest {
    /* Class variable(s): */
    protected static DockerClient mDockerClient;

    /**
     * Performs setup before all the test methods.
     */
    @BeforeClass
    public static void setUpOnceBeforeAllTests() throws DockerCertificateException {
        mDockerClient = DefaultDockerClient.fromEnv().readTimeoutMillis(10000).build();
    }

    /**
     * Tests issue 552 obtaining next log message from Docker container hangs
     * indefinitely.
     * Note that if this test fails due to timeout, the Docker container needs
     * to be stopped and removed manually.
     * Expected result: An exception should be thrown when waiting for a certain
     * string in container log output times out.
     *
     * @throws DockerException If error occurs running Docker container.
     * @throws InterruptedException Expected exception.
     */
    @Test(timeout = 40000L, expected = InterruptedException.class)
    public void testContainerLogHang()
        throws DockerException, InterruptedException {
        final IvanDockerContainer theTomcatContainer =
            new IvanDockerContainer(mDockerClient, "tomcat:latest");

        try {
            theTomcatContainer.
                setDockerContainerName("TomcatContainer").
                create().
                start().
                waitUntilRunning(Duration.standardSeconds(10)).
                logContainerInfo().
                attachToLogStreams().
                waitForLog("Yellow", Duration.standardSeconds(30));
        } finally {
            theTomcatContainer
                .stopIgnoreExceptions()
                .remove();
        }
    }
}
