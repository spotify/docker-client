package com.spotify.docker.it;

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
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a Docker container.
 * Stripped down version for the {@link LogReaderHangTest}.
 *
 * @author Ivan Krizsan
 */
public class IvanDockerContainer {
    /* Constant(s): */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(DockerContainer.class);

    /* Instance variable(s): */
    protected DockerClient mDockerClient;
    protected String mDockerImageName;
    protected String mDockerContainerName;
    protected String mDockerContainerId;
    protected Map<String, List<PortBinding>> mContainerPortMappings
        = new HashMap<>();
    protected List<String> mEnvironmentVariables = new ArrayList<>();
    protected List<String> mCommands = new ArrayList<>();
    protected List<String> mEntrypoints = new ArrayList<>();
    protected LogStream mContainerLogStream;

    /**
     * Creates a {@code IvanDockerContainer} that interacts with Docker using the
     * supplied client and will be created from the Docker image with the supplied
     * name.
     *
     * @param inDockerClient Docker client.
     * @param inDockerImageName Name of Docker image from which to create container.
     * Note that the name is created from the repository name and the image tag.
     * Example: ubuntu:16.04
     */
    public IvanDockerContainer(final DockerClient inDockerClient,
        final String inDockerImageName) {
        Preconditions.checkNotNull(inDockerClient);
        Preconditions.checkArgument(StringUtils.isNotEmpty(inDockerImageName));

        mDockerClient = inDockerClient;
        mDockerImageName = inDockerImageName;
    }

    /**
     * Creates the Docker container.
     * Will create a name for the new Docker container consisting of a random
     * UUID if no Docker container name has been set.
     * Note that all configuration of arguments, ports, environment variables
     * etc etc must be performed prior to creating a Docker container.
     *
     * @return This object.
     * @throws DockerException If error occurred creating Docker container.
     * @throws InterruptedException If thread was interrupted.
     */
    public IvanDockerContainer create() throws DockerException,
        InterruptedException {
        Preconditions.checkNotNull(mDockerClient);
        Preconditions.checkArgument(StringUtils.isNotEmpty(mDockerImageName));

        final HostConfig.Builder theHostConfigBuilder = HostConfig.builder();
        final HostConfig theHostConfig =
            theHostConfigBuilder.portBindings(mContainerPortMappings).build();

        final ContainerConfig theContainerConfig =
            ContainerConfig.builder().
                hostConfig(theHostConfig).
                image(mDockerImageName).
                exposedPorts(mContainerPortMappings.keySet()).
                env(mEnvironmentVariables).
                cmd(mCommands).
                entrypoint(mEntrypoints).
                build();

        /* Create a name for the Docker container if none is set. */
        if (StringUtils.isEmpty(mDockerContainerName)) {
            mDockerContainerName = UUID.randomUUID().toString();
        }

        final ContainerCreation theContainerCreation =
            mDockerClient.createContainer(
                theContainerConfig, mDockerContainerName);
        mDockerContainerId = theContainerCreation.id();

        return this;
    }

    /**
     * Starts the Docker container that previously has been created.
     *
     * @return This object.
     * @throws DockerException If an error occurred starting the Docker container.
     * @throws InterruptedException If the thread was interrupted.
     */
    public IvanDockerContainer start() throws DockerException, InterruptedException {
        if (StringUtils.isEmpty(mDockerContainerId)) {
            throw new IllegalStateException(
                "A Docker container must be created before it can be started");
        }
        mDockerClient.startContainer(mDockerContainerId);
        LOGGER.debug("Started Docker container with id {}", mDockerContainerId);

        return this;
    }

    /**
     * Stops the Docker container if it is running, ignoring all exceptions.
     * Exceptions will be logged, but not rethrown.
     *
     * @return This object.
     */
    public IvanDockerContainer stopIgnoreExceptions() {
        try {
            mDockerClient.killContainer(mDockerContainerId);
        } catch (final Exception theException) {
            LOGGER.error("An error occurred trying to stop Docker container"
                + " with id: " + mDockerContainerId, theException);
        }

        return this;
    }

