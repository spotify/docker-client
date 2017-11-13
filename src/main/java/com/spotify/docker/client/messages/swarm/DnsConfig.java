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
import javax.annotation.Nullable;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class DnsConfig {

  @Nullable
  @JsonProperty("Nameservers")
  public abstract ImmutableList<String> nameServers();

  @Nullable
  @JsonProperty("Search")
  public abstract ImmutableList<String> search();

  @Nullable
  @JsonProperty("Options")
  public abstract ImmutableList<String> options();


  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder nameServers(String... nameServers);

    public abstract Builder nameServers(List<String> nameServers);
    
    public abstract Builder search(String... search);

    public abstract Builder search(List<String> search);
    
    public abstract Builder options(String... options);

    public abstract Builder options(List<String> options);


    public abstract DnsConfig build();
  }

  public static Builder builder() {
    return new AutoValue_DnsConfig.Builder();
  }

  @JsonCreator
  static DnsConfig create(
      @JsonProperty("Nameservers") final List<String> nameServers,
      @JsonProperty("Search") final List<String> search,
      @JsonProperty("Options") final List<String> options) {
    return builder()
        .nameServers(nameServers)
        .search(search)
        .options(options)
        .build();
  }

}
