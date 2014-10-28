/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import com.google.common.base.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Raw results from the "top" (or "ps") command for a specific container
 */
public class TopResults {

  @JsonProperty("Titles") private List<String> titles;
  @JsonProperty("Processes") private List<List<String>> processes;

  public List<String> titles() {
    return titles;
  }

  public List<List<String>> processes() {
    return processes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TopResults that = (TopResults) o;

    return Objects.equal(this.titles, that.titles) &&
        Objects.equal(this.processes, that.processes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(titles, processes);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("titles", titles)
        .add("processes", processes)
        .toString();
  }
}
