/*
 * Copyright (c) 2016 Spotify AB.
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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ImageHistory {

  @JsonProperty("Id")
  private String id;
  @JsonProperty("Created")
  private Long created;
  @JsonProperty("CreatedBy")
  private String createdBy;
  @JsonProperty("Tags")
  private ImmutableList<String> tags;
  @JsonProperty("Size")
  private Long size;
  @JsonProperty("Comment")
  private String comment;

  public String id() {
    return id;
  }

  public Long created() {
    return created;
  }

  public String createdBy() {
    return createdBy;
  }

  public ImmutableList<String> tags() {
    return tags;
  }

  public Long size() {
    return size;
  }

  public String comment() {
    return comment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ImageHistory that = (ImageHistory) o;

    return Objects.equals(this.id, that.id) &&
           Objects.equals(this.created, that.created) &&
           Objects.equals(this.createdBy, that.createdBy) &&
           Objects.equals(this.tags, that.tags) &&
           Objects.equals(this.size, that.size) &&
           Objects.equals(this.comment, that.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, created, createdBy, tags, size, comment);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("created", created)
        .add("createdBy", createdBy)
        .add("tags", tags)
        .add("size", size)
        .add("comment", comment)
        .toString();
  }
}
