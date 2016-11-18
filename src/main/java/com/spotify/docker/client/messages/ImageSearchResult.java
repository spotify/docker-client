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


@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ImageSearchResult {

  @JsonProperty("description")
  public abstract String description();

  @JsonProperty("is_official")
  public abstract boolean official();

  @JsonProperty("is_automated")
  public abstract boolean automated();

  @JsonProperty("name")
  public abstract String name();

  @JsonProperty("star_count")
  public abstract int starCount();

  @JsonCreator
  static ImageSearchResult create(
      @JsonProperty("description") final String description,
      @JsonProperty("is_official") final boolean official,
      @JsonProperty("is_automated") final boolean automated,
      @JsonProperty("name") final String name,
      @JsonProperty("star_count") final int starCount) {
    return new AutoValue_ImageSearchResult(description, official, automated, name, starCount);
  }
}
