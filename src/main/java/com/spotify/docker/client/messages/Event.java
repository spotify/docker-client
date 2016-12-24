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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.google.auto.value.AutoValue;
import com.spotify.docker.client.jackson.UnixTimestampDeserializer;
import java.util.Date;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, setterVisibility = NONE, getterVisibility = NONE)
public abstract class Event {

  @Nullable
  @JsonProperty("status")
  public abstract String status();

  @Nullable
  @JsonProperty("id")
  public abstract String id();

  @Nullable
  @JsonProperty("from")
  public abstract String from();

  @NotNull
  @JsonProperty("time")
  @JsonDeserialize(using = UnixTimestampDeserializer.class)
  public abstract Date time();

  @JsonCreator
  static Event create(
      @JsonProperty("status") final String status,
      @JsonProperty("id") final String id,
      @JsonProperty("from") final String from,
      @JsonProperty("time") final Date time) {
    return new AutoValue_Event(status, id, from, time);
  }
}
