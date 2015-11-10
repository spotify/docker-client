package com.spotify.docker.test;

/**
 * Represents a port mapping from Docker host port to container port.
 * See Docker docs on
 * <a href="http://docs.docker.com/engine/reference/run/#expose-incoming-ports">port mapping</a>.
 */
public @interface PortBinding {

  /**
   * @return the Docker host port
   */
  int hostPort();

  /**
   * @return the container port
   */
  int containerPort();

  /**
   * @return the protocol for the port; must be "tcp" or "udp"
   */
  String protocol();
}
