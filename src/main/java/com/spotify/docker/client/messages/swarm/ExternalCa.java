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

package com.spotify.docker.client.messages.swarm;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class ExternalCa {

  public static final String PROTOCOL_CFSSL = "cfssl";

  @JsonProperty("Protocol")
  public abstract String protocol();

  @JsonProperty("URL")
  public abstract String url();

  @JsonProperty("Options")
  public abstract ImmutableMap<String, String> options();

  @JsonCreator
  static ExternalCa create(
      @JsonProperty("Protocol") final String protocol,
      @JsonProperty("URL") final String url,
      @JsonProperty("Options") final Map<String, String> options) {
    return new AutoValue_ExternalCa(protocol, url, ImmutableMap.copyOf(options));
  }
}
