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
import com.google.common.collect.ImmutableList;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class NetworkAttachmentConfig {

  @NotNull
  @JsonProperty("Target")
  public abstract String target();

  @Nullable
  @JsonProperty("Aliases")
  public abstract ImmutableList<String> aliases();

  @NotNull
  public static Builder builder() {
    return new AutoValue_NetworkAttachmentConfig.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    @JsonProperty("Target")
    public abstract Builder target(String target);

    @JsonProperty("Aliases")
    public abstract Builder aliases(String... aliases);

    @JsonProperty("Aliases")
    public abstract Builder aliases(List<String> aliases);

    public abstract NetworkAttachmentConfig build();
  }

  @JsonCreator
  static NetworkAttachmentConfig create(
      @JsonProperty("Target") final String target,
      @JsonProperty("Aliases") final List<String> aliases) {
    return builder()
        .target(target)
        .aliases(aliases)
        .build();
  }
}
