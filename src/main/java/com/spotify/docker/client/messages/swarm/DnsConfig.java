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

    /**
    * @deprecated  As of release 7.0.0, replaced by {@link #nameServers(String...)}.
    */
    @Deprecated
    public Builder withNameServers(final String... nameServers) {
      if (nameServers != null && nameServers.length > 0) {
        nameServers(nameServers);
      }
      return this;
    }

    /**
    * @deprecated  As of release 7.0.0, replaced by {@link #nameServers(List)}.
    */
    @Deprecated
    public Builder withNameServers(final List<String> nameServers) {
      nameServers(nameServers);
      return this;
    }
    
    public abstract Builder search(String... search);

    public abstract Builder search(List<String> search);

    /**
    * @deprecated  As of release 7.0.0, replaced by {@link #search(String...)}.
    */
    @Deprecated
    public Builder withsearch(final String... search) {
      if (search != null && search.length > 0) {
        search(search);
      }
      return this;
    }

    /**
    * @deprecated  As of release 7.0.0, replaced by {@link #search(List)}.
    */
    @Deprecated
    public Builder withsearch(final List<String> search) {
      search(search);
      return this;
    }
    
    public abstract Builder options(String... options);

    public abstract Builder options(List<String> options);

    /**
    * @deprecated  As of release 7.0.0, replaced by {@link #options(String...)}.
    */
    @Deprecated
    public Builder withoptions(final String... options) {
      if (options != null && options.length > 0) {
        options(options);
      }
      return this;
    }

    /**
    * @deprecated  As of release 7.0.0, replaced by {@link #options(List)}.
    */
    @Deprecated
    public Builder withoptions(final List<String> options) {
      options(options);
      return this;
    }

    public abstract com.spotify.docker.client.messages.swarm.DnsConfig build();
  }

  public static Builder builder() {
    return new AutoValue_DnsConfig.Builder();
  }

  @JsonCreator
  static com.spotify.docker.client.messages.swarm.DnsConfig create(
      @JsonProperty("Nameservers") final List<String> nameServers,
      @JsonProperty("Search") final List<String> search,
      @JsonProperty("Options") final List<String> options) {
    final Builder builder = builder();
    if (nameServers != null) {
      builder.nameServers(nameServers);
    }
    if (search != null) {
      builder.search(search);
    }
    if (options != null) {
      builder.options(options);
    }
    return builder.build();
  }

}
