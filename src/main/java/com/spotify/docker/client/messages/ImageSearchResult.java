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

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * @author xcoulon
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class ImageSearchResult {

  @JsonProperty("description") private String description;
  @JsonProperty("is_official") private boolean official;
  @JsonProperty("is_automated") private boolean automated;
  @JsonProperty("name") private String name;
  @JsonProperty("star_count") private int starCount;
  
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (automated ? 1231 : 1237);
    result = prime * result + ((description == null) ? 0 : description.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + (official ? 1231 : 1237);
    result = prime * result + starCount;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ImageSearchResult other = (ImageSearchResult) obj;
    if (automated != other.automated) {
      return false;
    }
    if (description == null) {
      if (other.description != null) {
        return false;
      }
    } else if (!description.equals(other.description)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (official != other.official) {
      return false;
    }
    if (starCount != other.starCount) {
      return false;
    }
    return true;
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
