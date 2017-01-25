package com.spotify.docker.it;

import com.google.common.base.Preconditions;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import com.spotify.docker.test.DockerContainer;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a Docker container.
 *
 * @author Ivan Krizsan
 */
public class IvanDockerContainer {
    /* Constant(s): */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(DockerContainer.class);
    /**
     * Docker container status created.
     */
    public static final String STATUS_CREATED = "created";
    /**
     * Docker container status restarting.
     */
    public static final String STATUS_RESTARTING = "restarting";
    /**
     * Docker container status running.
     */
    public static final String STATUS_RUNNING = "running";
    /**
     * Docker container status removing.
     */
    public static final String STATUS_REMOVING = "removing";
    /**
     * Docker container status paused.
     */
    public static final String STATUS_PAUSED = "paused";
    /**
     * Docker container status exited.
     */
    public static final String STATUS_EXITED = "exited";
    /**
     * Docker container status dead.
     */
    public static final String STATUS_DEAD = "dead";

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

        final String theContainerCreationJson;
        try {
            theContainerCreationJson =
                JsonConverter.objectToJson(theContainerCreation, true);
            LOGGER.debug("Created Docker container with name {}"
                    + " and id {} from Docker image {}",
                mDockerContainerName,
                mDockerContainerId,
                mDockerImageName);
            LOGGER.debug("Container creation result: {}", theContainerCreationJson);
        } catch (final Exception theException) {
            theException.printStackTrace();
        }

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
        logContainerInfo();

