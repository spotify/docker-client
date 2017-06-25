/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2016 Spotify AB
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

import javax.annotation.Nullable;


@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ServiceCreateResponse {

  @JsonProperty("ID")
  public abstract String id();

  /**
   * @since API 1.25
   */
  @Nullable
  @JsonProperty("Warnings")
  public abstract ImmutableList<String> warnings();

  static ServiceCreateResponse create(
          @JsonProperty("ID") final String id) {
    return create(id, null);
  }

  @JsonCreator
  static ServiceCreateResponse create(
          @JsonProperty("ID") final String id,
          @JsonProperty("Warnings") final List<String> warnings) {
    final ImmutableList<String> warningsT = warnings == null
            ? null : ImmutableList.copyOf(warnings);
    return new AutoValue_ServiceCreateResponse(id, warningsT);
  }
}