    /**
     * Removes the Docker container from the set of containers maintained
     * locally by the Docker client.
     * Does nothing if the Docker container does not exist.
     *
     * @return This object.
     * @throws DockerException If error occurred trying to remove Docker container.
     * @throws InterruptedException If thread was interrupted.
     */
    public IvanDockerContainer remove()
        throws DockerException, InterruptedException {
        mDockerClient.removeContainer(mDockerContainerId);
        return this;
    }

    /**
     * Attaches to the log streams of a running Docker container.
     *
     * @return This object.
     * @throws DockerException If error occurred trying attach to Docker container's
     * log stream.
     * @throws InterruptedException If thread was interrupted.
     */
    public IvanDockerContainer attachToLogStreams()
        throws DockerException, InterruptedException {
//            mContainerLogStream = mDockerClient.attachContainer(mDockerContainerId,
//                DockerClient.AttachParameter.LOGS,
//                DockerClient.AttachParameter.STDOUT,
//                DockerClient.AttachParameter.STDERR,
//                DockerClient.AttachParameter.STREAM);
        mContainerLogStream = mDockerClient.logs(
            mDockerContainerId,
            DockerClient.LogsParam.stdout(),
            DockerClient.LogsParam.stderr(),
            DockerClient.LogsParam.timestamps(),
            DockerClient.LogsParam.follow());

        return this;
    }

    /**
     * Waits the maximum supplied duration of time for the Docker container output
     * log containing the supplied string to match.
     * Will timeout if the supplied timeout-time is exceeded.
     * Note that before waiting for log, you must attach to the log streams of
     * the running Docker container.
     *
     * @param inStringToMatch Case-sensitive string to look for in log from
     * the container.
     * @param inTimeout Maximum time to wait for log from container.
     * @return This object.
     * @throws InterruptedException If timed out waiting for container log.
     */
    public IvanDockerContainer waitForLog(final String inStringToMatch,
        final Duration inTimeout) throws InterruptedException {
        if (mContainerLogStream == null) {
            throw new IllegalStateException(
                "Not attached to Docker container's log streams");
        }

        boolean theLogStringMatched = false;
        final DateTime theStartTime = new DateTime();

        LOGGER.debug("Waiting for log string '{}' from container with id {}...",
            inStringToMatch, mDockerContainerId);

        String theContainerLogString = "";

        do {
            /* Timeout waiting for the container? */
            final DateTime theCurrentTime = new DateTime();
            final Duration theCurrentWaitDuration =
                new Interval(theStartTime, theCurrentTime).toDuration();
            if (theCurrentWaitDuration.isLongerThan(inTimeout)) {
                LOGGER.debug(
                    "Matching string '{}' in log from container with id {} timed out",
                    inStringToMatch,
                    mDockerContainerId);
                throw new InterruptedException(
                    "Timeout waiting for log from Docker container with id "
                        + mDockerContainerId);
            }

            /*
             * Is there log output from the container?
             * If so, check if it contains the sought-for string.
             */
            if (mContainerLogStream.hasNext()) {
                final LogMessage theContainerLogMessage =
                    mContainerLogStream.next();
                theContainerLogString =
                    String.valueOf(UTF_8.decode(theContainerLogMessage.content()));
                LOGGER.debug("Log string from container {}: {}", mDockerContainerId,
                    theContainerLogString);

                theLogStringMatched =
                    theContainerLogString.contains(inStringToMatch);
            }
        } while (!theLogStringMatched);

        LOGGER.debug("Found string '{}' in Docker container log '{}'",
            inStringToMatch, theContainerLogString);

        return this;
    }

    public IvanDockerContainer setDockerContainerName(final String inDockerContainerName) {
        mDockerContainerName = inDockerContainerName;
        return this;
    }
}
