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
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.jackson.UnixTimestampDeserializer;
import com.spotify.docker.client.jackson.UnixTimestampSerializer;

import java.util.Date;
import java.util.Map;

import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, setterVisibility = NONE, getterVisibility = NONE)
public abstract class Event {

  /**
   * Event status.
   * @return status
   * @deprecated Use {@link #action()} instead
   */
  @Deprecated
  @Nullable
  @JsonProperty("status")
  public abstract String status();

  /**
   * Event actor id. When the event type is "container" this is the container id.
   * @return id
   * @deprecated Use the {@link com.spotify.docker.client.messages.Event.Actor#id()}
   *     field from {@link #actor()}
   */
  @Deprecated
  @Nullable
  @JsonProperty("id")
  public abstract String id();

  /**
   * When the event type is "container" this is the image id.
   * @return from
   * @deprecated Use the "image" attribute in the
   *     {@link com.spotify.docker.client.messages.Event.Actor#attributes()}
   *     map from {@link #actor()}
   */
  @Deprecated
  @Nullable
  @JsonProperty("from")
  public abstract String from();

  @Nullable
  @JsonProperty("Type")
  public abstract Type type();

  /**
   * Event action.
   * @return action
   * @since API 1.22
   */
  @Nullable
  @JsonProperty("Action")
  public abstract String action();

  /**
   * Event actor.
   * @return {@link Actor}
   * @since API 1.22
   */
  @Nullable
  @JsonProperty("Actor")
  public abstract Actor actor();

  @JsonProperty("time")
  @JsonDeserialize(using = UnixTimestampDeserializer.class)
  @JsonSerialize(using = UnixTimestampSerializer.class)
  public abstract Date time();

  @Nullable
  @JsonProperty("timeNano")
  public abstract Long timeNano();

  @JsonCreator
  static Event create(
      @JsonProperty("status") final String status,
      @JsonProperty("id") final String id,
      @JsonProperty("from") final String from,
      @JsonProperty("Type") final Type type,
      @JsonProperty("Action") final String action,
      @JsonProperty("Actor") final Actor actor,
      @JsonProperty("time") final Date time,
      @JsonProperty("timeNano") final Long timeNano) {
    return new AutoValue_Event(status, id, from, type, action, actor, time, timeNano);
  }

  @AutoValue
  public abstract static class Actor {

    @JsonProperty("ID")
    public abstract String id();

    @Nullable
    @JsonProperty("Attributes")
    public abstract ImmutableMap<String, String> attributes();

    @JsonCreator
    static Actor create(
        @JsonProperty("ID") final String id,
        @JsonProperty("Attributes") final Map<String, String> attributes) {
      final ImmutableMap<String, String> attributesT = attributes == null
                                                       ? null : ImmutableMap.copyOf(attributes);
      return new AutoValue_Event_Actor(id, attributesT);
    }
  }

  public enum Type {
    CONTAINER("container"),
    IMAGE("image"),
    VOLUME("volume"),
    NETWORK("network"),
    DAEMON("daemon"),
    PLUGIN("plugin"),
    NODE("node"),
    SERVICE("service"),
    SECRET("secret");

    private final String name;

    @JsonCreator
    Type(final String name) {
      this.name = name;
    }

    @JsonValue
    public String getName() {
      return name;
    }
  }
}
