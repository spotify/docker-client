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

package com.spotify.docker.client.messages;

import java.util.Collections;
import java.util.List; 
import java.util.Map; 

import com.google.common.base.MoreObjects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class LogConfig {

  @JsonProperty("Type") private String logType;
  @JsonProperty("Config") private Map<String, String> logOptions;

  public String logType() {
    return logType;
  }

  public void logType(final String logType) {
    this.logType = logType;
  }

  public Map<String, String> logOptions() {
    return (logOptions == null) ? null : Collections.unmodifiableMap(logOptions);
  }

  public void logOptions(final Map<String, String> logOptions) {
    this.logOptions = logOptions;
  }

  public static LogConfig create(final String logType, final Map<String, String> logOptions) {
    final LogConfig logConfig = new LogConfig();
    logConfig.logType(logType);
    logConfig.logOptions(logOptions);
    return logConfig;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final LogConfig that = (LogConfig) o;

    if (logType != null ? !logType.equals(that.logType) : that.logType != null) {
      return false;
    }
    if (logOptions != null ? !logOptions.equals(that.logOptions) : that.logOptions != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = logType != null ? logType.hashCode() : 0;
    result = 31 * result + (logOptions != null ? logOptions.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("Type", logType)
        .add("Config", logOptions)
        .toString();
  }
}
