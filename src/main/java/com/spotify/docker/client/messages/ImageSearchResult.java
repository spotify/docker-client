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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * @author xcoulon
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ImageSearchResult {

  @JsonProperty("description")
  private String description;
  @JsonProperty("is_official")
  private boolean official;
  @JsonProperty("is_automated")
  private boolean automated;
  @JsonProperty("name")
  private String name;
  @JsonProperty("star_count")
  private int starCount;

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return the official
   */
  public boolean isOfficial() {
    return official;
  }

  /**
   * @return the automated
   */
  public boolean isAutomated() {
    return automated;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the starCount
   */
  public int getStarCount() {
    return starCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final ImageSearchResult that = (ImageSearchResult) o;

    return Objects.equals(this.description, that.description) &&
           Objects.equals(this.official, that.official) &&
           Objects.equals(this.automated, that.automated) &&
           Objects.equals(this.name, that.name) &&
           Objects.equals(this.starCount, that.starCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, official, automated, name, starCount);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("official", official)
        .add("automated", automated)
        .add("starCount", starCount)
        .add("description", description)
        .toString();
  }


}
