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
import com.google.auto.value.AutoValue;

import java.math.BigInteger;
import org.jetbrains.annotations.NotNull;

@AutoValue
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public abstract class MemoryStats {

  @NotNull
  @JsonProperty("stats")
  public abstract Stats stats();

  @NotNull
  @JsonProperty("max_usage")
  public abstract Long maxUsage();

  @NotNull
  @JsonProperty("usage")
  public abstract Long usage();

  @NotNull
  @JsonProperty("failcnt")
  public abstract Long failcnt();

  @NotNull
  @JsonProperty("limit")
  public abstract Long limit();

  @JsonCreator
  static MemoryStats create(
      @JsonProperty("stats") final Stats stats,
      @JsonProperty("max_usage") final Long maxUsage,
      @JsonProperty("usage") final Long usage,
      @JsonProperty("failcnt") Long failcnt,
      @JsonProperty("limit") Long limit
  ) {
    return new AutoValue_MemoryStats(stats, maxUsage, usage, failcnt, limit);
  }

  @AutoValue
  public abstract static class Stats {

    @NotNull
    @JsonProperty("active_file")
    public abstract Long activeFile();

    @NotNull
    @JsonProperty("total_active_file")
    public abstract Long totalActiveFile();

    @NotNull
    @JsonProperty("inactive_file")
    public abstract Long inactiveFile();

    @NotNull
    @JsonProperty("total_inactive_file")
    public abstract Long totalInactiveFile();

    @NotNull
    @JsonProperty("cache")
    public abstract Long cache();

    @NotNull
    @JsonProperty("total_cache")
    public abstract Long totalCache();

    @NotNull
    @JsonProperty("active_anon")
    public abstract Long activeAnon();

    @NotNull
    @JsonProperty("total_active_anon")
    public abstract Long totalActiveAnon();

    @NotNull
    @JsonProperty("inactive_anon")
    public abstract Long inactiveAnon();

    @NotNull
    @JsonProperty("total_inactive_anon")
    public abstract Long totalInactiveAnon();

    @NotNull
    @JsonProperty("hierarchical_memory_limit")
    public abstract BigInteger hierarchicalMemoryLimit();

    @NotNull
    @JsonProperty("mapped_file")
    public abstract Long mappedFile();

    @NotNull
    @JsonProperty("total_mapped_file")
    public abstract Long totalMappedFile();

    @NotNull
    @JsonProperty("pgmajfault")
    public abstract Long pgmajfault();

    @NotNull
    @JsonProperty("total_pgmajfault")
    public abstract Long totalPgmajfault();

    @NotNull
    @JsonProperty("pgpgin")
    public abstract Long pgpgin();

    @NotNull
    @JsonProperty("total_pgpgin")
    public abstract Long totalPgpgin();

    @NotNull
    @JsonProperty("pgpgout")
    public abstract Long pgpgout();

    @NotNull
    @JsonProperty("total_pgpgout")
    public abstract Long totalPgpgout();

    @NotNull
    @JsonProperty("pgfault")
    public abstract Long pgfault();

    @NotNull
    @JsonProperty("total_pgfault")
    public abstract Long totalPgfault();

    @NotNull
    @JsonProperty("rss")
    public abstract Long rss();

    @NotNull
    @JsonProperty("total_rss")
    public abstract Long totalRss();

    @NotNull
    @JsonProperty("rss_huge")
    public abstract Long rssHuge();

    @NotNull
    @JsonProperty("total_rss_huge")
    public abstract Long totalRssHuge();

    @NotNull
    @JsonProperty("unevictable")
    public abstract Long unevictable();

    @NotNull
    @JsonProperty("total_unevictable")
    public abstract Long totalUnevictable();

    @NotNull
    @JsonProperty("total_writeback")
    public abstract Long totalWriteback();

    @NotNull
    @JsonProperty("writeback")
    public abstract Long writeback();

    @JsonCreator
    static Stats create(
        @JsonProperty("active_file") final Long activeFile,
        @JsonProperty("total_active_file") final Long totalActiveFile,
        @JsonProperty("inactive_file") final Long inactiveFile,
        @JsonProperty("total_inactive_file") final Long totalInactivefile,
        @JsonProperty("cache") final Long cache,
        @JsonProperty("total_cache") final Long totalCache,
        @JsonProperty("active_anon") final Long activeAnon,
        @JsonProperty("total_active_anon") final Long totalActiveAnon,
        @JsonProperty("inactive_anon") final Long inactiveAnon,
        @JsonProperty("total_inactive_anon") final Long totalInactiveAnon,
        @JsonProperty("hierarchical_memory_limit") final BigInteger hierarchicalMemoryLimit,
        @JsonProperty("mapped_file") final Long mappedFile,
        @JsonProperty("total_mapped_file") final Long totalMappedFile,
        @JsonProperty("pgmajfault") final Long pgmajfault,
        @JsonProperty("total_pgmajfault") final Long totalPgmajfault,
        @JsonProperty("pgpgin") final Long pgpgin,
        @JsonProperty("total_pgpgin") final Long totalPgpgin,
        @JsonProperty("pgpgout") final Long pgpgout,
        @JsonProperty("total_pgpgout") final Long totalPgpgout,
        @JsonProperty("pgfault") final Long pgfault,
        @JsonProperty("total_pgfault") final Long totalPgfault,
        @JsonProperty("rss") final Long rss,
        @JsonProperty("total_rss") final Long totalRss,
        @JsonProperty("rss_huge") final Long rssHuge,
        @JsonProperty("total_rss_huge") final Long totalRssHuge,
        @JsonProperty("unevictable") final Long unevictable,
        @JsonProperty("total_unevictable") final Long totalUnevictable,
        @JsonProperty("writeback") final Long writeback,
        @JsonProperty("total_writeback") final Long totalWriteback
    ) {
      return new AutoValue_MemoryStats_Stats(
          activeFile, totalActiveFile, inactiveFile, totalInactivefile, cache, totalCache,
          activeAnon, totalActiveAnon, inactiveAnon, totalInactiveAnon, hierarchicalMemoryLimit,
          mappedFile, totalMappedFile, pgmajfault, totalPgmajfault, pgpgin, totalPgpgin, pgpgout,
          totalPgpgout, pgfault, totalPgfault, rss, totalRss, rssHuge, totalRssHuge, unevictable,
          totalUnevictable, writeback, totalWriteback);
    }
  }
}
