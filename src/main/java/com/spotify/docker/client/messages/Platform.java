/*-
 * -\-\-
 * docker-client
 * --
 * Copyright (C) 2018 Spotify AB
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
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.DEFAULT;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;


@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = DEFAULT)
public abstract class Platform {

  @JsonProperty("Architecture")
  public abstract String architecture();

  @JsonProperty("OS")
  public abstract String os();

  @JsonProperty("OSVersion")
  public abstract String osVersion();

  @JsonProperty("OSFeatures")
  public abstract ImmutableList<String> osFeatures();

  @JsonProperty("Variant")
  public abstract String variant();

  @JsonProperty("Features")
  public abstract ImmutableList<String> features();

  @JsonCreator
  static Platform create(
            @JsonProperty("Architecture") String architecture,
            @JsonProperty("OS") String os,
            @JsonProperty("OSVersion") String osVersion,
            @JsonProperty("OSFeatures") ImmutableList<String> osFeatures,
            @JsonProperty("Variant") String variant,
            @JsonProperty("Features") ImmutableList<String> features) {
    return new AutoValue_Platform(architecture, os, osVersion, osFeatures,
                variant, features);
  }
}
