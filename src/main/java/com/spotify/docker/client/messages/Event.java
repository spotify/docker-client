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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.jackson.UnixTimestampDeserializer;

import java.util.Date;
import java.util.Objects;

@JsonAutoDetect(fieldVisibility = ANY, setterVisibility = NONE, getterVisibility = NONE)
public class Event {

  @JsonProperty("status") private String status;
  @JsonProperty("id") private String id;
  @JsonProperty("from") private String from;
  @JsonProperty("Type") private String type;
  @JsonProperty("Action") private String action;
  @JsonProperty("Actor") private Actor actor;
  @JsonProperty("time")
  @JsonDeserialize(using = UnixTimestampDeserializer.class)
  private Date time;
  @JsonProperty("timeNano") private Long timeNano;

  /**
   * Event status.
   * @return status
   * @deprecated Use {@link #action()} instead
   */
  @Deprecated
  public String status() {
    return status;
  }

  /**
   * Event actor id. When the event type is "container" this is the container id.
   * @return id
   * @deprecated Use the {@link com.spotify.docker.client.messages.Event.Actor#id()}
   *     field from {@link #actor()}
   */
  @Deprecated
  public String id() {
    return id;
  }

  /**
   * When the event type is "container" this is the image id.
   * @return from
   * @deprecated Use the "image" attribute in the
   *     {@link com.spotify.docker.client.messages.Event.Actor#attributes()}
   *     map from {@link #actor()}
   */
  @Deprecated
  public String from() {
    return from;
  }

  public String type() {
    return type;
  }

  /**
   * Event action.
   * @return action
   * @since API 1.22
   */
  public String action() {
    return action;
  }

  /**
   * Event actor.
   * @return actor
   * @since API 1.22
   */
  public Actor actor() {
    return actor;
  }

  public Date time() {
    return time == null ? null : new Date(time.getTime());
  }

  public Long timeNano() {
    return timeNano;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final Event that = (Event) obj;

    return Objects.equals(this.status, that.status)
           && Objects.equals(this.id, that.id)
           && Objects.equals(this.from, that.from)
           && Objects.equals(this.type, that.type)
           && Objects.equals(this.action, that.action)
           && Objects.equals(this.actor, that.actor)
           && Objects.equals(this.time, that.time)
           && Objects.equals(this.timeNano, that.timeNano);

  }

  @Override
  public int hashCode() {
    return Objects.hash(status, id, from, time);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("status", status)
        .add("id", id)
        .add("from", from)
        .add("type", type)
        .add("action", action)
        .add("actor", actor)
        .add("time", time)
        .add("timeNano", timeNano)
        .toString();
  }

  public static class Actor {
    @JsonProperty("ID") private String id;
    @JsonProperty("Attributes") private ImmutableMap<String, String> attributes;

    public String id() {
      return id;
    }

    public ImmutableMap<String, String> attributes() {
      return attributes;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final Actor that = (Actor) obj;
      return Objects.equals(this.id, that.id)
             && Objects.equals(this.attributes, that.attributes);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, attributes);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("attributes", attributes)
          .toString();
    }
  }
}
