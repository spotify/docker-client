/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
 * Copyright (C) 2016 Thoughtworks, Inc
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


import java.util.Objects;

public abstract class Param {

  private final String name;
  private final String value;

  Param(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Parameter name.
   *
   * @return name of parameter
   * @since Docker 1.9, API version 1.21
   */
  public String name() {
    return name;
  }

  /**
   * Parameter value.
   *
   * @return value of parameter
   * @since Docker 1.9, API version 1.21
   */
  public String value() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Param that = (Param) obj;

    return Objects.equals(name, that.name)
            && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }
}
