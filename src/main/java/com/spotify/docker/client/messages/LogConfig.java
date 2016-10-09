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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class LogConfig {

  @JsonProperty("Type")
  private String logType;
  @JsonProperty("Config")
  private Map<String, String> logOptions;

  @SuppressWarnings("unused")
  private LogConfig() {
  }

  private LogConfig(final String logType, final Map<String, String> logOptions) {
    this.logType = logType;
    this.logOptions = ImmutableMap.copyOf(logOptions);
  }

  public String logType() {
    return logType;
  }

  public Map<String, String> logOptions() {
    return (logOptions == null) ? null : Collections.unmodifiableMap(logOptions);
  }

  public static LogConfig create(final String logType) {
    return create(logType, Collections.<String, String>emptyMap());
  }

  public static LogConfig create(final String logType, final Map<String, String> logOptions) {
    return new LogConfig(logType, logOptions);
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

    return Objects.equals(this.logType, that.logType) &&
           Objects.equals(this.logOptions, that.logOptions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(logType, logOptions);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("Type", logType)
        .add("Config", logOptions)
        .toString();
  }
}
