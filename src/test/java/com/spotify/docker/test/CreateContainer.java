/*
 * Copyright (c) 2014 Jan-Willem Gmelig Meyling.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@link CreateContainer} annotation can be used on any test method to provide the values for
 * the {@link com.spotify.docker.client.messages.ContainerCreation} object that the {@link
 * DockerContainer} will username for the {@link com.spotify.docker.client.DockerClient}.
 *
 * @author Jan-Willem Gmelig Meyling
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface CreateContainer {

  /**
   * @return String value containing the image name to use for the container
   */
  String image();

  /**
   * @return An array of mountpoint mappings (strings) inside the container
   */
  String[] volumes() default {};

  /**
   * @return Command to run specified as an array of strings.
   */
  String[] command() default {};

  /**
   * Start the container after creation
   */
  boolean start() default false;

}
