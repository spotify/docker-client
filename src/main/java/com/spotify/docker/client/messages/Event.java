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
import com.spotify.docker.client.jackson.UnixTimestampDeserializer;

import java.util.Date;
import java.util.Objects;

@JsonAutoDetect(fieldVisibility = ANY, setterVisibility = NONE, getterVisibility = NONE)
public class Event {

  @JsonProperty("status")
  private String status;
  @JsonProperty("id")
  private String id;
  @JsonProperty("from")
  private String from;

  @JsonProperty("time")
  @JsonDeserialize(using = UnixTimestampDeserializer.class)
  private Date time;

  public String status() {
    return status;
  }

  public String id() {
    return id;
  }

  public String from() {
    return from;
  }

  public Date time() {
    return time == null ? null : new Date(time.getTime());
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
           && Objects.equals(this.time, that.time);

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
        .add("time", time)
        .toString();
  }
}
