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
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class Image {

  @JsonProperty("Created")
  public abstract String created();

  @JsonProperty("Id")
  public abstract String id();

  @JsonProperty("ParentId")
  public abstract String parentId();

  @Nullable
  @JsonProperty("RepoTags")
  public abstract ImmutableList<String> repoTags();

  @Nullable
  @JsonProperty("RepoDigests")
  public abstract ImmutableList<String> repoDigests();

  @JsonProperty("Size")
  public abstract Long size();

  @JsonProperty("VirtualSize")
  public abstract Long virtualSize();

  @Nullable
  @JsonProperty("Labels")
  public abstract ImmutableMap<String, String> labels();

  @JsonCreator
  static Image create(
      @JsonProperty("Created") final String created,
      @JsonProperty("Id") final String id,
      @JsonProperty("ParentId") final String parentId,
      @JsonProperty("RepoTags") final List<String> repoTags,
      @JsonProperty("RepoDigests") final List<String> repoDigests,
      @JsonProperty("Size") final Long size,
      @JsonProperty("VirtualSize") final Long virtualSize,
      @JsonProperty("Labels") final Map<String, String> labels) {
    final ImmutableList<String> repoTagsCopy = repoTags == null
                                               ? null : ImmutableList.copyOf(repoTags);
    final ImmutableList<String> repoDigestsCopy = repoDigests == null
                                                  ? null : ImmutableList.copyOf(repoDigests);
    final ImmutableMap<String, String> labelsCopy = labels == null
                                                    ? null : ImmutableMap.copyOf(labels);
    return new AutoValue_Image(created, id, parentId, repoTagsCopy, repoDigestsCopy, size,
        virtualSize, labelsCopy);
  }
}
