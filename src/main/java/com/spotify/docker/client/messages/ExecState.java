/*
 * Copyright (c) 2015 Spotify AB.
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

package com.spotify.docker.client.messages;

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ExecState {

  @JsonProperty("Running") private Boolean running;
  @JsonProperty("ExitCode") private Integer exitCode;

  public Boolean running() {
    return running;
  }

  public Integer exitCode() {
    return exitCode;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ExecState that = (ExecState) o;

    if (running != null ? !running.equals(that.running) : that.running != null) {
      return false;
    }
    if (exitCode != null ? !exitCode.equals(that.exitCode) : that.exitCode != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = running != null ? running.hashCode() : 0;
    result = 31 * result + (exitCode != null ? exitCode.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("running", running)
        .add("exitCode", exitCode)
        .toString();
  }
}
