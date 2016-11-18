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
public abstract class ImageHistory {

  @JsonProperty("Id")
  public abstract String id();

  @JsonProperty("Created")
  public abstract Long created();

  @JsonProperty("CreatedBy")
  public abstract String createdBy();

  @Nullable
  @JsonProperty("Tags")
  public abstract ImmutableList<String> tags();

  @JsonProperty("Size")
  public abstract Long size();

  @Nullable
  @JsonProperty("Comment")
  public abstract String comment();

  @JsonCreator
  static ImageHistory create(
      @JsonProperty("Id") final String id,
      @JsonProperty("Created") final Long created,
      @JsonProperty("CreatedBy") final String createdBy,
      @JsonProperty("Tags") final List<String> tags,
      @JsonProperty("Size") final Long size,
      @JsonProperty("Comment") final String comment) {
    final ImmutableList<String> tagsCopy = tags == null
                                           ? null : ImmutableList.copyOf(tags);
    return new AutoValue_ImageHistory(id, created, createdBy, tagsCopy, size, comment);
  }
}
