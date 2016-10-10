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

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProgressDetail {

  @JsonProperty
  private long current;
  @JsonProperty
  private long start;
  @JsonProperty
  private long total;

  public long current() {
    return current;
  }

  public long start() {
    return start;
  }

  public long total() {
    return total;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("current", current)
        .add("start", start)
        .add("total", total)
        .toString();
  }

}
