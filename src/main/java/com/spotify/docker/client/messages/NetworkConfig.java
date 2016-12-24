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
import com.google.auto.value.AutoValue;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class NetworkConfig {

  @NotNull
  @JsonProperty("Name")
  public abstract String name();

  @Nullable
  @JsonProperty("Driver")
  public abstract String driver();

  @Nullable
  @JsonProperty("IPAM")
  public abstract Ipam ipam();

  @NotNull
  @JsonProperty("Options")
  public abstract ImmutableMap<String, String> options();

  @Nullable
  @JsonProperty("CheckDuplicate")
  public abstract Boolean checkDuplicate();

  @NotNull
  public static Builder builder() {
    return new AutoValue_NetworkConfig.Builder()
        .options(new HashMap<String, String>());
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder name(final String name);

    abstract ImmutableMap.Builder<String, String> optionsBuilder();

    public Builder addOption(final String key, final String value) {
      optionsBuilder().put(key, value);
      return this;
    }

    public abstract Builder options(Map<String, String> options);

    public abstract Builder ipam(final Ipam ipam);

    public abstract Builder driver(final String driver);

    public abstract Builder checkDuplicate(Boolean check);

    public abstract NetworkConfig build();
  }

}
