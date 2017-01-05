/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
 * Copyright (c) 2014 Oleg Poleshuk
 * Copyright (c) 2014 CyDesign Ltd
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

package com.spotify.docker.client.messages;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Raw results from the "top" (or "ps") command for a specific container.
 */
@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class TopResults {

  @JsonProperty("Titles")
  public abstract ImmutableList<String> titles();

  @JsonProperty("Processes")
  public abstract ImmutableList<ImmutableList<String>> processes();

  @JsonCreator
  static TopResults create(
      @JsonProperty("Titles") final List<String> titles,
      @JsonProperty("Processes") final List<List<String>> processes) {
    final ImmutableList.Builder<ImmutableList<String>> processesBuilder = ImmutableList.builder();
    for (final List<String> process : processes) {
      processesBuilder.add(ImmutableList.copyOf(process));
    }
    return new AutoValue_TopResults(ImmutableList.copyOf(titles), processesBuilder.build());
  }
}