        return this;
    }

    /**
     * Logs information about the Docker container to debug log, if enabled.
     *
     * @return This object.
     */
    public IvanDockerContainer logContainerInfo() {
        if (LOGGER.isDebugEnabled()) {
            try {
                final ContainerInfo theContainerInfo =
                    mDockerClient.inspectContainer(mDockerContainerId);
                final String theContainerInfoJson =
                    JsonConverter.objectToJson(theContainerInfo, true);
                LOGGER.debug(
                    "Info for Docker container with id " + mDockerContainerId
                        + ": " + theContainerInfoJson);
            } catch (final Exception theException) {
                LOGGER.warn(
                    "Exception occurred trying to log info for Docker container "
                        + mDockerContainerId,
                    theException);
            }
        }

        return this;
    }

    /**
     * Stops the Docker container if it is running.
     *
     * @return This object.
     * @throws DockerException If error occurred trying to stop Docker container.
     * @throws InterruptedException If thread was interrupted.
     */
    public IvanDockerContainer stop() throws DockerException, InterruptedException {
        mDockerClient.killContainer(mDockerContainerId);
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
     * Checks whether the Docker container is running or not.
     *
     * @return True if Docker container is running, false otherwise.
     */
    public boolean isRunning() {
        boolean theContainerRunningFlag = false;
        try {
            /* If there is no container id, then container cannot be running. */
            if (StringUtils.isNotEmpty(mDockerContainerId)) {
                final ContainerInfo theContainerInfo =
                    mDockerClient.inspectContainer(mDockerContainerId);
                theContainerRunningFlag = theContainerInfo.state().running();

                if (theContainerRunningFlag) {
                    LOGGER.debug("Docker container {} is up and running.",
                        mDockerContainerId);
                }
            }
        } catch (final Exception theException) {
            LOGGER.error("Error occurred checking if Docker container is running",
                theException);
        }
        return theContainerRunningFlag;
    }

    /**
     * Checks whether the Docker container has finished or not.
     *
     * @return True if Docker container has finished, false otherwise.
     * @throws DockerException If error occurred inspecting Docker container.
     * @throws InterruptedException If thread was interrupted.
     */
    public boolean hasFinished() throws DockerException, InterruptedException {
        boolean theContainerFinishedFlag = false;

        if (StringUtils.isNotEmpty(mDockerContainerId)) {
            final ContainerInfo theContainerInfo =
                mDockerClient.inspectContainer(mDockerContainerId);
            final String theContainerStatus = theContainerInfo.state().status();
            theContainerFinishedFlag = containerStatusIsOneOf(
                theContainerStatus, STATUS_DEAD, STATUS_EXITED, STATUS_REMOVING);

            if (theContainerFinishedFlag) {
                LOGGER.debug("Docker container {} has finished with exit "
                        + "code {} and status {}",
                    mDockerContainerId, theContainerInfo.state().exitCode(),
                    theContainerInfo.state().status());
            }
        }
        return theContainerFinishedFlag;
    }

    /**
     * Determines whether the supplied container status matches any of the supplied
     * container status values to match.
     *
     * @param inContainerStatus Container status.
     * @param inContainerStatusValuesToMatch Container status values to match
     * against.
     * @return True of the container status matches any of the values to match.
     */
    protected boolean containerStatusIsOneOf(final String inContainerStatus,
        final String... inContainerStatusValuesToMatch) {
        for (String theContainerStatusValue : inContainerStatusValuesToMatch) {
            if (theContainerStatusValue.equalsIgnoreCase(inContainerStatus)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Waits for the supplied duration of time until the Docker container with
     * the supplied container id is running.
     * Will time out if supplied timeout-time is exceeded.
     *
     * @param inTimeout Duration of time to wait for container to enter running
     * state.
     * @return This object.
     * @throws DockerException If error occurred trying to determine Docker container
     * state.
     * @throws InterruptedException If waiting for container to enter running state
     * times out.
     */
    public IvanDockerContainer waitUntilRunning(final Duration inTimeout)
        throws InterruptedException, DockerException {
        boolean theContainerRunningFlag;
        final DateTime theStartTime = new DateTime();

        LOGGER.debug("Waiting for container with id {} enter running state.",
            mDockerContainerId);

        if (!hasFinished()) {
            do {
            /* Timeout waiting for the container? */
                final DateTime theCurrentTime = new DateTime();
                final Duration theWaitTime = new Interval(
                    theStartTime, theCurrentTime).toDuration();
                if (theWaitTime.isLongerThan(inTimeout)) {
                    final String theErrorMessage =
                        MessageFormat.format("Timed out waiting for container "
                                + "with id {0} to enter running state.",
                            mDockerContainerId);
                    LOGGER.debug(theErrorMessage);
                    throw new InterruptedException(theErrorMessage);
                }

                theContainerRunningFlag = isRunning();
            } while (!theContainerRunningFlag);

            LOGGER.debug("Docker container with id {} is now running.",
                mDockerContainerId);
            logContainerInfo();
        } else {
            LOGGER.debug("Docker container with id {} has already finished",
                mDockerContainerId);
            throw new IllegalStateException("Docker container has already finished");
        }
        return this;
    }

    /**
     * Clears all the port bindings of the container.
     * This does not affect a container that is already started.
     *
     * @return This object.
     */
    public IvanDockerContainer clearPortBindings() {
        mContainerPortMappings.clear();
        return this;
    }

    /**
     * Adds a binding of a port that will be exposed when the Docker container
     * starts. Port ranges are specified in the format 1234-1236.
     *
     * @param inHostPort Port, or port range, in the host on which the container
     * port will be exposed.
     * @param inContainerPort Port, or port range, in the container that is to be
     * exposed.
     * @param inHostIPAddress IP address in the host at which the port will
     * be exposed.
     * @return This object.
     */
    public IvanDockerContainer addPortBinding(final String inHostPort,
        final String inContainerPort, final String inHostIPAddress) {
        List<PortBinding> thePortBindings;
        if (mContainerPortMappings.containsKey(inContainerPort)) {
            thePortBindings = mContainerPortMappings.get(inContainerPort);
        } else {
            thePortBindings = new ArrayList<>();
            mContainerPortMappings.put(inContainerPort, thePortBindings);
        }
        final PortBinding theNewPortBinding =
            PortBinding.of(inHostIPAddress, inHostPort);
        thePortBindings.add(theNewPortBinding);

        return this;
    }

    /**
     * Adds a binding of a port that will be exposed when the Docker container
     * starts. Port ranges are specified in the format 1234-1236.
     *
     * @param inHostPort Port, or port range, in the host on which the container
     * port will be exposed.
     * @param inContainerPort Port, or port range, in the container that is to be
     * exposed.
     * @return This object.
     */
    public IvanDockerContainer addPortBinding(final String inHostPort,
        final String inContainerPort) {
        addPortBinding(inHostPort, inContainerPort, null);
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

    /**
     * Adds supplied environment variable to the list of environment variables
     * that will be set in the Docker container.
     * Environment variables are on the form: VARIABLENAME=VALUE
     *
     * @param inVariable Environment variable name.
     * @return This object.
     */
    public IvanDockerContainer addEnvironmentVar(final String inVariable) {
        mEnvironmentVariables.add(inVariable);
        return this;
    }

    /**
     * Adds supplied command to the list of commands that will be executed
     * when the Docker container starts.
     *
     * @param inCommand Command.
     * @return This object.
     * @see <a href="https://docs.docker.com/engine/reference/run/#/cmd-default-command-or-options"/>
     */
    public IvanDockerContainer addCommand(final String inCommand) {
        mCommands.add(inCommand);
        return this;
    }

    /**
     * Adds supplied entrypoint ( to the list of shell commands that will be executed
     * when the Docker container starts.
     *
     * @param inEntrypoint Entrypoint.
     * @return This object.
     * @see <a href="https://docs.docker.com/engine/reference/run/#/entrypoint-default-command-to-execute-at-runtime"/>
     */
    public IvanDockerContainer addEntrypoint(final String inEntrypoint) {
        mEntrypoints.add(inEntrypoint);
        return this;
    }


    public String logs() {
        // TODO implement me
        return "";
    }

    public String getDockerImageName() {
        return mDockerImageName;
    }

    public IvanDockerContainer setDockerImageName(final String inDockerImageName) {
        mDockerImageName = inDockerImageName;
        return this;
    }

    public String getDockerContainerName() {
        return mDockerContainerName;
    }

    public IvanDockerContainer setDockerContainerName(final String inDockerContainerName) {
        mDockerContainerName = inDockerContainerName;
        return this;
    }

    public String getDockerContainerId() {
        return mDockerContainerId;
    }
}
